package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.dto.UserDTO;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final JWTBearerService jwtBearerService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JWTBearerService jwtBearerService, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.jwtBearerService = jwtBearerService;
        this.passwordEncoder = passwordEncoder;
    }

    public String getJwtTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
        {
            return authorizationHeader.substring(7);
        }
        else
        {
            throw new InvalidToken("Token is missing or header is incorrect.");
        }
    }

    public UserDTO getUser(String token) throws InvalidToken, IOException {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("UserService.getUser(), invalid token");

        User retrievedUser = userRepository.findById(tokenSubject).orElse(null);

        if (retrievedUser == null)
            throw new IOException("UserService.updateUserFirstName(), database error, user not found");

        return userToDto(retrievedUser);
    }

    public UserDTO getUserById(String id) throws IOException
    {
        User retrievedUser = userRepository.findById(id).orElse(null);

        if (retrievedUser == null)
            throw new IOException("UserService.getUserById(), database error, user not found");

        return userToDto(retrievedUser);
    }

    public String getUserId(String token) throws InvalidToken, IOException
    {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("UserService.getUser(), invalid token");
        User retrievedUser = userRepository.findById(tokenSubject).orElse(null);
        if (retrievedUser == null)
            throw new IOException("UserService.getUserId(), database error, user not found");
        return retrievedUser.getId();
    }

    public UserDTO updateUserPassword(String token, String oldPassword, String newPassword) throws IOException {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("UserService.updateUserFirstName(), invalid token");

        //tokenSubject is itself the userId field
        User retrievedUser = userRepository.findById(tokenSubject).orElse(null);

        if (retrievedUser == null)
            throw new IOException("UserService.updateUserFirstName(), database error, user not found");

        if (passwordEncoder.matches(oldPassword, retrievedUser.getPassword()))
        {
            retrievedUser.setPassword(passwordEncoder.encode(newPassword));
        }
        else
        {
            throw new InvalidToken("UserService.updateUserFirstName(), old password is incorrect.");
        }

        userRepository.save(retrievedUser);
        return userToDto(retrievedUser);
    }

    public UserDTO updateUserFirstName(String token, String firstName) throws IOException {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("UserService.updateUserFirstName(), invalid token");

        //tokenSubject is itself the userId field
        User retrievedUser = userRepository.findById(tokenSubject).orElse(null);

        if (retrievedUser == null)
            throw new IOException("UserService.updateUserFirstName(), database error, user not found");

        if (firstName != null && !firstName.isEmpty())
        {
            retrievedUser.setFirstName(firstName);
        }

        userRepository.save(retrievedUser);
        return userToDto(retrievedUser);
    }

    public UserDTO updateUserLastName(String token, String lastName) throws IOException {
        String tokenSubject = jwtBearerService.extractSubject(token);
        if (!jwtBearerService.isTokenValid(token, tokenSubject))
            throw new InvalidToken("UserService.updateUserLastName(), invalid token");

        //tokenSubject is itself the userId field
        User retrievedUser = userRepository.findById(tokenSubject).orElse(null);

        if (retrievedUser == null)
            throw new IOException("UserService.updateUserLastName(), database error, user not found");

        if (lastName != null && !lastName.isEmpty())
        {
            retrievedUser.setLastName(lastName);
        }

        userRepository.save(retrievedUser);
        return userToDto(retrievedUser);
    }

    private UserDTO userToDto(User userToConvert)
    {
        UserDTO userUpdateRequest = new UserDTO();
        userUpdateRequest.setFirstName(userToConvert.getFirstName());
        userUpdateRequest.setLastName(userToConvert.getLastName());
        userUpdateRequest.setId(userToConvert.getId());
        userUpdateRequest.setRole(userToConvert.getRole().toString());
        userUpdateRequest.setEmail(userToConvert.getEmail());
        return userUpdateRequest;
    }
}
