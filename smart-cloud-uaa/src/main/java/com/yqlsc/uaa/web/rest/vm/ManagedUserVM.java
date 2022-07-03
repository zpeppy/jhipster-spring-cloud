package com.yqlsc.uaa.web.rest.vm;

import com.yqlsc.uaa.service.dto.UserDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author peppy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "管理用户信息", description = "管理用户信息")
public class ManagedUserVM extends UserDTO {
    private static final long serialVersionUID = -6020634631273345491L;

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

    @ApiModelProperty("密码")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "密码长度为4至100")
    private String password;

}
