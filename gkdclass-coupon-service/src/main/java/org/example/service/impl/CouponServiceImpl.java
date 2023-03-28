package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.LoginInterceptor;
import org.example.enums.BizCodeEnum;
import org.example.enums.CouponCategoryEnum;
import org.example.enums.CouponPulishEnum;
import org.example.enums.CouponStateEnum;
import org.example.exception.BizException;
import org.example.mapper.CouponMapper;
import org.example.mapper.CouponRecordMapper;
import org.example.model.CouponDO;
import org.example.model.CouponRecordDO;
import org.example.model.LoginUser;
import org.example.service.CouponRecordService;
import org.example.service.CouponService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.vo.CouponVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@Service
@Slf4j
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponRecordMapper couponRecordMapper;


    @Override
    public Map<String, Object> pageCouponActivity(int page, int size) {
        Page<CouponDO> pageInfo = new Page<>(page, size);
        IPage<CouponDO> couponDOIPage = couponMapper.selectPage(pageInfo, new QueryWrapper<CouponDO>()
                .eq("publish", CouponPulishEnum.PUBLISH)
                .eq("category", CouponCategoryEnum.PROMOTION)
                .orderByDesc("create_time"));

        Map<String, Object> pageMap = new HashMap<>(3);
        //总条数
        pageMap.put("total_record", couponDOIPage.getTotal());
        //总页数
        pageMap.put("total_page", couponDOIPage.getPages());
        //总的内容
        pageMap.put("current_data", couponDOIPage.getRecords().stream()
                .map(obj -> beanProcess(obj))
                .collect(Collectors.toList()));
        return pageMap;
    }

    /**
     * 领取优惠券
     * 1、获取优惠券是否存在
     * 2、校验优惠是否可以领取：日期，类型，库存，超过限制
     * 3、扣件库存
     * 4、保存领券记录
     *
     * @param couponId
     * @param category
     * @return
     */
    @Override
    public JsonData receiveCoupon(long couponId, CouponCategoryEnum category) {
        //1.要先获取用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        CouponDO couponDO = couponMapper.selectOne(new QueryWrapper<CouponDO>()
                //判断优惠券id
                .eq("id", couponId)
                //判断优惠券类型是否合法
                .eq("category", category.name()));

        //判断优惠券是否可以领取
        this.checkCoupon(couponDO, loginUser.getId());

        //没异常，就构建领券记录
        CouponRecordDO couponRecordDO = new CouponRecordDO();
        BeanUtils.copyProperties(couponDO, couponRecordDO);
        couponRecordDO.setCouponId(couponId);
        couponRecordDO.setUseState(CouponStateEnum.NEW.name());
        couponRecordDO.setCreateTime(new Date());
        couponRecordDO.setUserId(loginUser.getId());
        couponRecordDO.setUserName(loginUser.getName());
        //自增
        couponRecordDO.setId(null);
        log.info("用户信息:{},优惠券信息:{}", loginUser.getName(), couponDO.getId());
        //TODO CouponRecordServiceImpl实现插入


        //扣减库存
        int rows = 1;
        if (rows == 1){
            couponRecordMapper.insert(couponRecordDO);
        }else {
            log.warn("发放优惠券失败:{},用户:{}",couponDO.getCouponTitle(),loginUser.getName());
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }

        return JsonData.buildSuccess();
    }

    /**
     * 判断优惠券是否可以领取
     */
    private void checkCoupon(CouponDO couponDO, Long user_id) {

        //判断优惠券是否查到
        if (couponDO == null) {
            throw  new BizException(BizCodeEnum.COUPON_NO_EXIST);
        }
        //判断是否还有库存
        if (couponDO.getStock() <= 0) {
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }


        //判断优惠券状态
        if(!couponDO.getPublish().equals(CouponPulishEnum.PUBLISH.name())){
            throw new BizException(BizCodeEnum.COUPON_STATE_ILLEGAL);
        }

        //判断领取时间
        long time = System.currentTimeMillis();
        long start = couponDO.getStartTime().getTime();
        long end = couponDO.getEndTime().getTime();
        if (time > end || time < start) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }

        //判断用户领取限制(去用户领取记录里面比较)
        int recordNum = couponRecordMapper.selectCount(new QueryWrapper<CouponRecordDO>()
                .eq("user_id", user_id)
                .eq("coupon_id", couponDO.getId()));
        if (recordNum >= couponDO.getUserLimit()){
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMITE);
        }
    }

    /**
     * 类型转换，couponDO转未couponVO
     *
     * @param couponDO
     * @return
     */
    private CouponVO beanProcess(CouponDO couponDO) {
        CouponVO couponVO = new CouponVO();
        BeanUtils.copyProperties(couponDO, couponVO);
        return couponVO;
    }
}
