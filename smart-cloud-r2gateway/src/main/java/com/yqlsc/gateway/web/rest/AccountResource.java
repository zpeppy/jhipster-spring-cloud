package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.repository.UserRepository;
import com.yqlsc.gateway.security.SecurityUtils;
import com.yqlsc.gateway.service.UserService;
import com.yqlsc.gateway.service.dto.PasswordChangeDTO;
import com.yqlsc.gateway.service.dto.UserDTO;
import com.yqlsc.gateway.web.rest.errors.EmailAlreadyUsedException;
import com.yqlsc.gateway.web.rest.errors.InvalidPasswordException;
import com.yqlsc.gateway.web.rest.vm.KeyAndPasswordVM;
import com.yqlsc.gateway.web.rest.vm.ManagedUserVM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Objects;

/**
 * @author peppy
 */
@Api(value = "账号管理", tags = "账号管理")
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {
        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    public AccountResource(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @ApiOperation(value = "注册账号", tags = "账号管理")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerAccount(@Valid @RequestBody ManagedUserVM managedUserVm) {
        if (!checkPasswordLength(managedUserVm.getPassword())) {
            throw new InvalidPasswordException();
        }
        return userService.registerUser(managedUserVm, managedUserVm.getPassword())
                // 发送邮件通知
                // .doOnSuccess()
                .then();
    }

    @ApiOperation(value = "校验账号是否启用", tags = "账号管理")
    @GetMapping("/activate")
    public Mono<Void> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key)
                .switchIfEmpty(Mono.error(new AccountResourceException("No user was found for this activation key")))
                .then();
    }

    @ApiOperation(value = "获取认证的账号", tags = "账号管理")
    @GetMapping("/authenticate")
    public Mono<String> isAuthenticated(ServerWebExchange request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getPrincipal().map(Principal::getName);
    }

    @ApiOperation(value = "获取当前账号", tags = "账号管理")
    @GetMapping("/account")
    public Mono<UserDTO> getAccount() {
        return userService.getUserWithAuthorities()
                .map(UserDTO::new)
                .switchIfEmpty(Mono.error(new AccountResourceException("User could not be found")));
    }

    @ApiOperation(value = "更新账号信息", tags = "账号管理")
    @PostMapping("/account")
    public Mono<Void> saveAccount(@Valid @RequestBody UserDTO userDTO) {
        return SecurityUtils.getCurrentUserLogin()
                .switchIfEmpty(Mono.error(new AccountResourceException("Current user login not found")))
                .flatMap(userLogin -> userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                        .filter(existingUser -> !existingUser.getLogin().equalsIgnoreCase(userLogin))
                        .hasElement()
                        .flatMap(emailExists -> {
                            if (emailExists) {
                                throw new EmailAlreadyUsedException();
                            }
                            return userRepository.findOneByLogin(userLogin);
                        }))
                .switchIfEmpty(Mono.error(new AccountResourceException("User could not be found")))
                .flatMap(user -> userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
                        userDTO.getLangKey(), userDTO.getImageUrl()));
    }

    @ApiOperation(value = "修改密码", tags = "账号管理")
    @PostMapping(path = "/account/change-password")
    public Mono<Void> changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (!checkPasswordLength(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        return userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    @ApiOperation(value = "重置密码", tags = "账号管理")
    @PostMapping(path = "/account/reset-password/init")
    public Mono<Void> requestPasswordReset(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
                .doOnSuccess(user -> {
                    if (Objects.nonNull(user)) {
                        // 发送邮件
                    } else {
                        // Pretend the request has been successful to prevent checking which emails really exist
                        // but log that an invalid attempt has been made
                        log.warn("Password reset requested for non existing mail");
                    }
                })
                .then();
    }

    @ApiOperation(value = "重置密码", tags = "账号管理")
    @PostMapping(path = "/account/reset-password/finish")
    public Mono<Void> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
                .switchIfEmpty(Mono.error(new AccountResourceException("No user was found for this reset key")))
                .then();
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
                password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
                password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
}
