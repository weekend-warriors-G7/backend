package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads the user details based on the provided email.
     *
     * <p>This method overrides the {@code loadUserByUsername} method from the {@link UserDetailsService}
     * interface, which is called internally by the {@link DaoAuthenticationProvider}'s
     * {@code retrieveUser} method during the authentication process.
     * The {@code retrieveUser} method uses this to load user information
     * for authentication and authorization.
     *
     * <p>Although the method is named {@code loadUserByUsername}, it has been implemented to load
     * the user by their email address instead of a traditional username. This is because the
     * application uses email-based authentication, and the {@code email} field is unique within
     * the {@code UserRepository}.
     *
     * <p>Since the {@link UsernamePasswordAuthenticationToken} in the authentication process
     * is configured to use an email, this implementation ensures that the correct user details
     * are loaded for authentication.
     *
     * @param email the email address of the user trying to authenticate.
     * @return a {@link UserDetails} object containing the user's email, password, and authorities.
     * @throws UsernameNotFoundException if no user is found with the given email address.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), user.getAuthorities());
    }

    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), user.getAuthorities());
    }
}
