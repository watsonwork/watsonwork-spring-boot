package com.workspace.controller;

import com.workspace.WorkspaceProperties;
import com.workspace.model.WebhookEvent;
import com.workspace.service.AuthService;
import com.workspace.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.workspace.MessageTypes.VERIFICATION;
import static com.workspace.WorkspaceConstants.X_OUTBOUND_TOKEN;
import static com.workspace.utils.MessageUtils.buildMessage;

@RestController
public class WorkspaceController {

    @Autowired
    private WorkspaceProperties workspaceProperties;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private AuthService authService;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String home(){
        return "Hello World!";
    }

    @RequestMapping(value = "webhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity webhookCallback(@RequestHeader(X_OUTBOUND_TOKEN) String outboundToken, @RequestBody WebhookEvent webhookEvent){
        if(VERIFICATION.equalsIgnoreCase(webhookEvent.getType()) && authService.isValidVerificationRequest(webhookEvent, outboundToken)) {
            return buildVerificationResponse(webhookEvent);
        }

        if(!workspaceProperties.getAppId().equals(webhookEvent.getUserId())) {
            // respond to webhook
            workspaceService.createMessage(webhookEvent.getSpaceId(), buildMessage("Echo App", webhookEvent.getContent()));
        }
        return ResponseEntity.ok().build();
    }

    private ResponseEntity buildVerificationResponse(WebhookEvent webhookEvent) {
        String responseBody = String.format("{\"response\": \"%s\"}", webhookEvent.getChallenge());

        String verificationHeader = authService.createVerificationHeader(responseBody);
        return ResponseEntity.status(HttpStatus.OK)
                .header(X_OUTBOUND_TOKEN, verificationHeader)
                .body(responseBody);
    }
}
