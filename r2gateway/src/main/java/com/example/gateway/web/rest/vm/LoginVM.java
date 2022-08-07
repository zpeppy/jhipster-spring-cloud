package com.example.gateway.web.rest.vm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * View Model object for storing a user's credentials.
 *
 * @author peppy
 */
@ApiModel(value = "登录信息", description = "登录信息")
@Data
public class LoginVM {

    @ApiModelProperty(value = "账号", required = true)
    @NotBlank(message = "账号不能为空")
    @Size(min = 1, max = 50, message = "账号长度为1至50")
    private String username;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 100, message = "密码长度为4至100")
    private String password;

    @ApiModelProperty(value = "是否记住我")
    private Boolean rememberMe;

}
