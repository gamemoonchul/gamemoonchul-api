package com.gamemoonchul.infrastructure.repository.impl;

import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.infrastructure.repository.ifs.MatchUserRepositoryIfs;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gamemoonchul.domain.entity.riot.QMatchGame.matchGame;
import static com.gamemoonchul.domain.entity.riot.QMatchUser.matchUser;

@Repository
public class MatchUserRepositoryImpl implements MatchUserRepositoryIfs {
    JPAQueryFactory queryFactory;

    public MatchUserRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MatchUser> searchByGameId(Long gameId) {
        return queryFactory
                .select(matchUser)
                .from(matchUser)
                .join(matchGame)
                .on(matchGame.gameId.eq(matchUser.matchGameId))
                .fetchJoin()
                .where(matchGame.id.eq(gameId))
                .fetch();
    }
}
