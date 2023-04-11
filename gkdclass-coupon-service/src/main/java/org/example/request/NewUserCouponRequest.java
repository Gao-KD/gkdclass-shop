package org.example.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiModel
public class NewUserCouponRequest {


    @ApiModelProperty(value = "用户Id",example = "19")
    @JsonProperty("user_id")
    private long userId;

    @ApiModelProperty(value = "用户名称",example = "gaokd")
    @JsonProperty("name")
    private String name;


}
