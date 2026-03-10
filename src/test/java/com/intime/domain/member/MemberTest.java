package com.intime.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 엔티티 단위 테스트")
class MemberTest {

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 이메일, 비밀번호, 닉네임으로 회원 생성")
        void createMember() {
            Member member = Member.create("test@email.com", "password123", "홍길동");

            assertThat(member.getEmail()).isEqualTo("test@email.com");
            assertThat(member.getPassword()).isEqualTo("password123");
            assertThat(member.getNickname()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("updateNickname 메서드")
    class UpdateNickname {

        @Test
        @DisplayName("성공 : 닉네임 변경")
        void updateNickname() {
            Member member = Member.create("test@email.com", "password123", "홍길동");

            member.updateNickname("김철수");

            assertThat(member.getNickname()).isEqualTo("김철수");
        }
    }
}
