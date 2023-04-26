package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    int lockProductStock(@Param("productId") long productId,@Param("buyNum") int buyNum);

    /**
     * 修改商品库存
     * @param productId
     * @param buyNum
     */
    void unLockProductStock(@Param("productId") Long productId,@Param("buyNum") Integer buyNum);
}
