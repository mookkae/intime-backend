package com.intime.presentation.member;

import com.intime.application.member.MemberService;
import com.intime.common.ApiResponse;
import com.intime.domain.member.Member;
import com.intime.domain.member.MemberCode;
import com.intime.presentation.member.dto.MemberResponse;
import com.intime.presentation.member.dto.NicknameUpdateRequest;
import com.intime.presentation.member.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@RequestBody SignupRequest request) {
        Member member = memberService.signup(request.email(), request.password());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(MemberCode.MEMBER_CREATED, MemberResponse.from(member)));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(@PathVariable Long memberId) {
        Member member = memberService.getMember(memberId);
        return ResponseEntity.ok(ApiResponse.of(MemberCode.MEMBER_FOUND, MemberResponse.from(member)));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateNickname(
            @PathVariable Long memberId,
            @RequestBody NicknameUpdateRequest request
    ) {
        Member member = memberService.updateNickname(memberId, request.nickname());
        return ResponseEntity.ok(ApiResponse.of(MemberCode.MEMBER_UPDATED, MemberResponse.from(member)));
    }
}
