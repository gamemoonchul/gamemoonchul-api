package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.common.exception.ApiException;
import com.gamemoonchul.domain.entity.*;
import com.gamemoonchul.domain.status.PostStatus;
import com.gamemoonchul.infrastructure.repository.CommentRepository;
import com.gamemoonchul.infrastructure.repository.MemberRepository;
import com.gamemoonchul.infrastructure.repository.PostRepository;
import com.gamemoonchul.infrastructure.web.dto.CommentFixRequest;
import com.gamemoonchul.infrastructure.web.dto.CommentRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CommentServiceTest extends TestDataBase {
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

    Post post;
    Member member;

    @BeforeEach
    void setUp() {
        post = PostDummy.createPost("Hi");
        post = postRepository.save(post);
        member = MemberDummy.create();
        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("코멘트 저장이 잘 되는지, post의 comment 개수가 정상적으로 업데이트 되는지 테스트")
    void saveTest() {
        // given
        CommentRequest request = CommentDummy.createRequest(post.getId());
        em.clear(); // 1차 캐쉬 제거

        // when
        Comment savedEntity = commentService.save(request, member);
        Post searchedPost = postRepository.findById(post.getId())
                .orElseThrow(
                        () -> new ApiException(PostStatus.POST_NOT_FOUND)
                );

        // then
        assertThat(searchedPost.getCommentCount()).isEqualTo(post.getCommentCount() + 1);
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
        Comment fixedComment = commentService.fix(content, member);

        // then
        assertThat(savedEntity.getContent()).isNotEqualTo(fixedComment.getContent());
        assertThat(savedEntity.getId()).isEqualTo(fixedComment.getId());
    }
}