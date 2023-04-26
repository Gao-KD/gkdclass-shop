package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.model.CouponRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@Mapper
public interface CouponRecordMapper extends BaseMapper<CouponRecordDO> {

    /**
     * 批量跟新优惠券实用记录
     * @param userId
     * @param useState
     * @param lockCouponRecordIds
     * @return
     */
    int lockUseStateBath(@Param("userId") Long userId,@Param("useState") String useState,@Param("lockCouponRecordIds") List<Long> lockCouponRecordIds);

    /**
     * 更新优惠券记录表为NEW
     * @param couponRecordId
     * @param useState
     */
    void updateState(@Param("couponRecordId") Long couponRecordId,@Param("useState") String useState);
}
