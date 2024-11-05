package com.gamemoonchul.infrastructure.web;

import com.gamemoonchul.application.PostBanService;
import com.gamemoonchul.application.converter.PostConverter;
import com.gamemoonchul.common.annotation.MemberSession;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.PostBan;
import com.gamemoonchul.infrastructure.web.common.RestControllerWithEnvelopPattern;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/post-bans")
@RestControllerWithEnvelopPattern
public class PostBanApiController {
    private final PostBanService postBanService;

    @GetMapping("/ban")
    public List<PostMainPageResponse> getBannedPosts(
        @MemberSession Member member
    ) {
        return postBanService.bannedPostList(member.getId());
    }

    @PostMapping("/ban/{postId}")
    public void ban(
        @MemberSession Member member,
        @PathVariable(name = "postId") Long postId
    ) {
        postBanService.ban(member, postId);
    }
}
