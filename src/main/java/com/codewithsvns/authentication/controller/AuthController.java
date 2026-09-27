package com.codewithsvns.authentication.controller;

import com.codewithsvns.authentication.dto.LoginRequest;
import com.codewithsvns.authentication.dto.RegisterRequest;
import com.codewithsvns.authentication.dto.RegisterResponse;
import com.codewithsvns.authentication.dto.ResendOtpRequest;
import com.codewithsvns.authentication.dto.VerifyOtpRequest;
import com.codewithsvns.authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        RegisterResponse response = new RegisterResponse(
                "Registration successful. Please verify your email with the OTP."
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        authService.verifyOtp(request);

        return ResponseEntity.ok(
                "Email verified successfully."
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        authService.resendOtp(request);

        return ResponseEntity.ok(
                "A new OTP has been sent to your email."
        );
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        String tokens = authService.login(request);

        return ResponseEntity.ok(tokens);
    }
}