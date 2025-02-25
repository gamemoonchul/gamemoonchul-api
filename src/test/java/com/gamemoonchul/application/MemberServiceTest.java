package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.application.member.MemberService;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.MemberDummy;
import com.gamemoonchul.domain.status.MemberStatus;
import com.gamemoonchul.infrastructure.repository.MemberRepository;
import com.gamemoonchul.infrastructure.web.dto.request.NicknameChangeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


class MemberServiceTest extends TestDataBase {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;


    @Test
    @DisplayName("닉네임 변경이 정상적으로 되는지 테스트")
    void updateNickName() {
        // given
        Member member = MemberDummy.create();
        memberRepository.save(member);
        NicknameChangeRequest request = new NicknameChangeRequest("우하하");

        // when
        memberService.updateNickName(member, request);
        Optional<Member> savedMember = memberRepository.findByNickname(request.nickname());

        // then
        assertThat(savedMember.get()
            .getNickname()).isEqualTo(request.nickname());
    }

    @Test
    @DisplayName("동일한 닉네임으로 두 번 저장하면 예외 발생하는지 테스트")
    void alreadyExistNickName() {
        // given
        Member member = MemberDummy.create();
        memberRepository.save(member);
        NicknameChangeRequest request = new NicknameChangeRequest("우하하");

        // when
        memberService.updateNickName(member, request);

        // then
        assertThatThrownBy(() -> memberService.updateNickName(member, request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(MemberStatus.ALREADY_EXIST_NICKNAME.getMessage());
    }
}
