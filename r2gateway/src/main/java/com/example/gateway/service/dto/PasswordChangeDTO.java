package com.example.gateway.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO representing a password change required data - current and new password.
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
