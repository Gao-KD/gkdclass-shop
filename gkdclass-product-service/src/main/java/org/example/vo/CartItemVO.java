package org.example.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * CartItemVO 购物项
 *
 *   商品id
 *   购买数量
 *   商品标题（冗余）
 *   商品图片（冗余）
 *   商品单价
 *   总价格 ( 单价*数量 )
 */
public class CartItemVO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("buy_num")
    private Integer buyNum;

    @JsonProperty("product_title")
    private String productTitle;

    @JsonProperty("product_img")
    private String productImg;

    private BigDecimal price;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(Integer buyNum) {
        this.buyNum = buyNum;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 商品单价 * 商品数量 = 商品总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.buyNum));
    }

    @Override
    public String toString() {
        return "CartItemVO{" +
                "productId=" + productId +
                ", buyNum=" + buyNum +
                ", productTitle='" + productTitle + '\'' +
                ", productImg='" + productImg + '\'' +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
