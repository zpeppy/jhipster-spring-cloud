package com.example.uaa.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO representing a password change required data - current and new password.
 *
 * @author peppy
 */
@ApiModel(value = "密码", description = "密码")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PasswordChangeDTO implements Serializable {

    private static final long serialVersionUID = 6031182957650404921L;

    @ApiModelProperty("当前密码")
    private String currentPassword;

    @ApiModelProperty("新密码")
    private String newPassword;

}
