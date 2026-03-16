package com.intime.support.fixture;

import com.intime.domain.member.Member;
import com.intime.support.TestReflectionUtils;

public class MemberFixture {

    public static Member createMember(Long memberId) {
        return createMember(memberId, "test@email.com", "password123", "테스트유저");
    }

    public static Member createMember(Long memberId, String email, String password, String nickname) {
        Member member = Member.create(email, password, nickname);
        if (memberId != null) {
            TestReflectionUtils.setField(member, "id", memberId);
        }
        return member;
    }
}
