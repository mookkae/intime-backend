package com.intime.presentation.member.api;

import com.intime.presentation.member.dto.MemberResponse;
import com.intime.presentation.member.dto.NicknameUpdateRequest;
import com.intime.presentation.member.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 관련 API")
@RequestMapping("/api/v1/members")
public interface MemberApi {

    @Operation(summary = "회원가입")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @PostMapping("/signup")
    ResponseEntity<MemberResponse> signup(@RequestBody SignupRequest request);

    @Operation(summary = "회원 조회")
    @ApiResponse(responseCode = "200", description = "회원 조회 성공")
    @GetMapping("/{memberId}")
    ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId);

    @Operation(summary = "닉네임 수정")
    @ApiResponse(responseCode = "200", description = "닉네임 수정 성공")
    @PatchMapping("/{memberId}")
    ResponseEntity<MemberResponse> updateNickname(
            @PathVariable Long memberId,
            @RequestBody NicknameUpdateRequest request
    );
}
