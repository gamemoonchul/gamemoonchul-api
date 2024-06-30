package com.gamemoonchul.application;

import com.gamemoonchul.application.converter.PostConverter;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.repository.VoteOptionRepository;
import com.gamemoonchul.infrastructure.web.common.Pagination;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import com.gamemoonchul.infrastructure.web.dto.response.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostOpenApiService {
    private final PostRepository postRepository;

    public Pagination<PostMainPageResponse> getLatestPosts(int page, int size) {
        Page<Post> savedPage = postRepository.findAllByOrderByCreatedAt(PageRequest.of(page, size));
        List<PostMainPageResponse> responses = savedPage.getContent()
                .stream()
                .map(PostConverter::toMainResponse
                )
                .collect(Collectors.toList());

        return new Pagination<>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getGrillPosts(int page, int size) {
        Double standardRatio = 45.0;
        Page<Post> savedPage = postRepository.findByVoteRatioGreaterThanEqual(standardRatio, PageRequest.of(page, size));
        List<PostMainPageResponse> responses = savedPage
                .getContent()
                .stream()
                .map(PostConverter::toMainResponse)
                .toList();
        return new Pagination<PostMainPageResponse>(savedPage, responses);
    }

    public Pagination<PostMainPageResponse> getHotPosts(int page, int size) {
        Page<Post> savedPage = postRepository.findAllByOrderByViewCountDesc(PageRequest.of(page, size));
        List<PostMainPageResponse> responses = savedPage
                .getContent()
                .stream()
                .map(PostConverter::toMainResponse)
                .toList();
        return new Pagination<>(savedPage, responses);
    }
}
