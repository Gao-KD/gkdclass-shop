package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.ProductOrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
@Mapper
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItemDO> {

}
