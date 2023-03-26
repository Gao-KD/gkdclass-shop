package org.example.request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * request包为接受前端传递的Json对象
 */

@ApiModel(value = "用户注册对象",description = "用户注册请求对象")
@Data
public class UserRegisterRequest {
    @ApiModelProperty(value = "昵称",example = "gaokd")
    private String name;

    @ApiModelProperty(value = "密码",example = "123456")
    private String pwd;

    @ApiModelProperty(value = "签名",example = "人生需要动态规划，学习需要贪心算法")
    private String slogan;

    @ApiModelProperty(value = "性别，0表示女，1表示男",example = "1")
    private Integer sex;

    @ApiModelProperty(value = "邮箱",example = "819978029@qq.com")
    private String mail;

    @ApiModelProperty(value = "验证码",example = "123456")
    private String code;
}
