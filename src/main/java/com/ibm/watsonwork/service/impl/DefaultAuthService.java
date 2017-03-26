package com.ibm.watsonwork.service.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.watsonwork.WatsonWorkConstants;
import com.ibm.watsonwork.WatsonWorkProperties;
import com.ibm.watsonwork.model.OauthResponse;
import com.ibm.watsonwork.model.TokenResponse;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.client.AuthClient;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@org.springframework.stereotype.Service

public class DefaultAuthService implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthService.class);

    private String appToken;
    private Date appTokenExpireTime;
    private Map<String, OauthResponse> oauthResponseMap = new ConcurrentHashMap<>();

    @Autowired
    private WatsonWorkProperties watsonWorkProperties;

    @Autowired
    private AuthClient authClient;

    @Override
    public String getAppAuthToken() {
        //if we never got the token or if the token is expired, set it
        if (appTokenExpireTime == null || appTokenExpireTime.before(new Date())) {
            try {
                TokenResponse tokenResponse = authClient.authenticateApp(createAppAuthHeader(), WatsonWorkConstants.CLIENT_CREDENTIALS).execute().body();
                appTokenExpireTime = getDate(tokenResponse.getExpiresIn());
                appToken = tokenResponse.getAccessToken();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return WatsonWorkConstants.BEARER + appToken;
    }

    @Override
    public String getAppId() {
        return watsonWorkProperties.getAppId();
    }

    @Override
    public String getAppSecret() {
        return watsonWorkProperties.getAppSecret();
    }

    @Override
    public String getWebhookSecret() {
        return watsonWorkProperties.getWebhookSecret();
    }

    @Override
    public String createVerificationHeader(String responseBody) {
        return HmacUtils.hmacSha256Hex(getWebhookSecret(), responseBody);
    }

    @Override
    @SneakyThrows(IOException.class)
    public OauthResponse exchangeCodeForToken(String code, String redirectUri) {
        Response<OauthResponse> response = authClient.exchangeCodeForToken(createAppAuthHeader(), code, WatsonWorkConstants.AUTHORIZATION_CODE, redirectUri).execute();
        OauthResponse oauthResponse = response.body();
        oauthResponseMap.put(oauthResponse.getId(), oauthResponse);
        return oauthResponse;
    }

    @Override
    public boolean isValidVerificationRequest(WebhookEvent webhookEvent, String outboundToken) {
        String requestBody = String.format("{\"type\":\"verification\",\"challenge\":\"%s\"}", webhookEvent.getChallenge());
        String verificationHeader = createVerificationHeader(requestBody);
        return outboundToken.equals(verificationHeader);
    }

    @Override
    public OauthResponse getUserOAuthResponse(String userId) {
        return oauthResponseMap.getOrDefault(userId, null);
    }

    private Date getDate(Integer secondsFromNow) {
        long millisFromNow = secondsFromNow * 1000L;
        return new Date(System.currentTimeMillis() + millisFromNow);
    }

    private String createAppAuthHeader() {
        return WatsonWorkConstants.BASIC + Base64.getEncoder().encodeToString((getAppId() + ":" + getAppSecret()).getBytes());
    }
}
