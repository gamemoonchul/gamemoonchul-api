package com.gamemoonchul.application.post;

import com.gamemoonchul.application.converter.CommentConverter;
import com.gamemoonchul.application.converter.PostConverter;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.entity.redis.RedisPostDetail;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.common.Pagination;
import com.gamemoonchul.infrastructure.web.dto.response.CommentResponse;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class PostOpenApiService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public RedisPostDetail getPostDetails(Long postId, Long requestMemberId) {
        Post post = postRepository.searchByPostId(postId).orElseThrow(() -> new BadRequestException(PostStatus.POST_NOT_FOUND));

        // DTO 대신 사용하고 있는데 이름 바꾸기 귀찮음
        RedisPostDetail redisPostDetail = PostConverter.toCache(post, (long) post.getComments().size());

        return redisPostDetail;
    }

    public Pagination<PostMainPageResponse> getLatestPosts(Long requestMemberId, int page, int size) {
        Page<Post> savedPage = postRepository.searchNewPostsWithoutBanPosts(requestMemberId, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = getPostMainPageResponses(savedPage);
        return new Pagination<>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getGrillPosts(Long requestMemberId, int page, int size) {
        Page<Post> savedPage = postRepository.searchGrillPostsWithoutBanPosts(requestMemberId, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = getPostMainPageResponses(savedPage);
        return new Pagination<PostMainPageResponse>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getHotPosts(int page, int size, Long requestMemberId) {
        Page<Post> savedPage = postRepository.searchHotPostWithoutBanPosts(requestMemberId, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = getPostMainPageResponses(savedPage);
        return new Pagination<>(savedPage, responses);
    }

    private List<PostMainPageResponse> getPostMainPageResponses(Page<Post> savedPage) {
        List<CompletableFuture<PostMainPageResponse>> futures = savedPage.getContent()
            .stream()
            .map(post -> CompletableFuture.supplyAsync(() -> convertResponseDtoWithRepository(post), executor))
            .toList();

        List<PostMainPageResponse> responses = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return responses;
    }

    private PostMainPageResponse convertResponseDtoWithRepository(Post post) {
        Integer commentCount = commentRepository.countByPostId(post.getId());
        return PostConverter.entityToResponse(post, commentCount);
    }
}
