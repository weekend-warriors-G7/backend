package com.weekendwarriors.weekend_warriors_backend.util;

import com.weekendwarriors.weekend_warriors_backend.config.AllowedEndpoints;
import com.weekendwarriors.weekend_warriors_backend.enums.JWTTokenType;
import com.weekendwarriors.weekend_warriors_backend.service.CustomUserDetailsService;
import com.weekendwarriors.weekend_warriors_backend.service.JWTBearerService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTBearerService jwtBearerService;
    private final CustomUserDetailsService userDetailsService;

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String errorMessage) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", errorMessage));
    }

    private Pattern[] convertPublicUrlsToPatterns() {
        return Arrays.stream(AllowedEndpoints.PUBLIC_URLS)
                .map(url -> url.replace("/**", "(/.*)?"))
                .map(url -> Pattern.compile("^" + url + "$"))
                .toArray(Pattern[]::new);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        boolean isPublicUrl = Arrays.stream(convertPublicUrlsToPatterns())
                .anyMatch(pattern -> pattern.matcher(request.getRequestURI()).matches());

        if (isPublicUrl) {
            filterChain.doFilter(request, response);
            return;
        }

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            this.writeErrorResponse(response, HttpStatus.BAD_REQUEST, "Authorization header is missing or invalid");
            return;
        }

        try
        {
            jwt = authHeader.substring(7);
            userId = jwtBearerService.extractSubject(jwt);
            JWTTokenType tokenType = jwtBearerService.extractTokenType(jwt);

            if (userId != null && tokenType == JWTTokenType.ACCESS && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                if (jwtBearerService.isTokenValid(jwt, userId)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    filterChain.doFilter(request, response);
                    return;
                }
            }
            this.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT token is invalid");
        }
        catch (ExpiredJwtException ex) {
            this.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT token has expired");
        } catch (Exception e) {
            this.writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}
