package com.gamemoonchul.application;

import com.gamemoonchul.MatchUserService;
import com.gamemoonchul.domain.entity.riot.MatchGame;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.domain.model.vo.riot.MatchVO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MatchGameServiceTest {
    @Autowired
    private MatchGameService matchGameService;

    @Autowired
    private MatchUserService matchUserService;

    @DisplayName("저장테스트")
    @Test
    void save() {
        // given
        MatchVO dummyVO = MatchVO.Dummy.createDummy();

        // when
        MatchGame matchGame = matchGameService.save(dummyVO);

        // when
        assertEquals(dummyVO.getMetadata().getMatchId(), matchGame.getId());
    }

    @DisplayName("조회테스트, matchUsers Loading Eager")
    @Test
    void setMatchGameService() {
        // given
        MatchVO dummyVO = MatchVO.Dummy.createDummy();
        MatchGame matchGame = matchGameService.save(dummyVO);
        List<MatchUser> matchUsers = matchUserService.saveAll(dummyVO.getInfo().getParticipants(), matchGame);

        // when
        Optional<MatchGame> optionalMatchGame = matchGameService.findById(matchGame.getId());
        MatchGame result = optionalMatchGame.get();

        // then
        assertEquals(matchGame.getId(), result.getId());
        assertEquals(matchGame.getMatchUsers().size(), result.getMatchUsers().size());
    }
}