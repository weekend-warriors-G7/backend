package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/image")
@Getter


// STRICTLY FOR TESTING PURPOUSES!!!!
public class ImageController {

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    private ImageManagement imageManagement;

    @GetMapping("/get-access-token")
    public String getAccessToken() {
        try {
            ImageManagement imageManagement = new ImageManagement(
                    "08abae2cd826bad",
                    "f87e16cb7b0a56b2b96b5dd0bda5c445485672ae",
                    "c438678c8e2d76a69b5b05e1dcacd1070fed51b6"
            );

            return "Access Token: " + imageManagement.getAccessCode(
                    imageManagement.getRefreshToken(),
                    imageManagement.getClientId(),
                    imageManagement.getClientSecret()
            );
        } catch (IOException e) {
            e.printStackTrace();
            return "Error retrieving access token: " + e.getMessage();
        }
    }

    @PostMapping("/add-image")
    public String PostImage (@RequestBody String image64)
    {
        try
        {
            ImageManagement imageManagement = new ImageManagement(
                    "08abae2cd826bad",
                    "f87e16cb7b0a56b2b96b5dd0bda5c445485672ae",
                    "c438678c8e2d76a69b5b05e1dcacd1070fed51b6"
            );

            return "Image added! Id: " + imageManagement.uploadImage(image64);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return "Error retrieving access token: " + e.getMessage();
        }
    }

    @DeleteMapping("/delete-image/{id}")
    public Boolean DeleteImage(@PathVariable String id)
    {
        try {
            ImageManagement imageManagement = new ImageManagement(
                    "08abae2cd826bad",
                    "f87e16cb7b0a56b2b96b5dd0bda5c445485672ae",
                    "c438678c8e2d76a69b5b05e1dcacd1070fed51b6"
            );

            return imageManagement.deleteImage(id);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/{id}")
    public String GetImage(@PathVariable String id)
    {
        try {
            ImageManagement imageManagement = new ImageManagement(
                    "08abae2cd826bad",
                    "f87e16cb7b0a56b2b96b5dd0bda5c445485672ae",
                    "c438678c8e2d76a69b5b05e1dcacd1070fed51b6"
            );
            return imageManagement.getImageLink(id);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @PostMapping(value = "/upload-file", consumes = {"multipart/form-data"})
    public String uploadImageFile(@RequestPart("file") MultipartFile file) {
        try {
            ImageManagement imageManagement = new ImageManagement(
                    "08abae2cd826bad",
                    "f87e16cb7b0a56b2b96b5dd0bda5c445485672ae",
                    "c438678c8e2d76a69b5b05e1dcacd1070fed51b6"
            );

            // Convert MultipartFile to File
            File tempFile = convertMultiPartToFile(file);
            String imageId = imageManagement.uploadImageFile(tempFile);

            // Delete temporary file
            tempFile.delete();

            return "Image uploaded! Id: " + imageId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading image: " + e.getMessage();
        }
    }
}
