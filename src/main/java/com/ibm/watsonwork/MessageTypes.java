package com.ibm.watsonwork;

public class MessageTypes {

    // Webhook event types
    public static final String MESSAGE_CREATED = "message-created";
    public static final String SPACE_MEMBERS_ADDED = "space-members-added";
    public static final String SPACE_MEMBERS_REMOVED = "space-members-removed";
    public static final String MESSAGE_ANNOTATION_ADDED = "message-annotation-added";
    public static final String MESSAGE_ANNOTATION_REMOVED = "message-annotation-removed";
    public static final String MESSAGE_ANNOTATION_EDITED = "message-annotation-edited";
    public static final String VERIFICATION = "verification";



    // Webhook annotation types
    public static final String GENERIC_ANNOTATION = "generic";
    public static final String MENTION_ANNOTATION = "mention";
    public static final String MOMENT_ANNOTATION = "conversation-moment";
    public static final String FOCUS_ANNOTATION = "message-focus";

    // Inbound webhook message type
    public static final String APP_MESSAGE = "appMessage";

}
