package com.example.gateway.service.dto;

import com.example.gateway.config.Constants;
import com.example.gateway.domain.Authority;
import com.example.gateway.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 *
 * @author peppy
 */
@ApiModel(value = "用户信息", description = "用户信息")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty(value = "账号", required = true)
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "账号格式不匹配")
    @Size(min = 1, max = 50, message = "账号长度为1至50")
    private String login;

    @ApiModelProperty(value = "姓")
    @Size(max = 50)
    private String firstName;

    @ApiModelProperty(value = "名")
    @Size(max = 50)
    private String lastName;

    @ApiModelProperty(value = "邮箱")
    @Email
    @Size(min = 5, max = 254)
    private String email;

    @ApiModelProperty(value = "头像")
    @Size(max = 256)
    private String imageUrl;

    @ApiModelProperty(value = "是否启用")
    private boolean activated = false;

    @ApiModelProperty(value = "语言")
    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.imageUrl = user.getImageUrl();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.toSet());
    }

}
