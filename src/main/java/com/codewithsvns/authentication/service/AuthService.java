package com.codewithsvns.authentication.service;

import com.codewithsvns.authentication.dto.LoginRequest;
import com.codewithsvns.authentication.dto.RegisterRequest;
import com.codewithsvns.authentication.dto.ResendOtpRequest;
import com.codewithsvns.authentication.dto.VerifyOtpRequest;
import com.codewithsvns.authentication.entity.User;
import com.codewithsvns.authentication.exception.BadRequestException;
import com.codewithsvns.authentication.exception.ConflictException;
import com.codewithsvns.authentication.exception.NotFoundException;
import com.codewithsvns.authentication.exception.TooManyRequestsException;
import com.codewithsvns.authentication.repository.UserRepository;
import com.codewithsvns.authentication.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.codewithsvns.authentication.dto.LoginResponse;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       EmailService emailService,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        String otp = otpService.generateOtp();

        String otpHash = passwordEncoder.encode(otp);
        user.setOtpHash(otpHash);

        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpResendCount(0);
        user.setVerified(false);

        emailService.sendOtp(user.getEmail(), otp);

        return userRepository.save(user);
    }

    public void verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified");
        }

        if (user.getOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getOtpExpiry())) {

            throw new BadRequestException("OTP has expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), user.getOtpHash())) {
            throw new BadRequestException("Invalid OTP");
        }

        user.setVerified(true);

        // OTP can no longer be reused
        user.setOtpHash(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }

    public void resendOtp(ResendOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified");
        }

        if (user.getOtpResendCount() >= 3) {
            throw new TooManyRequestsException(
                    "Maximum OTP resend attempts reached"
            );
        }

        String otp = otpService.generateOtp();

        String otpHash = passwordEncoder.encode(otp);

        user.setOtpHash(otpHash);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        user.setOtpResendCount(user.getOtpResendCount() + 1);

        userRepository.save(user);

        emailService.sendOtp(user.getEmail(), otp);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new BadRequestException(
                    "Please verify your email before logging in"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user.getEmail());

        String refreshToken =
                jwtService.generateRefreshToken(user.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    public String refreshAccessToken(String refreshToken) {

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BadRequestException(
                    "Invalid or expired refresh token"
            );
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException(
                    "Invalid refresh token"
            );
        }

        String email = jwtService.extractEmail(refreshToken);

        return jwtService.generateAccessToken(email);
    }
}
