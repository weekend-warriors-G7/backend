package com.weekendwarriors.weekend_warriors_backend.calls;

import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
@Component
public class ImageManagement {
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String accessCode;
    private final WebClient webClient;
    private final String defaultImageId = "YWDk8ZY";

    public ImageManagement(
            @Value("${imgur.client.id}") String clientId,
            @Value("${imgur.client.secret}") String clientSecret,
            @Value("${imgur.refresh.token}") String refreshToken) throws IOException {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.webClient = WebClient.builder().build();
        this.accessCode = getAccessCode(refreshToken, clientId, clientSecret);
    }

    public String getAccessCode(String refreshToken, String clientId, String clientSecret) throws IOException {
        try {
            String response = webClient.post()
                    .uri("https://api.imgur.com/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("refresh_token", refreshToken)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("grant_type", "refresh_token"))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("access_token");
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to get access token: " + e.getResponseBodyAsString(), e);
        }
    }

    public String uploadImage(String image64) throws IOException {
        try {
            String response = webClient.post()
                    .uri("https://api.imgur.com/3/image")
                    .header("Authorization", "Bearer " + this.accessCode)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("image", image64))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonObjectComplete = new JSONObject(response);
            JSONObject jsonObject = jsonObjectComplete.getJSONObject("data");
            return jsonObject.getString("id");
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to upload image: " + e.getResponseBodyAsString(), e);
        }
    }

    public Boolean deleteImage(String imageId) throws IOException {
        if(imageExists(imageId)) {
            try {
                webClient.delete()
                        .uri("https://api.imgur.com/3/image/" + imageId)
                        .header("Authorization", "Bearer " + this.accessCode)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                return true;
            } catch (WebClientResponseException e) {
                throw new IOException("Failed to delete image: " + e.getResponseBodyAsString(), e);
            }
        }
        else
            return false;
    }

    public Boolean imageExists(String imageId) throws IOException {
        try {
            webClient.get()
                    .uri("https://api.imgur.com/3/image/" + imageId)
                    .header("Authorization", "Bearer " + this.accessCode)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to check if image exists: " + e.getResponseBodyAsString(), e);
        }
    }

    public String getImageLink(String imageId) throws IOException {
        return "https://i.imgur.com/"+imageId+".jpeg";
    }

    public String uploadImageFile(File imageFile) throws IOException {
        try {
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());

            String response = webClient.post()
                    .uri("https://api.imgur.com/3/image")
                    .header("Authorization", "Bearer " + this.accessCode)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("image", fileContent))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonObjectComplete = new JSONObject(response);
            JSONObject jsonObject = jsonObjectComplete.getJSONObject("data");
            return jsonObject.getString("id");
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to upload image file: " + e.getResponseBodyAsString(), e);
        }
    }

    public String provideDefaultImageId()
    {
        return defaultImageId;
    }
}