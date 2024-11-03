package com.gamemoonchul.application.post;

import com.gamemoonchul.application.CommentService;
import com.gamemoonchul.application.converter.CommentConverter;
import com.gamemoonchul.application.converter.PostConverter;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.entity.redis.RedisPostDetail;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.repository.redis.RedisPostDetailRepository;
import com.gamemoonchul.infrastructure.web.common.Pagination;
import com.gamemoonchul.infrastructure.web.dto.response.CommentResponse;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class PostOpenApiService {
    private final PostRepository postRepository;
    private final CommentService commentService;

    public RedisPostDetail getPostDetails(Long postId, Long requestMemberId) {
        Post post = postRepository.searchByPostId(postId).orElseThrow(() -> new BadRequestException(PostStatus.POST_NOT_FOUND));
        post.viewCountUp();

        RedisPostDetail redisPostDetail = PostConverter.toCache(post);

        List<CommentResponse> comments = commentService.searchByPostId(redisPostDetail.getId(), requestMemberId).stream()
            .map(CommentConverter::toResponse).toList(); // 변경 자주 일어남, 캐싱 X
        redisPostDetail.setComments(comments);

        return redisPostDetail;
    }

    public Pagination<PostMainPageResponse> getLatestPosts(Long requestMemberId, int page, int size) {
        Page<Post> savedPage = postRepository.searchNewPostsWithoutBanPosts(requestMemberId, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = savedPage.getContent()
            .stream()
            .map(PostConverter::entityToResponse)
            .collect(Collectors.toList());

        return new Pagination<>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getGrillPosts(Long requestMemberId, int page, int size) {
        Page<Post> savedPage = postRepository.searchGrillPostsWithoutBanPosts(requestMemberId, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = savedPage.getContent()
            .stream()
            .map(PostConverter::entityToResponse)
            .toList();
        return new Pagination<PostMainPageResponse>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getHotPosts(int page, int size, Long requestMemberId) {
        Page<Post> savedPage = postRepository.searchHotPostWithoutBanPosts(requestMemberId, PageRequest.of(page, size));

        List<PostMainPageResponse> responses = savedPage.getContent()
            .stream()
            .map(PostConverter::entityToResponse)
            .toList();
        return new Pagination<>(savedPage, responses);
    }
}
