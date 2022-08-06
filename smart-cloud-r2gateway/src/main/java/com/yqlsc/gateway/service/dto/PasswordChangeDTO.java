package com.yqlsc.gateway.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码时使用的 DTO
 *
 * @author peppy
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PasswordChangeDTO {

    private String currentPassword;

    private String newPassword;

}
