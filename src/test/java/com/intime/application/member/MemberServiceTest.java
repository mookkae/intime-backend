package com.intime.application.member;

import com.intime.common.exception.BusinessException;
import com.intime.domain.member.Member;
import com.intime.domain.member.MemberCode;
import com.intime.domain.member.MemberRepository;
import com.intime.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("signup 메서드")
    class Signup {

        @Test
        @DisplayName("성공 : 회원가입 시 '기다려' 접두사 닉네임이 생성된다")
        void signup() {
            // given
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Member result = memberService.signup("test@email.com", "password123");

            // then
            assertThat(result.getEmail()).isEqualTo("test@email.com");
            assertThat(result.getNickname()).startsWith("기다려");
        }
    }

    @Nested
    @DisplayName("getMember 메서드")
    class GetMember {

        @Test
        @DisplayName("성공 : 회원 조회")
        void getMember() {
            // given
            Member member = MemberFixture.createMember(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            Member result = memberService.getMember(1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 : 존재하지 않는 회원")
        void memberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getBaseCode())
                            .isEqualTo(MemberCode.MEMBER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateNickname 메서드")
    class UpdateNickname {

        @Test
        @DisplayName("성공 : 닉네임 변경")
        void updateNickname() {
            // given
            Member member = MemberFixture.createMember(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            Member result = memberService.updateNickname(1L, "김철수");

            // then
            assertThat(result.getNickname()).isEqualTo("김철수");
        }
    }
}
