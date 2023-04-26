package org.example.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.enums.BizCodeEnum;
import org.example.request.LockProductRequest;
import org.example.service.ProductService;
import org.example.utils.JsonData;
import org.example.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
@Api("商品模块")
@RestController
@RequestMapping("/api/product/v1")
public class ProductController {

    @Autowired
    private ProductService productService;


    @ApiOperation("商品分页列表")
    @GetMapping("page")
    public JsonData pageProductList(
            @ApiParam(value = "当前页") @RequestParam(value = "page", defaultValue = "1") int page,
            @ApiParam(value = "每页显示多少条") @RequestParam(value = "size", defaultValue = "10") int size){

        Map<String, Object> pageMap = productService.pageProductActivity(page, size);
        return JsonData.buildSuccess(pageMap);
    }

    @ApiOperation("商品详情")
    @GetMapping("/detail")
    public JsonData detail(@ApiParam(value = "商品id",required = true)@RequestParam(value = "product_id") long product_id){
        ProductVO productVO = productService.findDetailById(product_id);
        return JsonData.buildSuccess(productVO);
    }

    @ApiOperation("商品库存锁定")
    @PostMapping("/lock_products")
    public JsonData lockProducts(@ApiParam(value = "商品库存锁定对象")@RequestBody LockProductRequest lockProductRequest){
        JsonData jsonData = productService.lockProductStock(lockProductRequest);
        return jsonData;
    }
}

