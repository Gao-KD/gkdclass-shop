package org.example.service;

import org.example.model.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.vo.ProductVO;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
public interface ProductService {

    /**
     * 分页查询商品列表
     * @param page
     * @param size
     * @return
     */
    Map<String, Object> pageProductActivity(int page, int size);

    /**
     * 根据商品id找商品详情
     * @param productId
     * @return
     */
    ProductVO findDetailById(long productId);
}
