package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

}
