package com.workspace.integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.workspace.model.VerificationRequest;
import com.workspace.model.WebhookEvent;
import com.workspace.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static com.workspace.MessageTypes.VERIFICATION;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class WorkspaceApplicationTests extends BaseWorkspaceApplicationTests {

    @Test
    public void postingWebhookEventReturns200() throws IOException {
        String jsonPayload = FileUtils.readFileToString(new File("src/test/resources/message-created-event.json"), StandardCharsets.UTF_8);
        WebhookEvent webhookEvent = getObjectMapper().readValue(jsonPayload, WebhookEvent.class);

        ResponseEntity responseEntity = getTestRestTemplate().postForEntity("/webhook", webhookEvent, ResponseEntity.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void postingVerificationEventReturnsVerificationResponse() throws IOException {
        VerificationRequest verificationRequest = new VerificationRequest(VERIFICATION, Utils.generateSecret());

        ResponseEntity<JsonNode> responseEntity = getTestRestTemplate().postForEntity("/webhook", verificationRequest, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        JsonNode jsonNode = getObjectMapper().readTree(responseEntity.getBody().toString());
        assertTrue(jsonNode.has("response"));
        assertThat(jsonNode.get("response").asText(), IsEqual.equalTo(verificationRequest.getChallenge()));
    }

    @Test
    public void postingInvalidVerificationEventReturns200WithNoBody() throws IOException {
        VerificationRequest verificationRequest = new VerificationRequest(VERIFICATION, null);

        ResponseEntity<JsonNode> responseEntity = getTestRestTemplate().postForEntity("/webhook", verificationRequest, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNull(responseEntity.getBody());
    }
}
