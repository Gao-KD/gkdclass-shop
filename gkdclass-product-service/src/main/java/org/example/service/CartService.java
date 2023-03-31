package org.example.service;

import org.example.request.CartItemRequest;
import org.example.vo.CartVO;

public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartItemRequest
     */
    void addToCart(CartItemRequest cartItemRequest);

    /**
     * 清空购物车
     */
    void clearCart();

    /**
     * 查看购物车
     * @return
     */
    CartVO myCart();
}
