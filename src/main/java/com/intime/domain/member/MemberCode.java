package com.intime.domain.member;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberCode implements BaseCode {

    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEMBER_EMAIL_DUPLICATE("MEMBER_EMAIL_DUPLICATE", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
