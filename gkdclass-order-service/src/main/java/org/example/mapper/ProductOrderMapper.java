package org.example.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.ProductOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.utils.JsonData;
import org.example.vo.OrderItemVO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrderDO> {
}
