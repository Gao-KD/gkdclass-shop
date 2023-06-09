package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    int reduceStock(long couponId);
}
