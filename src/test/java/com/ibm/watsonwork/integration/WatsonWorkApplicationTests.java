package com.ibm.watsonwork.integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.watsonwork.MessageTypes;
import com.ibm.watsonwork.model.VerificationRequest;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class WatsonWorkApplicationTests extends BaseWatsonWorkApplicationTests {

    @Test
    public void postingWebhookEventReturns200() throws IOException {
        String jsonPayload = FileUtils.readFileToString(new File("src/test/resources/message-created-event.json"), StandardCharsets.UTF_8);
        WebhookEvent webhookEvent = getObjectMapper().readValue(jsonPayload, WebhookEvent.class);

        ResponseEntity responseEntity = getTestRestTemplate().postForEntity("/webhook", webhookEvent, ResponseEntity.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void postingVerificationEventReturnsVerificationResponse() throws IOException {
        VerificationRequest verificationRequest = new VerificationRequest(MessageTypes.VERIFICATION, Utils.generateSecret());

        ResponseEntity<JsonNode> responseEntity = getTestRestTemplate().postForEntity("/webhook", verificationRequest, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        JsonNode jsonNode = getObjectMapper().readTree(responseEntity.getBody().toString());
        assertTrue(jsonNode.has("response"));
        assertThat(jsonNode.get("response").asText(), IsEqual.equalTo(verificationRequest.getChallenge()));
    }

    @Test
    public void postingInvalidVerificationEventReturns200WithNoBody() throws IOException {
        VerificationRequest verificationRequest = new VerificationRequest(MessageTypes.VERIFICATION, null);

        ResponseEntity<JsonNode> responseEntity = getTestRestTemplate().postForEntity("/webhook", verificationRequest, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNull(responseEntity.getBody());
    }
}
