package com.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.workspace.model.WebhookEvent;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

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
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setType(WorkspaceConstants.VERIFICATION);
        webhookEvent.setChallenge("j2m0dj7f4offx989gvm4pg8zpt83qqsm");

        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("X-OUTBOUND-TOKEN", "f18d241715d8e257062d8b7956a3563aa591dfd32c127cebc10b73336ec87025");
                    return execution.execute(request, body);
                }));

        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity("/webhook", webhookEvent, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        JsonNode jsonNode = getObjectMapper().readTree(responseEntity.getBody().toString());
        assertTrue(jsonNode.has("response"));
        assertThat(jsonNode.get("response").asText(), IsEqual.equalTo(webhookEvent.getChallenge()));
    }

    @Test
    public void postingInvalidVerificationEventReturns200WithNoBody() throws IOException {
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setType(WorkspaceConstants.VERIFICATION);
        webhookEvent.setChallenge("j2m0dj7f4offx989gvm4pg8zpt83qqsm");

        ResponseEntity<JsonNode> responseEntity = getTestRestTemplate().postForEntity("/webhook", webhookEvent, JsonNode.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNull(responseEntity.getBody());
    }
}
