package com.gamemoonchul.application;

import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.entity.Vote;
import com.gamemoonchul.domain.entity.VoteOptions;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.repository.VoteRepository;
import com.gamemoonchul.infrastructure.web.common.Pagination;
import com.gamemoonchul.infrastructure.web.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostOpenApiService {
    private final PostRepository postRepository;
    private final VoteRepository voteRepository;

    public Pagination<PostResponseDto> fetchByLatest(Pageable pageable) {
        Page<Post> savedPage = postRepository.findAllByOrderByCreatedAt(pageable);
        List<PostResponseDto> responses = savedPage.getContent()
                .stream()
                .map(post -> {
                            HashMap<MatchUser, Integer> vote = voteRepository.getVoteRateByPostId(post.getId());
                            return PostResponseDto.entityToResponse(post, vote);
                        }
                )
                .collect(Collectors.toList());

        return new Pagination<PostResponseDto>(savedPage, responses);
    }
}
