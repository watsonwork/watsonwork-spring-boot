package com.ibm.watsonwork.utils;

import java.util.Collections;

import com.ibm.watsonwork.MessageTypes;
import com.ibm.watsonwork.model.Annotation;
import com.ibm.watsonwork.model.Message;

public class MessageUtils {

    public static Message buildMessage(String messageTitle, String messageText) {
        Annotation annotation = new Annotation();
        annotation.setType(MessageTypes.GENERIC_ANNOTATION);
        annotation.setVersion(1.0);
        annotation.setColor("#1DB954");
        annotation.setTitle(messageTitle);
        annotation.setText(messageText);

//        Actor actor = new Actor();
//        actor.setUrl("");
//        actor.setAvatar("");
//        actor.setName("");
//        annotation.setActor(actor);

        Message message = new Message();
        message.setType(MessageTypes.APP_MESSAGE);
        message.setVersion(1.0);
        message.setAnnotations(Collections.singletonList(annotation));

        return message;
    }
}
