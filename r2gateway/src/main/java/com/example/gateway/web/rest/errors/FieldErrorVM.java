package com.example.gateway.web.rest.errors;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author peppy
 */
@ApiModel(value = "字段错误信息", description = "字段错误信息")
@Getter
@AllArgsConstructor
public class FieldErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("对象名")
    private final String objectName;

    @ApiModelProperty("字段名")
    private final String field;

    @ApiModelProperty("错误信息")
    private final String message;

}
