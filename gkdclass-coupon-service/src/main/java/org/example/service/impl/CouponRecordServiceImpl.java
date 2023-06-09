package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.LoginInterceptor;
import org.example.config.RabbitMQConfig;
import org.example.enums.BizCodeEnum;
import org.example.enums.CouponStateEnum;
import org.example.enums.ProductOrderStateEnum;
import org.example.enums.StockTaskStateEnum;
import org.example.exception.BizException;
import org.example.feign.ProductOrderFeignService;
import org.example.mapper.CouponRecordMapper;
import org.example.mapper.CouponTaskMapper;
import org.example.model.*;
import org.example.request.LockCouponRecordRequest;
import org.example.service.CouponRecordService;
import org.example.utils.JsonData;
import org.example.vo.CouponRecordVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@Slf4j
@Service
public class CouponRecordServiceImpl implements CouponRecordService {
    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponTaskMapper couponTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignService productOrderFeignService;
    /**
     * 用户优惠券记录分页
     * @param page
     * @param size
     * @return
     */
    @Override
    public Map<String, Object> pageCouponRecordActivity(int page, int size) {

        //获取用户的信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();


        //封装分页信息
        Page<CouponRecordDO > pageInfo = new Page<>(page, size);
        //查找对应的优惠券信息,根据pageInfo信息查找
        IPage<CouponRecordDO> iPage = couponRecordMapper.selectPage(pageInfo,new QueryWrapper<CouponRecordDO>()
                .eq("user_id", loginUser.getId())
                .orderByDesc("create_time" ));
        Map<String,Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", iPage.getTotal());
        pageMap.put("total_page", iPage.getPages());
        pageMap.put("current_data", iPage.getRecords().stream().map(obj->beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    public List<CouponRecordVO> findByRecordId(long recordId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<CouponRecordDO> couponRecordDOS = couponRecordMapper.selectList(new QueryWrapper<CouponRecordDO>()
                .eq("coupon_id", recordId)
                .eq("user_id", loginUser.getId()));
        if (couponRecordDOS == null){
            return null;
        }
        return beanProcessList(couponRecordDOS);
    }

    /**
     * 1)锁定优惠券
     * 2)task表插入记录
     * 3)发送延迟消息
     * @param lockCouponRecordRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public JsonData lockCouponRecords(LockCouponRecordRequest lockCouponRecordRequest) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        String orderOutTradeNo = lockCouponRecordRequest.getOrderOutTradeNo();
        List<Long> lockCouponRecordIds = lockCouponRecordRequest.getLockCouponRecordIds();

        //优惠券记录表锁定
        int updateRows = couponRecordMapper.lockUseStateBath(loginUser.getId(), CouponStateEnum.USED.name(),lockCouponRecordIds);

        //把锁定的优惠券插入到coupon_task表
        List<CouponTaskDO> collect = lockCouponRecordIds.stream().map(obj -> {
            CouponTaskDO couponTaskDO = new CouponTaskDO();
            couponTaskDO.setCreateTime(new Date());
            couponTaskDO.setOutTradeNo(orderOutTradeNo);
            couponTaskDO.setCouponRecordId(obj);
            couponTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            return couponTaskDO;
        }).collect(Collectors.toList());

        int insertRows = couponTaskMapper.insertBatch(collect);


        log.info("优惠券记录锁定updateRows={}",updateRows);
        log.info("工单锁定insertRows={}",insertRows);

        if (lockCouponRecordIds.size() == insertRows && insertRows == updateRows){
            //发送延迟消息
            for (CouponTaskDO couponTaskDO : collect){
                CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
                couponRecordMessage.setTaskId(couponTaskDO.getId());
                couponRecordMessage.setOutTradeNo(couponTaskDO.getOutTradeNo());
                //发送延迟消息
                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getCouponReleaseDelayRoutingKey(),couponRecordMessage);
                log.info("优惠券锁定消息发送mq成功:{}",couponRecordMessage.toString());
            }


            return JsonData.buildSuccess();
        }else {
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }
    }

    /**
     * 解锁优惠券记录
     * 1）查询task工单是否存在
     * 2）查询订单状态
     * @param recordMessage
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        CouponTaskDO couponTaskDO = couponTaskMapper.selectOne(new QueryWrapper<CouponTaskDO>().eq("id", recordMessage.getTaskId()));
        if (couponTaskDO == null){
            log.warn("工作单不存在,消息:{}", recordMessage);
            return true;
        }
        //匹配锁状态
        String lockState = couponTaskDO.getLockState();

        if (lockState.equalsIgnoreCase(StockTaskStateEnum.LOCK.name())){

            //查询订单状态，返回的jsonData是订单的状态
            JsonData jsonData = productOrderFeignService.queryProductOrderState(recordMessage.getOutTradeNo());

            if (jsonData.getCode() == 0){
                //正常响应，判断订单状态
                String state = jsonData.getData().toString();
                if (ProductOrderStateEnum.UNPAID.name().equalsIgnoreCase(state)){
                    //状态是UNPAID状态，则返回给消息队列，重新投递
                    log.warn("订单状态是UNPAID，返回给消息队列，重新投递:{}", recordMessage);
                    return false;
                }
                //如果已经支付
                if (ProductOrderStateEnum.PAID.name().equalsIgnoreCase(state)){
                    //修改状态为finish
                    couponTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    couponTaskMapper.update(couponTaskDO,new QueryWrapper<CouponTaskDO>().eq("id",recordMessage.getTaskId()));
                    log.info("订单已经支付，修改库存锁定工作单FINISH状态:{}",recordMessage);
                    return true;
                }
            }
            //订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL
            log.warn("订单不存在");
            //修改优惠券工单为CNACEL
            couponTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            couponTaskMapper.update(couponTaskDO,new QueryWrapper<CouponTaskDO>().eq("id",recordMessage.getTaskId()));
            //恢复优惠券记录表为NEW状态
            couponRecordMapper.updateState(couponTaskDO.getCouponRecordId(),CouponStateEnum.NEW.name());
            return true;
        }else {
            log.warn("工作单状态不是LOCK，state={},消息题={}",lockState,recordMessage);
            return true;
        }
    }

    /**
     * entity属性赋值
     * @param couponRecordDO
     * @return
     */
    private CouponRecordVO beanProcess(CouponRecordDO couponRecordDO) {
        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return couponRecordVO;
    }

    /**
     * List属性赋值
     * @param couponRecordDOList
     * @return
     */
    private List<CouponRecordVO> beanProcessList(List<CouponRecordDO> couponRecordDOList) {
        List<CouponRecordVO> couponRecordVOList = new ArrayList<>();
        for (CouponRecordDO couponRecordDO : couponRecordDOList) {
            CouponRecordVO couponRecordVO = new CouponRecordVO();
            BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
            couponRecordVOList.add(couponRecordVO);
        }
        return couponRecordVOList;
    }

}
