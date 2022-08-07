package com.example.gateway.web.rest.vm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * View Model object for storing the user's key and password.
 *
 * @author peppy
 */
@ApiModel(value = "密码", description = "密码")
@Data
public class KeyAndPasswordVM {

    @ApiModelProperty("校验 key")
    private String key;

    @ApiModelProperty("新密码")
    private String newPassword;

}
