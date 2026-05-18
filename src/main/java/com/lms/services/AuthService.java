package com.lms.services;

import com.lms.dtos.AuthenticationResponse;
import com.lms.dtos.LoginRequest;
import com.lms.dtos.RegisterRequest;
import com.lms.entities.User;
import com.lms.repositories.UserRepository;
import com.lms.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Handles login and registration. The only service that mints JWTs directly. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /** Hash password, save, immediately issue a JWT so the user is signed in. */
    public AuthenticationResponse register(RegisterRequest request) {
        User user = new User(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),    // never store plaintext
            request.getFirstName(),
            request.getLastName(),
            request.getRole()
        );
        userRepository.save(user);
        return createAuthResponse(user);
    }

    /** Verify credentials via Spring's auth manager, then return a JWT. */
    public AuthenticationResponse login(LoginRequest request) {
        // Throws BadCredentialsException → GlobalExceptionHandler → 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return createAuthResponse(user);
    }

    /** Build the JWT with id/role/firstName/lastName claims so the frontend can render without an extra call. */
    private AuthenticationResponse createAuthResponse(User user) {
        org.springframework.security.core.userdetails.User userDetails =
            new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());

        return new AuthenticationResponse(jwtService.generateToken(extraClaims, userDetails));
    }
}
