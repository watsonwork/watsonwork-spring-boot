package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.WebhookEvent;

public interface AuthService extends Service {

    String getAppAuthToken();

    String getAppId();

    String getAppSecret();

    String getWebhookSecret();

    String createVerificationHeader(String responseBody);

    boolean isValidVerificationRequest(WebhookEvent webhookEvent, String outBoundToken);
}
