package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.Message;

public interface WorkspaceService extends Service{

    void createMessage(String spaceId,Message message);
}
