package com.intime.application.member.dto;

public record MemberSignupCommand(
        String email,
        String password
) {
}
