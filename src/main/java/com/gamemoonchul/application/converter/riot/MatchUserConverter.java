package com.gamemoonchul.application.converter.riot;

import com.gamemoonchul.domain.entity.riot.MatchGame;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import com.gamemoonchul.domain.model.vo.riot.ParticipantRecord;
import org.springframework.stereotype.Service;


@Service
public class MatchUserConverter {
    public MatchUser toEntities(ParticipantRecord participant, MatchGame matchGame) {
        MatchUser entity = MatchUser.builder()
                .puuid(participant.puuid())
                .win(participant.win())
                .matchGameId(matchGame.getGameId())
                .nickname(getNickname(participant))
                .championName(participant.championName())
                .build();
        return entity;
    }

    private String getNickname(ParticipantRecord participantVO) {
        return participantVO.summonerName() + " #" + participantVO.riotIdTagline();
    }

    private String getChampionThumbnail(String championName) {
        return "https://ddragon.leagueoflegends.com/cdn/11.1.1/img/champion/" + championName + ".png";
    }
}
