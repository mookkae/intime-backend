package com.intime.domain.member;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberCode implements BaseCode {

    MEMBER_CREATED("MEMBER_CREATED_201", HttpStatus.CREATED, "회원이 생성되었습니다."),
    MEMBER_FOUND("MEMBER_FOUND_200", HttpStatus.OK, "회원 조회에 성공했습니다."),
    MEMBER_UPDATED("MEMBER_UPDATED_200", HttpStatus.OK, "회원 정보가 수정되었습니다."),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND_404", HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
