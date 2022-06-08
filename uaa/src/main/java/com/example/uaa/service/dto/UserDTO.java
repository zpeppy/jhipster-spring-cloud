package com.example.uaa.service.dto;


import com.example.common.config.Constants;
import com.example.uaa.domain.Authority;
import com.example.uaa.domain.User;
import com.google.common.collect.Sets;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
@NoArgsConstructor
@Data
@ApiModel(value = "用户信息", description = "用户信息")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 9076801770248800759L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("账号")
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "账号格式不匹配")
    @Size(min = 1, max = 50, message = "账号长度为1至50")
    private String login;

    @ApiModelProperty("姓")
    @Size(max = 50, message = "姓最大长度为50")
    private String firstName;

    @ApiModelProperty("名")
    @Size(max = 50, message = "名最大长度为50")
    private String lastName;

    @ApiModelProperty("邮箱")
    @Email(message = "邮箱格式不符合")
    @Size(min = 5, max = 254, message = "邮箱内容长度为5至254")
    private String email;

    @ApiModelProperty("图片")
    @Size(max = 256, message = "图片地址最大长度为256")
    private String imageUrl;

    @ApiModelProperty("是否启用")
    private boolean activated = false;

    @ApiModelProperty("语言")
    @Size(min = 2, max = 10, message = "语言内容长度为2至10")
    private String langKey;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private Instant createdDate;

    @ApiModelProperty("最后修改人")
    private String lastModifiedBy;

    @ApiModelProperty("最后修改时间")
    private Instant lastModifiedDate;

    @ApiModelProperty("角色列表")
    private Set<String> authorities = Sets.newHashSet();

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.getActivated();
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
