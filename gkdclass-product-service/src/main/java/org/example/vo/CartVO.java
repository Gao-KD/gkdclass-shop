package org.example.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车信息:
    * 1、商品总件数
    * 2、整个购物车总价
    * 3、实际支付总价
 */
public class CartVO {

    private List<CartItemVO> cartItemLists;

    @JsonProperty("total_num")
    private Integer totalNum;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("real_pay_price")
    private BigDecimal realPayPrice;

    public void setCartItemLists(List<CartItemVO> cartItemLists) {
        this.cartItemLists = cartItemLists;
    }

    public List<CartItemVO> getCartItemLists() {
        return cartItemLists;
    }

    /**
     * 总件数
     * @return
     */
    public Integer getTotalNum() {
        if (this.cartItemLists != null){
            int total = cartItemLists.stream().mapToInt(CartItemVO::getBuyNum).sum();
            return total;
        }
        return 0;
    }

    /**
     * 总价格
     * @return
     */
    public BigDecimal getTotalPrice() {
        BigDecimal price = BigDecimal.ZERO;
        if (this.cartItemLists != null){
            for (CartItemVO cartItemVO : cartItemLists){
                BigDecimal totalPrice = cartItemVO.getTotalPrice();
                price = price.add(totalPrice);
            }
        }
        return price;
    }

    /**
     * 实际支付价格
     * @return
     */
    public BigDecimal getRealPayPrice() {
        BigDecimal price = new BigDecimal("0");
        if (this.cartItemLists != null){
            for (CartItemVO cartItemVO : cartItemLists){
                BigDecimal totalPrice = cartItemVO.getTotalPrice();
                price = price.add(totalPrice);
            }
        }
        return price;
    }

    @Override
    public String toString() {
        return "CartVO{" +
                "cartItemLists=" + cartItemLists +
                ", totalNum=" + totalNum +
                ", totalPrice=" + totalPrice +
                ", realPayPrice=" + realPayPrice +
                '}';
    }
}
