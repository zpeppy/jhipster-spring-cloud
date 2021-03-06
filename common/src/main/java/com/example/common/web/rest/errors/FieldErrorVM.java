package com.example.common.web.rest.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 字段错误视图对象
 *
 * @author peppy
 */
@Getter
@AllArgsConstructor
public class FieldErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String objectName;

    private final String field;

    private final String message;

}
