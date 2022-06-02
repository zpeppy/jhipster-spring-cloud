package com.example.common.exception.obj;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author peppy
 */
@ApiModel(value = "ErrorInfo 错误信息", description = "ErrorInfo 错误信息")
@Data
public class ErrorInfo implements Serializable {

    private static final long serialVersionUID = 6691334422995037195L;

    @ApiModelProperty(value = "异常类型")
    private String type;

    @ApiModelProperty(value = "异常标题")
    private String title;

    @ApiModelProperty(value = "异常状态码")
    private int status;

    @ApiModelProperty(value = "异常详情")
    private String detail;

    @ApiModelProperty(value = "请求的地址")
    private String path;

    @ApiModelProperty(value = "错误信息")
    private String message;

    @ApiModelProperty(value = "调用方法名")
    private String methodKey;

    @ApiModelProperty(value = "错误信息")
    private String reason;

    //

    @ApiModelProperty(value = "报错时间")
    private String time;

    @ApiModelProperty(value = "异常 code")
    private String code;

    @ApiModelProperty(value = "堆栈错误信息")
    private String stackTrace;

}
