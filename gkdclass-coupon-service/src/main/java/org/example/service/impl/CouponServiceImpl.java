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
import org.omg.CORBA.TIMEOUT;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
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
    // 如果有事务, 那么加入事务, 没有的话新建一个(默认情况下)
    @Transactional(rollbackFor=Exception.class,propagation= Propagation.REQUIRED)
    @Override
    public JsonData receiveCoupon(long couponId, CouponCategoryEnum category) {

        /*

        String uuid = CommonUtil.generateUUID();
        String lockKey = "lock:coupon:" + couponId;
        //十分钟过期时间
        Boolean lockFlag = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 10,TimeUnit.MINUTES);
        if (lockFlag) {
            log.info("加锁成功:{}", couponId);
            try {
                //执行业务逻辑

            } finally {
                //解锁 采用lua脚本
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                Integer result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList(lockKey), uuid);
                log.info("解锁:{}",result);

            }
        } else {
            //加锁失败
            try {
                //睡眠
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error("自旋失败");
            }
            //自旋
            receiveCoupon(couponId, category);
        }
**/

//        synchronized(this) {  分布式集群部署的时候会失效
        //1.要先获取用户信息


        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        String lockKey = "lock:coupon:"+couponId;
        RLock rlock = redissonClient.getLock(lockKey);
        //多个线程进入会阻塞等待
        rlock.lock();
        log.info("领券接口加锁成功:{}",Thread.currentThread().getId());
        try {
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


            //扣减库存 要加分布式锁
            int rows = couponMapper.reduceStock(couponId);


            if (rows == 1) {
                // CouponRecordServiceImpl实现插入
                couponRecordMapper.insert(couponRecordDO);
            } else {
                log.warn("发放优惠券失败:{},用户:{}", couponDO.getCouponTitle(), loginUser.getName());
                throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
            }
        }finally {
            //解锁
            rlock.unlock();
            log.info("领券接口解锁成功");
        }

//        }
        return JsonData.buildSuccess();
    }

    /**
     * 判断优惠券是否可以领取
     */
    private void checkCoupon(CouponDO couponDO, Long user_id) {

        //判断优惠券是否查到
        if (couponDO == null) {
            throw new BizException(BizCodeEnum.COUPON_NO_EXIST);
        }
        //判断是否还有库存
        if (couponDO.getStock() <= 0) {
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }


        //判断优惠券状态
        if (!couponDO.getPublish().equals(CouponPulishEnum.PUBLISH.name())) {
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
        if (recordNum >= couponDO.getUserLimit()) {
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
