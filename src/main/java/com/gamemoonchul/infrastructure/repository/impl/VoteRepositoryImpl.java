package com.gamemoonchul.infrastructure.repository.impl;

import com.gamemoonchul.domain.entity.Vote;
import com.gamemoonchul.domain.entity.VoteOptions;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.infrastructure.repository.ifs.VoteRepositoryIfs;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.gamemoonchul.domain.entity.QPost.post;
import static com.gamemoonchul.domain.entity.QVote.vote;
import static com.gamemoonchul.domain.entity.QVoteOptions.voteOptions;
import static com.gamemoonchul.domain.entity.riot.QMatchUser.matchUser;

@Repository
public class VoteRepositoryImpl implements VoteRepositoryIfs {
    JPAQueryFactory queryFactory;

    public VoteRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Vote> searchVoteByPostIdAndVoteUserId(Long postId, Long voteUserId) {
        return Optional.empty();
    }

    @Override
    public HashMap<MatchUser, Integer> getVoteRateByPostId(Long postId) {
        List<Vote> searchedVotes = queryFactory.select(vote)
                .from(vote)
                .join(vote.post, post)
                .fetchJoin()
                .join(vote.voteOptions, voteOptions)
                .fetchJoin()
                .join(voteOptions.matchUser, matchUser)
                .fetchJoin()
                .where(post.id.eq(postId))
                .fetch()
                ;

        HashMap<MatchUser, Integer> voteRateHashMap = new HashMap<>();
        int sum = 0;
        for (Vote v: searchedVotes) {
            MatchUser curMatchUser = v.getVoteOptions().getMatchUser();
            int curVal = voteRateHashMap.getOrDefault(curMatchUser, 0);
            voteRateHashMap.put(curMatchUser, curVal + 1);
            sum++;
        }

        for (MatchUser vo: voteRateHashMap.keySet()) {
            voteRateHashMap.put(vo, voteRateHashMap.get(vo) * 100 / sum);
        }

        return voteRateHashMap;
    }
}
