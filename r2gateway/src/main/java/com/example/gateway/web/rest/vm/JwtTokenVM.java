package com.example.gateway.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author peppy
 */
public class JwtTokenVM {

    private String accessToken;

    public JwtTokenVM(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
