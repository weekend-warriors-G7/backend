package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.RefreshTokenRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.TokenRequest;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")

public class UserController {
    UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/getUserDetails")
    public ResponseEntity<Map<String, String>> getUser(@RequestBody TokenRequest tokenRequest)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        User user = userService.getUser(tokenRequest.getToken());

        if (user != null)
        {
            jsonResponseMap.put("email", user.getEmail());
            jsonResponseMap.put("firstName", user.getFirstName());
            jsonResponseMap.put("lastName",  user.getLastName());
        }
        else
        {
            jsonResponseMap.put("email", "");
            jsonResponseMap.put("firstName", "");
            jsonResponseMap.put("lastName",  "");
        }
        return ResponseEntity.ok(jsonResponseMap);
    }
}
