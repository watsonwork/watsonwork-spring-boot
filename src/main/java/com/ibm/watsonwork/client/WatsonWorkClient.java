package com.ibm.watsonwork.client;

import com.ibm.watsonwork.model.FileShareResponse;
import com.ibm.watsonwork.model.Message;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WatsonWorkClient {

    @Headers({"Content-Type: application/json"})
    @POST("/v1/spaces/{spaceId}/messages")
    Call<Message> createMessage(@Header("Authorization") String authToken,
                                @Path("spaceId") String spaceId, @Body Message message);


    @Multipart
    @POST("/v1/spaces/{spaceId}/files")
    Call<FileShareResponse> shareFile(@Header("Authorization") String authToken,
                                      @Path("spaceId") String spaceId, @Part MultipartBody.Part file,
                                      @Query(value = "dim") String dim);

    @Multipart
    @POST("/photos/")
    Call<ResponseBody> uploadAppPhoto(@Header("Authorization") String authToken,
                                      @Part MultipartBody.Part file);
}
