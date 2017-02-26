package com.workspace.service.impl;

import com.workspace.client.WorkspaceClient;
import com.workspace.model.Message;
import com.workspace.service.AuthService;
import com.workspace.service.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Service
public class DefaultWorkspaceService implements WorkspaceService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultWorkspaceService.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private WorkspaceClient workspaceClient;

    @Override
    public void createMessage(String spaceId, Message message) {
        Call<Message> call = workspaceClient.createMessage(authService.getAppAuthToken(), spaceId, message);

        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                LOGGER.info("Message successfully posted to Inbound Webhook.");
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                LOGGER.error("Posting message to Inbound Webhook failed.", t);
            }
        });
    }
}
