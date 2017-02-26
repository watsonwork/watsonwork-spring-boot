package com.workspace.service;

import com.workspace.model.WebhookEvent;

public interface AuthService extends Service {

    String getAppAuthToken();

    String getAppId();

    String getAppSecret();

    String getWebhookSecret();

    String createVerificationHeader(String responseBody);

    boolean isValidVerificationRequest(WebhookEvent webhookEvent, String outBoundToken);
}
