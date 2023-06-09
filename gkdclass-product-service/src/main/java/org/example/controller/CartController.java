package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.request.CartItemRequest;
import org.example.service.CartService;
import org.example.utils.JsonData;
import org.example.vo.CartItemVO;
import org.example.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("购物车模块")
@RestController
@RequestMapping("/api/cart/v1")
public class CartController {

    @Autowired
    private CartService cartService;


    @ApiOperation("添加购物车")
    @PostMapping("addCart")
    public JsonData addCart(@ApiParam("购物项") @RequestBody CartItemRequest cartItemRequest){
        cartService.addToCart(cartItemRequest);
        return JsonData.buildSuccess();
    }

    @ApiOperation("清空购物车")
    @GetMapping("clearCart")
    public JsonData clearCart(){
        cartService.clearCart();
        return JsonData.buildSuccess();
    }

    @ApiOperation("查看购物车")
    @GetMapping("myCart")
    public JsonData myCart(){
        CartVO cartVO = cartService.myCart();
        return JsonData.buildSuccess(cartVO);
    }

    @ApiOperation("删除购物车")
    @PostMapping("deleteCart")
    public JsonData deleteCart(@RequestBody CartItemRequest cartItemRequest){
        cartService.deleteCart(cartItemRequest.getProductId());
        return JsonData.buildSuccess();
    }


    @ApiOperation("修改购物车商品数量")
    @PostMapping("changeCart")
    public JsonData changeCart(@RequestBody CartItemRequest cartItemRequest){
        cartService.changeCart(cartItemRequest);
        return JsonData.buildSuccess();
    }

    /**
     * 用于订单服务，获取对应的订单项确认信息
     * @return
     */
    @ApiOperation("订单确认并删除购物车中存放对应的商品项")
    @PostMapping("confirm_order_cart_items")
    public JsonData confirmOrderCartItems(@ApiParam("商品id列表") List<Long> productIdList){
        List<CartItemVO> cartItemVOList = cartService.confirmOrderCartItems(productIdList);
        return JsonData.buildSuccess(cartItemVOList);
    }
}
