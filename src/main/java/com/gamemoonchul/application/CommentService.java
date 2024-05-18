package com.gamemoonchul.application;

import com.gamemoonchul.application.converter.CommentConverter;
import com.gamemoonchul.common.exception.ApiException;
import com.gamemoonchul.domain.entity.Comment;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.CommentRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentConverter commentConverter;

    /**
     * 테스트를 하기 위해서 실제 Post, Member가 객체맵핑 되있어야 합니다.
     */
    public Comment save(CommentRequest request, Member member) {
        Comment comment = commentConverter.requestToEntity(member, request);
        Post post = postRepository.findById(request.postId())
                .orElseThrow(
                        () -> new ApiException(PostStatus.POST_NOT_FOUND)
                );
        post.addComment(comment);
        postRepository.save(post);
        return commentRepository.save(comment);
    }
}
