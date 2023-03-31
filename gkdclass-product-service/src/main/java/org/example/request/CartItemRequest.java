package org.example.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class CartItemRequest {

    @ApiModelProperty(value = "购买商品id")
    @JsonProperty("product_id")
    private Long productId;

    @ApiModelProperty(value = "购买数量")
    @JsonProperty("buy_num")
    private Integer buyNum;


}
