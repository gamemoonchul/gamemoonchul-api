package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.domain.entity.*;
import com.gamemoonchul.infrastructure.repository.MemberRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PostBanServiceTest extends TestDataBase {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostBanService postBanService;

    @Test
    @DisplayName("게시글 차단 테스트")
    void ban() {
        // given
        Member member1 = memberRepository.save(MemberDummy.createWithId("hi"));
        Member member2 = memberRepository.save(MemberDummy.createWithId("bye"));
        Post post = postRepository.save(PostDummy.createPostWithSpecificMember(member1));

        // when
        postBanService.ban(member2, post.getId());

        // then
        List<PostMainPageResponse> postBans = postBanService.bannedPostList(member2.getId());
        assertEquals(1, postBans.size());
        assertThat(postBans.get(0).title()).isEqualTo(post.getTitle());
    }
}
