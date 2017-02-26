package com.workspace;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties
public class WorkspaceProperties {

    @Value("${workspace.webhook.secret}")
    private String webhookSecret;

    @Value("${workspace.app.id}")
    private String appId;

    @Value("${workspace.app.secret}")
    private String appSecret;

    @Value("${workspace.api.uri}")
    private String apiUri;
}
