package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.Message;

public interface WatsonWorkService extends Service{

    void createMessage(String spaceId,Message message);
}
