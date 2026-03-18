package com.intime.presentation.member;

import com.intime.application.member.MemberService;
import com.intime.application.member.dto.MemberInfo;
import com.intime.application.member.dto.MemberSignupCommand;
import com.intime.presentation.member.api.MemberApi;
import com.intime.presentation.member.dto.NicknameUpdateRequest;
import com.intime.presentation.member.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<MemberInfo> signup(@RequestBody SignupRequest request) {
        MemberSignupCommand command = new MemberSignupCommand(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signup(command));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberInfo> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.getMember(memberId));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberInfo> updateNickname(
            @PathVariable Long memberId,
            @RequestBody NicknameUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateNickname(memberId, request.nickname()));
    }
}
