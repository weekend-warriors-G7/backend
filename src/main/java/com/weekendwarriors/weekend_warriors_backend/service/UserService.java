package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final JWTBearerService jwtBearerService;

    public UserService(UserRepository userRepository, JWTBearerService jwtBearerService)
    {
        this.userRepository = userRepository;
        this.jwtBearerService = jwtBearerService;
    }

    public User getUser(String token) throws InvalidToken
    {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("invalid token");


        //token subject contains the userId
        return userRepository.findById(tokenSubject).orElse(null);
    }
}
