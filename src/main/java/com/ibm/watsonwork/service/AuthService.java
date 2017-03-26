package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.OauthResponse;
import com.ibm.watsonwork.model.WebhookEvent;

public interface AuthService extends Service {

    String getAppAuthToken();

    String getAppId();

    String getAppSecret();

    String getWebhookSecret();

    String createVerificationHeader(String responseBody);

    OauthResponse exchangeCodeForToken(String code, String redirectUri);

    boolean isValidVerificationRequest(WebhookEvent webhookEvent, String outBoundToken);

    OauthResponse getUserOAuthResponse(String id);


}
