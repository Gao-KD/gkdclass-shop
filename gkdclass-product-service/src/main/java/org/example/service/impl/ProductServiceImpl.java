package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.model.ProductDO;
import org.example.mapper.ProductMapper;
import org.example.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Map<String, Object> pageProductActivity(int page, int size) {
        Page<ProductDO> pageInfo = new Page<>(page,size);
        IPage<ProductDO> productDOIPage = productMapper.selectPage(pageInfo, null);
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", productDOIPage.getTotal());
        pageMap.put("total_page", productDOIPage.getPages());
        pageMap.put("current_data", productDOIPage.getRecords().stream().map(
                obj -> beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        return beanProcess(productDO);
    }


    private ProductVO beanProcess(ProductDO obj) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(obj, productVO);
        //剩余库存 - 锁定库存
        productVO.setStock(productVO.getStock() - obj.getLockStock());
        return productVO;
    }
}
