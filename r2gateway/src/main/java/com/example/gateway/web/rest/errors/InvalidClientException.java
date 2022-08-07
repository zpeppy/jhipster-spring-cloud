package com.example.gateway.web.rest.errors;

/**
 * @author peppy
 */
public class InvalidClientException extends RuntimeException {

    public InvalidClientException(String msg) {
        super(msg);
    }

    public int getHttpErrorCode() {
        return 401;
    }

    public String getOAuth2ErrorCode() {
        return "invalid_client";
    }

}
