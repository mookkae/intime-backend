package com.intime.application.member.dto;

import com.intime.domain.member.Member;

import java.time.LocalDateTime;

public record MemberInfo(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {

    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getCreatedAt()
        );
    }
}
