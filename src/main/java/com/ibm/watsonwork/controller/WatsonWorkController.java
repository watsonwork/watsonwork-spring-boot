package com.ibm.watsonwork.controller;

import java.io.File;
import java.io.FileNotFoundException;

import com.ibm.watsonwork.WatsonWorkProperties;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.WatsonWorkService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.ibm.watsonwork.MessageTypes.VERIFICATION;
import static com.ibm.watsonwork.WatsonWorkConstants.X_OUTBOUND_TOKEN;
import static com.ibm.watsonwork.utils.MessageUtils.buildMessage;

@RestController
public class WatsonWorkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatsonWorkController.class);

    @Autowired
    private WatsonWorkProperties watsonWorkProperties;

    @Autowired
    private WatsonWorkService watsonWorkService;

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

        if(StringUtils.isNotEmpty(webhookEvent.getUserId()) && !StringUtils.equals(watsonWorkProperties.getAppId(), webhookEvent.getUserId())) {
            /* respond to webhook */

            // send an echo message
            watsonWorkService.createMessage(webhookEvent.getSpaceId(), buildMessage("Echo App", webhookEvent.getContent()));

            // upload a sample file/image
            // Remove the following block of code if you do not want to share a file on every echo message. This is just an example.
            File file = null;
            try {
                file = ResourceUtils.getFile("classpath:watson-work.jpg");
            } catch (FileNotFoundException e) {
                LOGGER.error("File not found.", e);
            }
            watsonWorkService.shareFile(webhookEvent.getSpaceId(), file, "256x256");
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
