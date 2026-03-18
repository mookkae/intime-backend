package com.intime.application.member;

import com.intime.application.member.dto.MemberInfo;
import com.intime.application.member.dto.MemberSignupCommand;
import com.intime.common.exception.BusinessException;
import com.intime.domain.member.Member;
import com.intime.domain.member.MemberCode;
import com.intime.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public MemberInfo signup(MemberSignupCommand command) {
        if (memberRepository.existsByEmail(command.email())) {
            throw new BusinessException(MemberCode.MEMBER_EMAIL_DUPLICATE);
        }
        String nickname = generateNickname();
        Member member = memberRepository.save(Member.create(command.email(), command.password(), nickname));
        return MemberInfo.from(member);
    }

    @Override
    public MemberInfo getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    @Transactional
    public MemberInfo updateNickname(Long memberId, String nickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberCode.MEMBER_NOT_FOUND));
        member.updateNickname(nickname);
        return MemberInfo.from(member);
    }

    private String generateNickname() {
        int number = ThreadLocalRandom.current().nextInt(1, 100000);
        return "기다려" + number;
    }
}
