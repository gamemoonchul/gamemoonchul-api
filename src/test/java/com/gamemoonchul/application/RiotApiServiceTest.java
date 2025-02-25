package com.gamemoonchul.application;

import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.domain.entity.riot.MatchGame;
import com.gamemoonchul.domain.model.vo.riot.MatchDummy;
import com.gamemoonchul.domain.model.vo.riot.MatchRecord;
import com.gamemoonchul.domain.model.vo.riot.ParticipantRecord;
import com.gamemoonchul.infrastructure.adapter.RiotApiAdapter;
import com.gamemoonchul.infrastructure.web.dto.response.MatchGameResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiotApiServiceTest extends TestDataBase {
    @Mock
    private RiotApiAdapter mockRiotApi;

    @Autowired
    private MatchGameService matchGameService;

    @Autowired
    private MatchUserService matchUserService;

    private RiotApiService riotApiService;

    @BeforeEach
    void setUp() {
        riotApiService = new RiotApiService(matchGameService, matchUserService, mockRiotApi);
    }

    @Test
    @DisplayName("이미 저장된 게임이 있을 때 lolSearchAdapter를 통해서 검색하지 않는다")
    void searchAlreadySavedGame() {
        // given
        MatchRecord vo = MatchDummy.create();
        MatchGame game = matchGameService.save(vo);
        List<ParticipantRecord> participantVO = vo.info()
                .participants();
        matchUserService.saveAll(participantVO, game);

        // when
        riotApiService.searchMatch(game.getGameId());

        // then
        verify(mockRiotApi, never()).searchMatch(anyString());
    }

    @Test
    @DisplayName("저장되지 않은 게임이 있을 때 lolSearchAdapter를 통해서 검색한다")
    void searchNotSavedGame() {
        // given
        String gameId = "KR_6980800844";
        MatchRecord match = MatchDummy.create();
        when(mockRiotApi.searchMatch(gameId)).thenReturn(match);

        // when
        MatchGameResponse response = riotApiService.searchMatch(gameId);

        // then
        verify(mockRiotApi, times(1)).searchMatch(gameId);
    }
}