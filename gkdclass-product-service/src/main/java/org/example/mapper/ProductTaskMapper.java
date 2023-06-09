package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.ProductTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaokd
 * @since 2023-04-11
 */
@Mapper
public interface ProductTaskMapper extends BaseMapper<ProductTaskDO> {

}
