package com.gamemoonchul.domain.converter;

import com.gamemoonchul.config.apple.entities.AppleUserInfo;
import com.gamemoonchul.config.oauth.user.OAuth2Provider;
import com.gamemoonchul.config.oauth.user.OAuth2UserInfo;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.enums.MemberRole;

import java.util.Optional;
import java.util.UUID;

public class MemberConverter {
    public static Member toEntity(OAuth2UserInfo userInfo) {
        Optional<String> nickname = Optional.ofNullable(userInfo
                .getNickname());
        if (nickname.isEmpty()) {
            nickname = Optional.ofNullable(UUID.randomUUID().toString());
        }
        Member member = Member.builder()
                .role(MemberRole.USER)
                .name(userInfo.getName())
                .identifier(userInfo.getIdentifier())
                .provider(userInfo.getProvider())
                .nickname(
                        nickname.get()
                )
                .score(0.0)
                .email(userInfo
                        .getEmail())
                .picture(userInfo
                        .getProfileImageUrl())
                .birth(null)
                .build();
        return member;
    }

    public static Member toEntity(AppleUserInfo userInfo) {
        Member member = Member.builder()
                .role(MemberRole.USER)
                .name(userInfo.getName())
                .identifier(userInfo.getSub()).provider(OAuth2Provider.APPLE)
                .nickname(UUID.randomUUID().toString())
                .score(0.0)
                .email(userInfo.getEmail())
                .picture(null)
                .birth(null)
                .build();
        return member;
    }
}
