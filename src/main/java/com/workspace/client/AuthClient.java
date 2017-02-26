package com.workspace.client;

import com.workspace.model.OauthResponse;
import com.workspace.model.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthClient {

    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<TokenResponse> authenticateApp(@Header("Authorization") String basicAuthorization,
                                        @Field("grant_type") String grantType);

    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<OauthResponse> exchangeCodeForToken(@Header("Authorization") String basicAuthorization, @Field("code") String code, @Field("grant_type") String grantType,
                                             @Field("redirect_uri") String redirectUri);
}
