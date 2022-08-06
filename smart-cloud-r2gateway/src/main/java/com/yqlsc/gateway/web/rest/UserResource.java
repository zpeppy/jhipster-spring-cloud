package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.config.Constants;
import com.yqlsc.gateway.domain.User;
import com.yqlsc.gateway.repository.UserRepository;
import com.yqlsc.gateway.security.AuthoritiesConstants;
import com.yqlsc.gateway.service.UserService;
import com.yqlsc.gateway.service.dto.UserDTO;
import com.yqlsc.gateway.web.rest.errors.BadRequestAlertException;
import com.yqlsc.gateway.web.rest.errors.EmailAlreadyUsedException;
import com.yqlsc.gateway.web.rest.errors.LoginAlreadyUsedException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author peppy
 */
@Api(value = "用户管理", tags = "用户管理")
@RestController
@RequestMapping("/api")
public class UserResource {
    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(Arrays.asList("id", "login", "firstName", "lastName", "email", "activated", "langKey"));

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;

    private final UserRepository userRepository;

    public UserResource(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @ApiOperation(value = "新建用户", tags = "用户管理")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        }
        return userRepository.findOneByLogin(userDTO.getLogin().toLowerCase())
                .hasElement()
                .flatMap(loginExists -> {
                    if (Boolean.TRUE.equals(loginExists)) {
                        return Mono.error(new LoginAlreadyUsedException());
                    }
                    return userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
                })
                .hasElement()
                .flatMap(emailExists -> {
                    if (Boolean.TRUE.equals(emailExists)) {
                        return Mono.error(new EmailAlreadyUsedException());
                    }
                    return userService.createUser(userDTO);
                })
                // 发送邮件通知
                // .doOnSuccess()
                .map(user -> {
                    try {
                        return ResponseEntity.created(new URI("/api/users/" + user.getLogin()))
                                .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", user.getLogin()))
                                .body(user);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ApiOperation(value = "修改用户信息", tags = "用户管理")
    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<UserDTO>> updateUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to update User : {}", userDTO);
        return userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                .filter(user -> !user.getId().equals(userDTO.getId()))
                .hasElement()
                .flatMap(emailExists -> {
                    if (Boolean.TRUE.equals(emailExists)) {
                        return Mono.error(new EmailAlreadyUsedException());
                    }
                    return userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
                })
                .filter(user -> !user.getId().equals(userDTO.getId()))
                .hasElement()
                .flatMap(loginExists -> {
                    if (Boolean.TRUE.equals(loginExists)) {
                        return Mono.error(new LoginAlreadyUsedException());
                    }
                    return userService.updateUser(userDTO);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(user -> ResponseEntity.ok()
                        .headers(HeaderUtil.createAlert(applicationName, "userManagement.updated", userDTO.getLogin()))
                        .body(user)
                );
    }

    @ApiOperation(value = "查询用户信息列表", tags = "用户管理")
    @GetMapping("/users")
    public Mono<ResponseEntity<Flux<UserDTO>>> getAllUsers(ServerHttpRequest request, Pageable pageable) {
        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return userService.countManagedUsers()
                .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
                .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
                .map(headers -> ResponseEntity.ok().headers(headers).body(userService.getAllManagedUsers(pageable)));
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @ApiOperation(value = "查询角色列表", tags = "用户管理")
    @GetMapping("/users/authorities")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<List<String>> getAuthorities() {
        return userService.getAuthorities().collectList();
    }

    @ApiOperation(value = "查询指定用户信息", tags = "用户管理")
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    public Mono<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return userService.getUserWithAuthoritiesByLogin(login)
                .map(UserDTO::new)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @ApiOperation(value = "删除指定用户信息", tags = "用户管理")
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        return userService.deleteUser(login)
                .map(it -> ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login)).build());
    }
}
