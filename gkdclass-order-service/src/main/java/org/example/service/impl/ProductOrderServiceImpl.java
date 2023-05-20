package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.LoginInterceptor;
import org.example.enums.BizCodeEnum;
import org.example.enums.CouponStateEnum;
import org.example.exception.BizException;
import org.example.feign.CouponFeignService;
import org.example.feign.ProductFeignService;
import org.example.feign.UserFeignService;
import org.example.model.LoginUser;
import org.example.model.ProductOrderDO;
import org.example.mapper.ProductOrderMapper;
import org.example.request.ConfirmOrderRequest;
import org.example.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.vo.CouponRecordVO;
import org.example.vo.OrderItemVO;
import org.example.vo.ProductOrderAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
@Service
@Slf4j
public class ProductOrderServiceImpl  implements ProductOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private UserFeignService userFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private CouponFeignService couponFeignService;
    /**
     * 防重提交
     * 用户微服务-确认收货地址
     * 商品微服务-获取最新购物项和价格
     * 订单验价
     * 优惠券微服务-获取优惠券
     * 验证价格
     * 锁定优惠券
     * 锁定商品库存
     * 创建订单对象
     * 创建子订单对象
     * 发送延迟消息-用于自动关单
     * 创建支付信息-对接三方支付
     */
    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);


        //获取收货地址详情
        ProductOrderAddressVO addressVO = this.getUserAddress(orderRequest.getAddressId());

        log.info("收货地址信息:{}",addressVO);

        //获取用户确认的商品信息
        List<Long> productIdList = orderRequest.getProductIdList();
        JsonData cartItemDate = productFeignService.ConfirmOrderCartItem(productIdList);

        List<OrderItemVO> orderItemList  =  cartItemDate.getData(new TypeReference<List<OrderItemVO>>(){});
        if (orderItemList == null){
            //购物车商品不存在
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST);
        }

        //验证价格，减去商品优惠券
        this.checkPrice(orderItemList,orderRequest);

/**
        //验证价格，减去商品优惠券
        this.checkPrice(orderItemList,orderRequest);

        //锁定优惠券
        this.lockCouponRecords(orderRequest ,orderOutTradeNo );

        //锁定库存
        this.lockProductStocks(orderItemList,orderOutTradeNo);


        //创建订单
        ProductOrderDO productOrderDO = this.saveProductOrder(orderRequest,loginUser,orderOutTradeNo,addressVO);

        //创建订单项
        this.saveProductOrderItems(orderOutTradeNo,productOrderDO.getId(),orderItemList);

        //发送延迟消息，用于自动关单 TODO


        //创建支付  TODO
*/

        return null;
    }

    /**
     * 验证价格
     * 1）统计全部商品的价格
     * 2）获取优惠券（判断是否满足优惠券的条件）
     * @param orderItemList
     * @param orderRequest
     */
    private void checkPrice(List<OrderItemVO> orderItemList, ConfirmOrderRequest orderRequest) {
        //统计商品总价格
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null){
            for (OrderItemVO itemVO : orderItemList) {
                BigDecimal itemRealPayAmount = itemVO.getAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
            //获取优惠券，判断是否可以使用
            CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());
            //计算购物车价格是否满足优惠券满减条件
            if (couponRecordVO != null){
                //计算是否符合满减
                if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0 ){
                    throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
                }else if (couponRecordVO.getConditionPrice().compareTo(realPayAmount) > 0){
                    realPayAmount = BigDecimal.ZERO;
                }else {
                    realPayAmount = realPayAmount.subtract(couponRecordVO.getConditionPrice());
                }
            }
            if (realPayAmount.compareTo(orderRequest.getRealPayPrice()) != 0){
                log.error("订单验价失败");
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
            }
        }
    }

    /**
     * 获取优惠券
     * @param couponRecordId
     * @return
     */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {
        if (couponRecordId == null || couponRecordId == 0){
            return null;
        }
        JsonData userCouponRecordById = couponFeignService.findUserCouponRecordById(couponRecordId);
        if (userCouponRecordById.getCode() != 0){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }else {
            CouponRecordVO data = userCouponRecordById.getData(new TypeReference<CouponRecordVO>(){});
            if (!couponAvaliable(data)){
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVALIABLE);
            }
            return data;
        }
    }

    private boolean couponAvaliable(CouponRecordVO data) {
        if (data.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())){
            long currentTimestamp = CommonUtil.getCurrentTimestamp();
            long end = data.getEndTime().getTime();
            long start = data.getStartTime().getTime();
            if (currentTimestamp >= start && currentTimestamp <= end){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO orderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
        if (orderDO == null){
            return "";
        }else {
            return orderDO.getState();
        }
    }

    /**
     * 查询订单接口
     * @param user_id
     * @return
     */
    @Override
    public List<ProductOrderDO> queryOrderByUserId(Long user_id) {
        List<ProductOrderDO> productOrderDOList = productOrderMapper.selectList(new QueryWrapper<ProductOrderDO>().eq("user_id",user_id));
        return productOrderDOList;
    }



    public ProductOrderAddressVO getUserAddress(long addressId){
        JsonData jsonData = userFeignService.detail(addressId);
        if (jsonData.getCode() != 0) {
            log.error("获取地址失败");
            throw new BizException(BizCodeEnum.ADDRESS_NO_EXITS);
        }
        ProductOrderAddressVO addressVO = jsonData.getData(new TypeReference<ProductOrderAddressVO>(){});

        return addressVO;
    }
}
