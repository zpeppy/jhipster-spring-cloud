package com.example.gateway.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO representing a password change required data - current and new password.
 *
 * @author peppy
 */
@ApiModel(value = "密码", description = "密码")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PasswordChangeDTO {

    @ApiModelProperty("当前密码")
    private String currentPassword;

    @ApiModelProperty("新密码")
    private String newPassword;

}
