package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.common.exception.NotFoundException;
import com.gamemoonchul.domain.entity.riot.MatchGame;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.domain.model.vo.riot.MatchDummy;
import com.gamemoonchul.domain.model.vo.riot.MatchRecord;
import com.gamemoonchul.domain.model.vo.riot.ParticipantRecord;
import com.gamemoonchul.domain.status.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchUserServiceTest extends TestDataBase {
    @Autowired
    private MatchGameService matchGameService;
    @Autowired
    private MatchUserService matchUserService;

    private List<ParticipantRecord> participants;
    private List<MatchUser> matchUsers;
    private MatchGame gameEntity;

    @BeforeEach
    void setUp() {
        MatchRecord gameVO = MatchDummy.create();
        gameEntity = matchGameService.save(gameVO);
        participants = gameVO.info()
                .participants();
        matchUserService.saveAll(participants, gameEntity);
        matchUsers = matchUserService.findByMatchGameId(gameEntity.getId());
    }

    @Test
    @DisplayName("MatchUser 생성 테스트, MatchGame Eager Loading 테스트")
    void createMatchUser() {
        // given // when // then
        assertEquals(participants.size(), matchUsers.size());
        assertEquals(matchUsers.get(0)
                .getMatchGameId()
                .getGameId(), gameEntity.getGameId());
    }

    @Test
    @DisplayName("matchUserId 찾을 수 있는지 테스트")
    void successFindById() {
        // given // when
        MatchUser searchedUser = matchUserService.findById(matchUsers.get(0).getId());

        // then
        assertEquals(matchUsers.get(0)
                .getNickname(), searchedUser.getNickname());
        assertEquals(matchUsers.get(0)
                .getMatchGameId(), searchedUser.getMatchGameId());
        assertEquals(matchUsers.get(0)
                .getPuuid(), searchedUser.getPuuid());
    }

    @Test
    @DisplayName("matchUserId 찾을 수 없는 경우 테스트 ")
    void occurredWrongMemberId() {
        // given
        Long id = 999999L;

        // when // then
        assertThrows(NotFoundException.class, () -> {
            matchUserService.findById(id);
        }, PostStatus.WRONG_MATCH_USER.getMessage());
    }
}
