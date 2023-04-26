package org.example.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class OrderItemRequest {

    @JsonProperty("product_id")
    private long productId;

    @JsonProperty("buy_num")
    private int buyNum;

}
