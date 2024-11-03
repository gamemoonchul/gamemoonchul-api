package com.gamemoonchul.infrastructure.repository.impl;

import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.infrastructure.repository.ifs.PostRepositoryIfs;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.gamemoonchul.domain.entity.QMember.member;
import static com.gamemoonchul.domain.entity.QMemberBan.memberBan;
import static com.gamemoonchul.domain.entity.QPost.post;
import static com.gamemoonchul.domain.entity.QPostBan.postBan;
import static com.gamemoonchul.domain.entity.QVoteOptions.voteOptions;
import static com.gamemoonchul.domain.entity.riot.QMatchUser.matchUser;

@Repository
/*
 * QueryDSL의 Repository 네이밍 규칙을 JPARepository에 Impl을 붙이는 방식으로 구현해야 합니다.
 */
public class PostRepositoryImpl implements PostRepositoryIfs {
    JPAQueryFactory queryFactory;

    public PostRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    @Cacheable(value = "post", key = "#postId", cacheManager = "postCacheManager")
    public Optional<Post> searchByPostId(Long postId) {
        Optional<Post> result = Optional.ofNullable(queryFactory.selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(post.voteOptions, voteOptions).fetchJoin()
            .join(voteOptions.matchUser, matchUser).fetchJoin()
            .where(post.id.eq(postId)).fetchOne());
        return result;
    }

    @Override
    public Page<Post> searchGrillPostsWithoutBanPosts(Long memberId, Pageable pageable) {
        BooleanBuilder isNotBanned = isNotBanned(memberId);

        List<Post> content = queryFactory.selectFrom(post)
            .where(isNotBanned, post.voteRatio.goe(45.0))
            .join(post.member, member).fetchJoin()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(post.voteCount.desc()).fetch();

        long total = Optional.ofNullable(queryFactory.select(post.count()).from(post)
            .where(isNotBanned, post.voteRatio.goe(45.0)).fetchOne()).orElseGet(() -> 0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Post> searchNewPostsWithoutBanPosts(Long memberId, Pageable pageable) {
        BooleanBuilder isNotBanned = isNotBanned(memberId);

        JPAQuery<Post> query = queryFactory.selectFrom(post)
            .where(isNotBanned)
            .join(post.member, member).fetchJoin()
            .orderBy(post.createdAt.desc());

        long total = Optional.ofNullable(queryFactory.select(post.count())
            .from(post)
            .where(isNotBanned)
            .fetchOne()).orElseGet(() -> 0L);

        List<Post> content = query.offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Post> searchHotPostWithoutBanPosts(Long memberId, Pageable pageable) {
        BooleanBuilder isNotBanned = isNotBanned(memberId);

        List<Post> content = queryFactory.selectFrom(post)
            .where(isNotBanned)
            .join(post.member, member).fetchJoin()
            .orderBy(post.viewCount.desc()).offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = Optional.ofNullable(queryFactory.select(post.count())
            .from(post)
            .where(isNotBanned)
            .fetchOne()).orElseGet(() -> 0L);

        return new PageImpl<>(content, pageable, total);
    }

    BooleanBuilder isNotBanned(Long memberId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (memberId != null) {
            builder.and(post.id.notIn(
                JPAExpressions.select(postBan.banPost.id)
                    .from(postBan)
                    .where(postBan.member.id.eq(memberId))
            ));
            builder.and(post.member.id.notIn(
                JPAExpressions.select(memberBan.banMember.id)
                    .from(memberBan)
                    .where(memberBan.member.id.eq(memberId))
            ));
        }
        return builder;
    }
}
