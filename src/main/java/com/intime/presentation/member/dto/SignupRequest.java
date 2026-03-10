package com.intime.presentation.member.dto;

public record SignupRequest(
        String email,
        String password
) {
}
