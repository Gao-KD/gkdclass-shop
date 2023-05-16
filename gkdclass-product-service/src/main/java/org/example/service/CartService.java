package org.example.service;

import org.example.request.CartItemRequest;
import org.example.vo.CartItemVO;
import org.example.vo.CartVO;

import java.util.List;

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

    /**
     * 删除购物车商品
     * @param productId
     */
    void deleteCart(Long productId);

    /**
     * 修改购物车商品数量
     * @param cartItemRequest
     */
    void changeCart(CartItemRequest cartItemRequest);

    List<CartItemVO> confirmOrderCartItems(List<Long> productIdList);
}
