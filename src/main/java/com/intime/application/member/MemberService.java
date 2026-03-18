package com.intime.application.member;

import com.intime.application.member.dto.MemberInfo;
import com.intime.application.member.dto.MemberSignupCommand;

public interface MemberService {

    MemberInfo signup(MemberSignupCommand command);

    MemberInfo getMember(Long memberId);

    MemberInfo updateNickname(Long memberId, String nickname);
}
