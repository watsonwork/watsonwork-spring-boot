package com.ibm.watsonwork.model;

import java.util.List;

import lombok.Data;

@Data
public class WebhookEvent {

    private String annotationId;
    private String annotationPayload;
    private String annotationType;
    private String challenge;
    private String content;
    private String contentType;
    private List<String> memberIds;
    private String messageId;
    private String spaceId;
    private String spaceName;
    private String time;
    private String type;
    private String userId;
    private String userName;
}
