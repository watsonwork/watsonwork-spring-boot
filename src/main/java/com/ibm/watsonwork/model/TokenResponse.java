package com.ibm.watsonwork.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    private String id;
    private String jti;
    private String scope;
    @JsonProperty("bearer_type")
    private String bearerType;
    @JsonProperty("token_type")
    private String tokenType;
}
