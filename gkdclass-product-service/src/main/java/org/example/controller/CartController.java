package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.request.CartItemRequest;
import org.example.service.CartService;
import org.example.utils.JsonData;
import org.example.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
