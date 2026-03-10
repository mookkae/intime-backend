package com.intime.application.member;

import com.intime.domain.member.Member;

public interface MemberService {

    Member signup(String email, String password);

    Member getMember(Long memberId);

    Member updateNickname(Long memberId, String nickname);
}
