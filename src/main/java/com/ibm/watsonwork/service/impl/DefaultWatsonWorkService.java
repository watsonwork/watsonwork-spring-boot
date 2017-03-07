package com.ibm.watsonwork.service.impl;

import com.ibm.watsonwork.client.WatsonWorkClient;
import com.ibm.watsonwork.model.Message;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.WatsonWorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Service
public class DefaultWatsonWorkService implements WatsonWorkService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultWatsonWorkService.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private WatsonWorkClient watsonWorkClient;

    @Override
    public void createMessage(String spaceId, Message message) {
        Call<Message> call = watsonWorkClient.createMessage(authService.getAppAuthToken(), spaceId, message);

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
