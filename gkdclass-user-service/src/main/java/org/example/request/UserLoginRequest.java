package org.example.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(value = "登陆对象" ,description = "登陆请求对象")
@Data
public class UserLoginRequest {

    private String name;

    private String pwd;

    private String mail;
}
