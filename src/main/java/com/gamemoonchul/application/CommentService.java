package com.gamemoonchul.application;

import com.gamemoonchul.common.constants.RedisKeyConstant;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.common.exception.NotFoundException;
import com.gamemoonchul.common.exception.UnauthorizedException;
import com.gamemoonchul.domain.entity.Comment;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.infrastructure.web.dto.request.CommentSaveRequest;
import com.gamemoonchul.domain.status.MemberStatus;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.request.CommentFixRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, Integer> redisTemplate;

    public List<Comment> searchByPostId(Long postId, Long requestMemberId) {
        return commentRepository.searchByPostId(postId, requestMemberId);
    }

    /**
     * 테스트를 하기 위해서 실제 Post, Member가 객체맵핑 되있어야 합니다.
     */
    public Comment save(CommentSaveRequest request, Member member) {
        validatePostNotReplied(request);
        Post post = postRepository.findById(request.postId())
            .orElseThrow(() -> new NotFoundException(PostStatus.POST_NOT_FOUND));
        Comment comment = Comment.builder()
            .parentId(request.parentId())
            .member(member)
            .post(post)
            .content(request.content())
            .build();

        String redisKey = RedisKeyConstant.postCountKey(post.getId());
        setIfAbsentCount(redisKey, post.getId());
        redisTemplate.opsForValue().increment(redisKey);

        postRepository.save(post);

        return commentRepository.save(comment);
    }

    /**
     * 부모의 Comment가 또 부모를 가지지 않는지 검증
     */
    private void validatePostNotReplied(CommentSaveRequest request) {
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                .orElseThrow(() -> new BadRequestException(PostStatus.COMMENT_NOT_FOUND));
            if (parentComment.getParentId() != null) {
                throw new BadRequestException(PostStatus.COMMENT_CANT_HAVE_GRANDMOTHER);
            } else if (!parentComment.getPost()
                .getId()
                .equals(request.postId())) {
                throw new BadRequestException(PostStatus.INVALID_REPLY);
            }
        }
    }

    public Comment fix(CommentFixRequest request, Long requestMemberId) {
        Comment modifiedComment = commentRepository.findById(request.commentId())
            .orElseThrow(() -> new NotFoundException(PostStatus.COMMENT_NOT_FOUND));
        modifiedComment.setContent(request.contents());
        // 글 작성자
        validateSameMemberId(modifiedComment.getMember(), requestMemberId);
        return commentRepository.save(modifiedComment);
    }

    public void delete(Long commentId, Long requestMemberId) {
        Comment savedComment = this.searchComment(commentId);
        Long postId = savedComment.getPost().getId();
        String redisKey = RedisKeyConstant.postCountKey(savedComment.getPost().getId());

        validateSameMemberId(savedComment.getMember(), requestMemberId);
        setIfAbsentCount(redisKey, postId);
        deleteChildComments(savedComment, redisKey);

        redisTemplate.opsForValue().decrement(redisKey);
        commentRepository.delete(savedComment);
    }

    private void deleteChildComments(Comment savedComment, String redisKey) {
        if (!savedComment.parentExist()) { // 대댓글이 아닐경우 자기 자신의 대댓글들 삭제
            List<Comment> children = commentRepository.findByParentId(savedComment.getId());
            commentRepository.deleteAll(children);
            redisTemplate.opsForValue().decrement(redisKey, children.size());
        }
    }

    private void setIfAbsentCount(String redisKey, Long postId) {
        // 해당 키에 대한 캐시가 없다면
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) { // equals는 Nullsafe하게 비교 가능
            Integer count = commentRepository.countByPostId(postId);
            redisTemplate.opsForValue().setIfAbsent(redisKey, count, Duration.ofMinutes(30));
        }
    }

    /**
     * 테스트 용이성을 위해서 분리
     */
    public Comment searchComment(Long commentId) {
        Comment comment = commentRepository.findByIdForUpdate(commentId)
            .orElseThrow(() -> new NotFoundException(PostStatus.COMMENT_NOT_FOUND));
        return comment;
    }

    private void validateSameMemberId(Member commentWriteMember, Long requestMemberId) {
        if (commentWriteMember.getId().equals(requestMemberId)) {
            return;
        }
        throw new UnauthorizedException(MemberStatus.NOT_AUTHORIZED_MEMBER);
    }

}
