package com.workspace.service.impl;

import java.util.Base64;
import java.util.Date;

import com.workspace.WorkspaceProperties;
import com.workspace.client.AuthClient;
import com.workspace.model.TokenResponse;
import com.workspace.model.WebhookEvent;
import com.workspace.service.AuthService;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.workspace.WorkspaceConstants.BASIC;
import static com.workspace.WorkspaceConstants.BEARER;
import static com.workspace.WorkspaceConstants.CLIENT_CREDENTIALS;

@org.springframework.stereotype.Service

public class DefaultAuthService implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthService.class);

    private String appToken;
    private Date appTokenExpireTime;

    @Autowired
    private WorkspaceProperties workspaceProperties;

    @Autowired
    private AuthClient authClient;

    @Override
    public String getAppAuthToken() {
        //if we never got the token or if the token is expired, set it
        if (appTokenExpireTime == null || appTokenExpireTime.before(new Date())) {
            try {
                TokenResponse tokenResponse = authClient.authenticateApp(createAppAuthHeader(), CLIENT_CREDENTIALS).execute().body();
                appTokenExpireTime = getDate(tokenResponse.getExpiresIn());
                appToken = tokenResponse.getAccessToken();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return BEARER + appToken;
    }

    @Override
    public String getAppId() {
        return workspaceProperties.getAppId();
    }

    @Override
    public String getAppSecret() {
        return workspaceProperties.getAppSecret();
    }

    @Override
    public String getWebhookSecret() {
        return workspaceProperties.getWebhookSecret();
    }

    @Override
    public String createVerificationHeader(String responseBody) {
        return HmacUtils.hmacSha256Hex(getWebhookSecret(), responseBody);
    }

    @Override
    public boolean isValidVerificationRequest(WebhookEvent webhookEvent, String outboundToken) {
        String requestBody = String.format("{\"type\":\"verification\",\"challenge\":\"%s\"}", webhookEvent.getChallenge());
        String verificationHeader = createVerificationHeader(requestBody);
        return outboundToken.equals(verificationHeader);
    }

    private Date getDate(Integer secondsFromNow) {
        long millisFromNow = secondsFromNow * 1000L;
        return new Date(System.currentTimeMillis() + millisFromNow);
    }

    private String createAppAuthHeader() {
        return BASIC + Base64.getEncoder().encodeToString((getAppId() + ":" + getAppSecret()).getBytes());
    }
}
