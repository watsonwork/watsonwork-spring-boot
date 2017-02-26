package com.workspace.service;

import com.workspace.model.Message;

public interface WorkspaceService extends Service{

    void createMessage(String spaceId,Message message);
}
