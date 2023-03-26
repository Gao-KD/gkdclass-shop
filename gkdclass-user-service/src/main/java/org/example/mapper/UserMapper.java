package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Gaokd
 * @since 2023-03-16
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}
