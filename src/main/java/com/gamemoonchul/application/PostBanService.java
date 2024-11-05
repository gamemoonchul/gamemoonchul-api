package com.gamemoonchul.application;

import com.gamemoonchul.application.converter.PostConverter;
import com.gamemoonchul.application.post.PostService;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.entity.PostBan;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.PostBanRepository;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@RequiredArgsConstructor
public class PostBanService {
    private final PostBanRepository postBanRepository;
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public void ban(Member member, Long postId) {
        Post post = postService.findById(postId);
        PostBan postBan = PostBan.builder()
            .banPost(post)
            .member(member)
            .build();
        postBanRepository.save(postBan);
    }

    public List<PostMainPageResponse> bannedPostList(Long id) {
        List<CompletableFuture<PostMainPageResponse>> futures = postBanRepository.searchByMemberId(id)
            .stream()
            .map(PostBan::getBanPost)
            .map(post -> CompletableFuture.supplyAsync(() -> {
                Integer commentCount = commentRepository.countByPostId(post.getId());
                return PostConverter.entityToResponse(post, commentCount);
            }, executor))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join) // 각 비동기 작업의 결과를 기다림
            .toList();
    }
}
