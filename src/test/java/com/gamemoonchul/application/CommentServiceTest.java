package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.application.post.PostOpenApiService;
import com.gamemoonchul.application.post.PostService;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.common.exception.NotFoundException;
import com.gamemoonchul.common.exception.UnauthorizedException;
import com.gamemoonchul.domain.entity.*;
import com.gamemoonchul.domain.entity.redis.RedisPostDetail;
import com.gamemoonchul.infrastructure.web.dto.request.CommentSaveRequest;
import com.gamemoonchul.domain.status.MemberStatus;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.MemberRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.request.CommentFixRequest;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CommentServiceTest extends TestDataBase {
    Post post;
    Member member;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        post = PostDummy.createPost("Hi");
        post = postRepository.save(post);
        member = MemberDummy.create();
        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("코트 저장이 잘 되는지, post의 comment 개수가 정상적으로 업데이트 되는지 테스트")
    void saveTest() {
        // given
        CommentSaveRequest request = CommentDummy.createSaveDto(post.getId());
        em.clear(); // 1차 캐쉬 제거

        // when
        Comment savedEntity = commentService.save(request, member);
        Integer savedCommentCount = commentRepository.countByPostId(post.getId());

        // then
        assertThat(savedCommentCount).isEqualTo(post.getCommentCount() + 1);
        assertThat(savedEntity.getContent()).isEqualTo(request.content());
        assertThat(savedEntity.getPost()
            .getId()).isEqualTo(request.postId());
    }

    @Test
    @DisplayName("코멘트 저장 후 수정시 ID는 그대로 인데 contents 내용이 수정이 되는지 테스트")
    void fixTest() throws InterruptedException {
        // given
        Comment savedEntity = commentRepository.save(CommentDummy.create(post, member));
        CommentFixRequest content = CommentFixRequest.builder()
            .contents("new contents")
            .commentId(savedEntity.getId())
            .build();
        em.clear();

        // when
        Comment fixedComment = commentService.fix(content, member.getId());

        // then
        assertThat(savedEntity.getContent()).isNotEqualTo(fixedComment.getContent());
        assertThat(savedEntity.getId()).isEqualTo(fixedComment.getId());
    }

    @Test
    @DisplayName("댓글을 작성한 유저가 아닌 경우에 댓글 수정 불가능")
    void canNotFixWithNotAuthorizedMember() throws InterruptedException {
        // given
        Comment savedEntity = commentRepository.save(CommentDummy.create(post, member));
        Member anotherMember = memberRepository.save(MemberDummy.createWithId("rookedsysc"));
        CommentFixRequest content = CommentFixRequest.builder()
            .contents("new contents")
            .commentId(savedEntity.getId())
            .build();
        em.clear();

        // when // then
        assertThatThrownBy(
            () -> commentService.fix(content, anotherMember.getId()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining(MemberStatus.NOT_AUTHORIZED_MEMBER.getMessage());
    }

    @Test
    @DisplayName("저장 안되어있는 Comment 수정할려고 하면 에러 발생하는지 테스트")
    void canNotFixNotExistComment() throws InterruptedException {
        // given
        CommentFixRequest content = CommentFixRequest.builder()
            .contents("new contents")
            .commentId(12L)
            .build();

        // when // then
        assertThatThrownBy(
            () -> commentService.fix(content, member.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(PostStatus.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delTest() throws InterruptedException {
        // given
        CommentSaveRequest request = CommentDummy.createSaveDto(post.getId());
        Comment savedComment = commentService.save(request, member);

        // when
        commentService.delete(savedComment.getId(), member.getId());

        // then
        assertThatThrownBy(
            () -> commentService.searchComment(savedComment.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(PostStatus.COMMENT_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("부모 댓글 삭제시 대댓글도 삭제 되는지 테스트")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void replyDeleteTest() throws InterruptedException {
        // given
        CommentSaveRequest request = CommentDummy.createSaveDto(post.getId());
        Comment savedComment = commentService.save(request, member);
        CommentSaveRequest replyRequest = CommentDummy.createSaveDto(savedComment.getId(), post.getId());
        Comment savedReply = commentService.save(replyRequest, member);

        // when
        commentService.delete(savedComment.getId(), member.getId());

        // then
        assertThatThrownBy(
            () -> commentService.searchComment(savedReply.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(PostStatus.COMMENT_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("댓글 삭제시 Comment Count가 감소하는지 테스트")
    void shouldDecreaseCommentCountWhenCommentIsDeleted() {
        // given
        CommentSaveRequest request = CommentDummy.createSaveDto(post.getId());
        Comment savedComment = commentService.save(request, member);
        Integer originCount = commentRepository.countByPostId(post.getId());

        // when
        commentService.delete(savedComment.getId(), member.getId());
        em.clear();
        Integer currentCommentCount = commentRepository.countByPostId(post.getId());

        // then
        assertThat(currentCommentCount).isEqualTo(originCount - 1);
    }

    @Test
    @DisplayName("댓글을 작성한 유저가 아닌 경우에 댓글 삭제 불가능")
    void canNotDelWithNotAuthorizedMember() throws InterruptedException {
        // given
        Comment savedEntity = commentRepository.save(CommentDummy.create(post, member));
        Member anotherMember = memberRepository.save(MemberDummy.createWithId("rookedsysc"));
        CommentFixRequest content = CommentFixRequest.builder()
            .contents("new contents")
            .commentId(savedEntity.getId())
            .build();
        em.clear();

        // when // then
        assertThatThrownBy(
            () -> commentService.delete(content.commentId(), anotherMember.getId()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining(MemberStatus.NOT_AUTHORIZED_MEMBER.getMessage());
    }

    @Test
    @DisplayName("저장 안되어있는 Comment 삭제할려고 하면 에러 발생하는지 테스트")
    void canNotDelNotExistComment() throws InterruptedException {
        // given
        CommentFixRequest content = CommentFixRequest.builder()
            .contents("new contents")
            .commentId(12L)
            .build();

        // when // then
        assertThatThrownBy(
            () -> commentService.delete(content.commentId(), member.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(PostStatus.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("대댓글 저장 테스트")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void replyCommentTest() {
        // given
        CommentSaveRequest parentComment = CommentDummy.createSaveDto(post.getId());
        Comment savedParentComment = commentService.save(parentComment, member);
        CommentSaveRequest request = CommentDummy.createSaveDto(savedParentComment.getId(), post.getId());

        // when
        Comment savedChildComment = commentService.save(request, member);

        // then
        assertThat(savedChildComment.getContent()).isEqualTo(request.content());
        assertThat(savedChildComment.getParentId()).isEqualTo(savedParentComment.getId());
    }

    @Test
    @DisplayName("다른 Post ID를 가진 대댓글이 작성되지 않는지 테스트")
    void replyAnotherPostExceptionTest() {
        // given
        Post anotherPost = postRepository.save(PostDummy.createPost("byebye"));
        CommentSaveRequest parentComment = CommentDummy.createSaveDto(post.getId());
        Comment savedParentComment = commentService.save(parentComment, member);
        CommentSaveRequest request = CommentDummy.createSaveDto(savedParentComment.getId(), anotherPost.getId());

        // when // then
        assertThatThrownBy(
            () -> commentService.save(request, member))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(PostStatus.INVALID_REPLY.getMessage());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("대댓글에 대댓글을 다는 행위가 허용되지 않는지 테스트")
    void replyingToReplyIsNotAllowed() {
        // given
        CommentSaveRequest parentComment = CommentDummy.createSaveDto(post.getId());
        Comment savedParrentComment = commentService.save(parentComment, member);
        CommentSaveRequest childComment = CommentDummy.createSaveDto(savedParrentComment.getId(), post.getId());
        Comment savedChildComment = commentService.save(childComment, member);
        CommentSaveRequest wrongComment = CommentDummy.createSaveDto(savedChildComment.getId(), post.getId());
        // when // then
        assertThatThrownBy(
            () -> commentService.save(wrongComment, member))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(PostStatus.COMMENT_CANT_HAVE_GRANDMOTHER.getMessage());
    }
}
