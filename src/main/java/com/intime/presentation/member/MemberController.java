package com.intime.presentation.member;

import com.intime.application.member.MemberService;
import com.intime.domain.member.Member;
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
    public ResponseEntity<MemberResponse> signup(@RequestBody SignupRequest request) {
        Member member = memberService.signup(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(MemberResponse.from(memberService.getMember(memberId)));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateNickname(
            @PathVariable Long memberId,
            @RequestBody NicknameUpdateRequest request
    ) {
        return ResponseEntity.ok(MemberResponse.from(memberService.updateNickname(memberId, request.nickname())));
    }
}
