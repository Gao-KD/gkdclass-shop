package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.LoginInterceptor;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.feign.UserFeignService;
import org.example.model.LoginUser;
import org.example.model.ProductOrderDO;
import org.example.mapper.ProductOrderMapper;
import org.example.request.ConfirmOrderRequest;
import org.example.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.vo.OrderItemVO;
import org.example.vo.ProductOrderAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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
/**
        //获取用户加入购物车的商品
        List<Long> productIdList = orderRequest.getProductIdList();

        JsonData cartItemDate = productFeignService.confirmOrderCartItem(productIdList);
        List<OrderItemVO> orderItemList  = cartItemDate.getData(new TypeReference<T>(){});
        log.info("获取的商品:{}",orderItemList);
        if(orderItemList == null){
            //购物车商品不存在
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }

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
