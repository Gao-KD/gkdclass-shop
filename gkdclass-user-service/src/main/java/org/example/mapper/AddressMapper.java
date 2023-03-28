package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.AddressDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 电商-公司收发货地址表 Mapper 接口
 * </p>
 *
 * @author Gaokd
 * @since 2023-03-16
 */
@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {

}
