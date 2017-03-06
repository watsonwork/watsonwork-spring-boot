package com.ibm.watsonwork.service.impl;

import java.util.Base64;
import java.util.Date;

import com.ibm.watsonwork.WorkspaceConstants;
import com.ibm.watsonwork.WorkspaceProperties;
import com.ibm.watsonwork.model.TokenResponse;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.client.AuthClient;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
                TokenResponse tokenResponse = authClient.authenticateApp(createAppAuthHeader(), WorkspaceConstants.CLIENT_CREDENTIALS).execute().body();
                appTokenExpireTime = getDate(tokenResponse.getExpiresIn());
                appToken = tokenResponse.getAccessToken();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return WorkspaceConstants.BEARER + appToken;
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
        return WorkspaceConstants.BASIC + Base64.getEncoder().encodeToString((getAppId() + ":" + getAppSecret()).getBytes());
    }
}
