package com.ibm.watsonwork.service.impl;

import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.ibm.watsonwork.client.WatsonWorkClient;
import com.ibm.watsonwork.model.FileShareResponse;
import com.ibm.watsonwork.model.Message;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.WatsonWorkService;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ibm.watsonwork.MessageTypes.FORM_DATA_FILE;

@Service
public class DefaultWatsonWorkService implements WatsonWorkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWatsonWorkService.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private WatsonWorkClient watsonWorkClient;

    @Override
    public void createMessage(@NotNull String spaceId, @NotNull Message message) {
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

    @Override
    public void shareFile(@NotNull String spaceId, @NotNull File file, String dimensions) {
        MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(FORM_DATA_FILE, file.getName(), RequestBody.create(mediaType, file));

        Call<FileShareResponse> fileShareResponseCall = watsonWorkClient.shareFile(authService.getAppAuthToken(), spaceId, filePart, dimensions);
        fileShareResponseCall.enqueue(new Callback<FileShareResponse>() {
            @Override
            public void onResponse(Call<FileShareResponse> call, Response<FileShareResponse> response) {
                LOGGER.info("File successfully shared to space");
            }

            @Override
            public void onFailure(Call<FileShareResponse> call, Throwable t) {
                LOGGER.error("Sharing file to space failed.", t);
            }
        });
    }

    @Override
    @SneakyThrows(FileNotFoundException.class)
    @PostConstruct
    public void uploadAppPhoto() {
        File file = ResourceUtils.getFile("classpath:app-photo.jpg");

        MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(FORM_DATA_FILE, file.getName(), RequestBody.create(mediaType, file));

        Call<ResponseBody> uploadAppPhoto = watsonWorkClient.uploadAppPhoto(authService.getAppAuthToken(), filePart);
        uploadAppPhoto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
                    LOGGER.error("Failed to upload app photo. Supported Media Type is .jpg or .jpeg");
                }
                LOGGER.info("App photo successfully uploaded");

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LOGGER.error("Failed to upload app photo.", t);
            }
        });
    }
}
