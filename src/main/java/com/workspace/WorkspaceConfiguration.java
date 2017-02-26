package com.workspace;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.client.AuthClient;
import com.workspace.client.WorkspaceClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class WorkspaceConfiguration {

    @Autowired
    private WorkspaceProperties workspaceProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public Retrofit retrofit(OkHttpClient client) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(workspaceProperties.getApiUri())
                .client(client)
                .build();
    }

    @Bean
    public WorkspaceClient workspaceClient(Retrofit retrofit) {
        return retrofit.create(WorkspaceClient.class);
    }

    @Bean
    public AuthClient authClient(Retrofit retrofit) {
        return retrofit.create(AuthClient.class);
    }
}
