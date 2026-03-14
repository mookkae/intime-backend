package com.intime.application.member;

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
    public Member signup(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(MemberCode.MEMBER_EMAIL_DUPLICATE);
        }
        String nickname = generateNickname();
        return memberRepository.save(Member.create(email, password, nickname));
    }

    @Override
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberCode.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public Member updateNickname(Long memberId, String nickname) {
        Member member = getMember(memberId);
        member.updateNickname(nickname);
        return member;
    }

    private String generateNickname() {
        int number = ThreadLocalRandom.current().nextInt(1, 100000);
        return "기다려" + number;
    }
}
