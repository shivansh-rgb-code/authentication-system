package com.codewithsvns.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private boolean isVerified;

    private String otpHash;
    private LocalDateTime otpExpiry;
    private int otpResendCount;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public int getOtpResendCount() {
        return otpResendCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public void setOtpResendCount(int otpResendCount) {
        this.otpResendCount = otpResendCount;
    }
}
