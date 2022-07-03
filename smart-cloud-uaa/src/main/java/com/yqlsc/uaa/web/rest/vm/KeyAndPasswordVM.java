package com.yqlsc.uaa.web.rest.vm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author peppy
 */
@ApiModel(value = "账号密码", description = "账号密码")
@Data
public class KeyAndPasswordVM implements Serializable {

    private static final long serialVersionUID = -299878870339989218L;

    @ApiModelProperty("账号")
    private String key;

    @ApiModelProperty("密码")
    private String newPassword;

}
