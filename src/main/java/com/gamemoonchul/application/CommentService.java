package com.gamemoonchul.application;

import com.gamemoonchul.application.converter.CommentConverter;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.common.exception.NotFoundException;
import com.gamemoonchul.common.exception.UnauthorizedException;
import com.gamemoonchul.domain.entity.Comment;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.model.dto.CommentSaveDto;
import com.gamemoonchul.domain.status.MemberStatus;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.request.CommentFixRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentConverter commentConverter;

    public List<Comment> searchByPostId(Long postId, Member member) {
        return commentRepository.searchByPostId(postId, Optional.ofNullable(member)
                .map(Member::getId)
                .orElse(null));
    }

    /**
     * 테스트를 하기 위해서 실제 Post, Member가 객체맵핑 되있어야 합니다.
     */
    public Comment save(CommentSaveDto request, Member member) {
        validatePostNotReplied(request);
        Comment comment = commentConverter.requestToEntity(member, request);
        Post post = postRepository.findById(request.postId())
                .orElseThrow(
                        () -> new NotFoundException(PostStatus.POST_NOT_FOUND)
                );
        post.commentCountUp();
        postRepository.save(post);

        return commentRepository.save(comment);
    }

    /**
     * 부모의 Comment가 또 부모를 가지지 않는지 검증
     */
    private void validatePostNotReplied(CommentSaveDto request) {
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(
                            () -> new BadRequestException(PostStatus.COMMENT_NOT_FOUND)
                    );
            if (parentComment.getParentId() != null) {
                throw new BadRequestException(PostStatus.COMMENT_CANT_HAVE_GRANDMOTHER);
            } else if (!parentComment.getPost()
                    .getId()
                    .equals(request.postId())) {
                throw new BadRequestException(PostStatus.INVALID_REPLY);
            }
        }
    }

    public Comment searchComment(Long commentId) {
        Comment result = commentRepository.searchByIdOrThrow(commentId);
        return result;
    }

    public Comment fix(CommentFixRequest request, Member authMember) {
        Comment fixedComment = commentConverter.requestToEntity(request);
        // 글 작성자
        validateSameMemberId(fixedComment.getMember(), authMember);
        return commentRepository.save(fixedComment);
    }

    public void delete(Long commentId, Member authMember) {
        Comment savedComment = this.searchComment(commentId);
        validateSameMemberId(savedComment.getMember(), authMember);

        if (!savedComment.parentExist()) { // 대댓글이 아닐경우 자기 자신의 대댓글들 삭제
            List<Comment> children = commentRepository.findByParentId(savedComment.getId());
            commentRepository.deleteAll(children);
        }
        commentCountDown(savedComment.getPost());
        commentRepository.delete(savedComment);
    }

    private void commentCountDown(Post post) {
        post.commentCountDown();
        postRepository.save(post);
    }

    private void validateSameMemberId(Member commentWriteMember, Member currentSignInMember) {
        if (commentWriteMember.getId()
                .equals(currentSignInMember.getId())) {
            return;
        }
        throw new UnauthorizedException(MemberStatus.NOT_AUTHORIZED_MEMBER);
    }

}
