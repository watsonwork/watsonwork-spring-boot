package com.ibm.watsonwork.service;

import java.io.File;

import com.ibm.watsonwork.model.Message;

public interface WatsonWorkService extends Service{

    void createMessage(String spaceId, Message message);

    void shareFile(String spaceId, File file, String dimensions);

    void uploadAppPhoto();
}
