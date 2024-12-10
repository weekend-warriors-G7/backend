package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.*;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")

public class UserController {
    UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getUser(HttpServletRequest request)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String token;
        UserDTO user;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            user = userService.getUser(token);
            jsonResponseMap.put("email", user.getEmail());
            jsonResponseMap.put("firstName", user.getFirstName());
            jsonResponseMap.put("lastName",  user.getLastName());
        }
        catch (Exception exception)
        {
            jsonResponseMap.put("error", exception.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }



        return ResponseEntity.ok(jsonResponseMap);
    }

    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getUserRole(HttpServletRequest request)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String token;
        UserDTO user;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            user = userService.getUser(token);

            jsonResponseMap.put("role", user.getRole());
        }
        catch (Exception exception)
        {
            jsonResponseMap.put("error", exception.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        return ResponseEntity.ok(jsonResponseMap);
    }

    @GetMapping("/id")
    public ResponseEntity<Map<String, String>> getUserId(HttpServletRequest request)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String token;
        UserDTO user;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            user = userService.getUser(token);

            jsonResponseMap.put("id", user.getId());
        }
        catch (Exception exception)
        {
            jsonResponseMap.put("error", exception.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        return ResponseEntity.ok(jsonResponseMap);
    }


    @PutMapping("/updateUserFirstName")
    public ResponseEntity<Map<String, String>> updateUserFirstName(HttpServletRequest request,@Valid @RequestBody FirstNameUpdateRequest updateRequest)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String firstName = updateRequest.getFirstName();
        UserDTO userDTO;
        String token;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            userDTO = userService.updateUserFirstName(token, firstName);
        }
        catch (Exception e)
        {
            jsonResponseMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        jsonResponseMap.put("email", userDTO.getEmail());
        jsonResponseMap.put("firstName", userDTO.getFirstName());
        jsonResponseMap.put("lastName",  userDTO.getLastName());

        return ResponseEntity.ok(jsonResponseMap);
    }

    @PutMapping("/updateUserLastName")
    public ResponseEntity<Map<String, String>> updateUserLastName(HttpServletRequest request,@Valid @RequestBody LastNameUpdateRequest updateRequest)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String lastName = updateRequest.getLastName();
        UserDTO userDTO;
        String token;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            userDTO = userService.updateUserLastName(token, lastName);
        }
        catch (Exception e)
        {
            jsonResponseMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        jsonResponseMap.put("email", userDTO.getEmail());
        jsonResponseMap.put("firstName", userDTO.getFirstName());
        jsonResponseMap.put("lastName",  userDTO.getLastName());

        return ResponseEntity.ok(jsonResponseMap);
    }

    @PutMapping("/updateUserPassword")
    public ResponseEntity<Map<String, String>> updateUserPassword(HttpServletRequest request,@Valid @RequestBody PasswordUpdateRequest updateRequest)
    {
        HashMap<String, String> jsonResponseMap = new HashMap<>();
        String oldPassword = updateRequest.getOldPassword();
        String newPassword = updateRequest.getNewPassword();
        UserDTO userDTO;
        String token;

        try
        {
            token = userService.getJwtTokenFromRequest(request);
        }
        catch (InvalidToken invalidToken)
        {
            jsonResponseMap.put("invalid request header", invalidToken.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        try
        {
            userDTO = userService.updateUserPassword(token, oldPassword, newPassword);
        }
        catch (Exception e)
        {
            jsonResponseMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(jsonResponseMap);
        }

        jsonResponseMap.put("email", userDTO.getEmail());
        jsonResponseMap.put("firstName", userDTO.getFirstName());
        jsonResponseMap.put("lastName",  userDTO.getLastName());

        return ResponseEntity.ok(jsonResponseMap);
    }
}
