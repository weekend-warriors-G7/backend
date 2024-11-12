package com.weekendwarriors.weekend_warriors_backend.calls;

import okhttp3.*;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Getter
@Component
public class ImageManagement
{
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String accesCode;

    public ImageManagement(@Value("${imgur.client.id}") String clientId, @Value("${imgur.client.secret}") String clientSecret, @Value("${imgur.refresh.token}") String refreshToken) throws IOException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.accesCode = getAccessCode(refreshToken, clientId, clientSecret);
    }

    public static String getAccessCode(String refreshToken, String clientId, String clientSecret) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();

        FormBody body = new FormBody.Builder()
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "refresh_token")
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/oauth2/token")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful() && response.body() != null)
            {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                return jsonObject.getString("access_token");
            }
            else
            {
                throw new IOException("Unexpected code: " + response);
            }
        }
    }

    public String uploadImage(String image64) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody body = new FormBody.Builder()
                .add("image", image64)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image")
                .post(body)
                .addHeader("Authorization", "Bearer " + this.getAccesCode())
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful() && response.body() != null)
            {
                String responseBody = response.body().string();
                JSONObject jsonObjectComplete = new JSONObject(responseBody);
                JSONObject jsonObject = jsonObjectComplete.getJSONObject("data");
                return jsonObject.getString("id");
            }
            else
            {
                throw new IOException("Unexpected code: " + response);
            }
        }
    }

    public Boolean deleteImage(String imageId) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image/"+imageId)
                .delete()
                .addHeader("Authorization", "Bearer " + this.getAccesCode())
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful() && response.body() != null)
            {
                return true;
            }
            else
            {
                throw new IOException("Unexpected code: " + response);
            }
        }
    }

    public Boolean imageExists(String imageId) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image/"+imageId)
                .get()
                .addHeader("Authorization", "Bearer " + this.getAccesCode())
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful())
            {
                return true;
            }
            else
            {
                throw new IOException("Unexpected code: " + response);
            }
        }
    }

    public String getImageLink(String imageId) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image/"+imageId)
                .get()
                .addHeader("Authorization", "Bearer " + this.getAccesCode())
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful())
            {
                String responseBody = response.body().string();
                JSONObject jsonObjectComplete = new JSONObject(responseBody);
                JSONObject jsonObject = jsonObjectComplete.getJSONObject("data");
                return jsonObject.getString("link");
            }
            else
            {
                throw new IOException("Unexpected code: " + response);
            }
        }
    }

    public String uploadImageFile(File imageFile) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();

        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/*"));

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image")
                .post(body)
                .addHeader("Authorization", "Bearer " + this.getAccesCode())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonObjectComplete = new JSONObject(responseBody);
                JSONObject jsonObject = jsonObjectComplete.getJSONObject("data");
                return jsonObject.getString("id");
            } else {
                throw new IOException("Unexpected response code: " + response);
            }
        }
    }
}
