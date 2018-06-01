package com.ibm.watsonwork.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.watsonwork.WatsonWorkProperties;
import com.ibm.watsonwork.model.OauthResponse;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.WatsonWorkService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import static com.ibm.watsonwork.MessageTypes.MESSAGE_CREATED;
import static com.ibm.watsonwork.MessageTypes.VERIFICATION;
import static com.ibm.watsonwork.WatsonWorkConstants.CALLBACK_TEMPLATE;
import static com.ibm.watsonwork.WatsonWorkConstants.CLIENT_ID_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.CODE_VALUE;
import static com.ibm.watsonwork.WatsonWorkConstants.COOKIE_ID_VALUE;
import static com.ibm.watsonwork.WatsonWorkConstants.HTTPS_OAUTH_CALLBACK;
import static com.ibm.watsonwork.WatsonWorkConstants.INDEX_TEMPLATE;
import static com.ibm.watsonwork.WatsonWorkConstants.NAME_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.REDIRECT_URI_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.RESPONSE_TYPE_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.STATE_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.STATE_VALUE;
import static com.ibm.watsonwork.WatsonWorkConstants.WATSONWORK_AUTH_URI_KEY;
import static com.ibm.watsonwork.WatsonWorkConstants.X_OUTBOUND_TOKEN;
import static com.ibm.watsonwork.utils.MessageUtils.buildMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Slf4j
public class WatsonWorkController {

    @Autowired
    private WatsonWorkProperties watsonWorkProperties;

    @Autowired
    private WatsonWorkService watsonWorkService;

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String hello(@CookieValue(value = COOKIE_ID_VALUE, required = false) String idCookie, Map<String, Object> model, HttpServletRequest request,
                        HttpServletResponse response) {
        populateHtmlTemplate(model, request);

        if (idCookie == null) {
            return INDEX_TEMPLATE;
        } else {
            OauthResponse oauthResponse = authService.getUserOAuthResponse(idCookie);
            if (oauthResponse == null) {
                //delete cookies, we don't have this person any more
                response.addCookie(new Cookie(COOKIE_ID_VALUE, null));
                return INDEX_TEMPLATE;
            } else {
                model.put(NAME_KEY, oauthResponse.getDisplayName());
                return CALLBACK_TEMPLATE;
            }
        }
    }

    @SneakyThrows(IOException.class)
    @GetMapping(value = "/oauthCallback")
    public String oauthCallback(@RequestParam(CODE_VALUE) String code, @RequestParam(STATE_KEY) String state, Map<String, Object> model,
                                HttpServletRequest request, HttpServletResponse response) {
        Assert.isTrue(StringUtils.equals(state, STATE_VALUE), "State value is not equal.");
        OauthResponse oauthResponse = authService.exchangeCodeForToken(code, String.format(HTTPS_OAUTH_CALLBACK, request.getServerName()));

        // At this point you have the user token. Your app can now act on behalf of the user.

        response.addCookie(new Cookie(COOKIE_ID_VALUE, oauthResponse.getId()));
        model.put(NAME_KEY, oauthResponse.getDisplayName());
        response.sendRedirect("/");
        return CALLBACK_TEMPLATE;
    }

    @PostMapping(value = "/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity webhookCallback(@RequestHeader(X_OUTBOUND_TOKEN) String outboundToken, @RequestBody String webhookEventString) {
		//log.info("Raw webHookEvent : \"" + webhookEventString + "\"" );

		if(!verifyWebHookRequest(webhookEventString, outboundToken))
		{
			log.info("In the WebHook request failed security verifcation:" + webhookEventString);
	        return ResponseEntity.status(403).build();     // return a forbidden status
		}
        
	    ObjectMapper objectMapper = new ObjectMapper();          
        WebhookEvent webhookEvent;
        try
		{
			webhookEvent = objectMapper.readValue(webhookEventString, WebhookEvent.class);
		}
		catch(Exception e)
		{
			log.info("In the WebHook body string not parsed:" + webhookEventString);
	        return ResponseEntity.status(400).build();     // return a bad request status
		}

        if (VERIFICATION.equalsIgnoreCase(webhookEvent.getType())) {
            log.info("building verification response...");
            return buildVerificationResponse(webhookEvent);
        }
        processWebhook(webhookEvent);
        return ResponseEntity.ok().build();
    }

    private void processWebhook(WebhookEvent webhookEvent) {
        log.info("processing webhook event...");
        if (StringUtils.equals(watsonWorkProperties.getAppId(), webhookEvent.getUserId())) {
            log.info("ignoring self messages...");
            return;
        }

        if (MESSAGE_CREATED.equalsIgnoreCase(webhookEvent.getType())) {
            if (StringUtils.isNotEmpty(webhookEvent.getUserId()) && !StringUtils.equals(watsonWorkProperties.getAppId(), webhookEvent.getUserId())) {
            /* respond to webhook */

                // send an echo message
                watsonWorkService.createMessage(webhookEvent.getSpaceId(), buildMessage("Echo App", webhookEvent.getContent()));
      
                // upload a sample file/image
                // Remove the following block of code if you do not want to share a file on every echo message. This is just an example.
                File file = null;
                try {
                    file = ResourceUtils.getFile("classpath:watson-work.jpg");
                } catch (FileNotFoundException e) {
                    log.error("File not found.", e);
                }
                watsonWorkService.shareFile(webhookEvent.getSpaceId(), file, "256x256");
      
            }
        }
    }

    private ResponseEntity buildVerificationResponse(WebhookEvent webhookEvent) {
        String responseBody = String.format("{\"response\": \"%s\"}", webhookEvent.getChallenge());
        if(webhookEvent.getChallenge() == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	
        String verificationHeader = authService.createVerificationHeader(responseBody);
        log.info("webhook verified...");
        return ResponseEntity.status(HttpStatus.OK)
                .header(X_OUTBOUND_TOKEN, verificationHeader)
                .body(responseBody);
    }


    private boolean verifyWebHookRequest(String body, String header)
    {
		try
		{
			String verification = authService.createVerificationHeader(body);
			if(verification.equals(header))
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			log.info("Exception verifying webhook request; likely bad webhook secret key?");
			return false;
		}
    }



    private void populateHtmlTemplate(Map<String, Object> model, HttpServletRequest request) {
        model.put(WATSONWORK_AUTH_URI_KEY, watsonWorkProperties.getOauthApi());
        model.put(RESPONSE_TYPE_KEY, CODE_VALUE);
        model.put(CLIENT_ID_KEY, watsonWorkProperties.getAppId());
        model.put(REDIRECT_URI_KEY, String.format(HTTPS_OAUTH_CALLBACK, request.getServerName()));
        model.put(STATE_KEY, STATE_VALUE);
    }
}
