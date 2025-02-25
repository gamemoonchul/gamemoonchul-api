package com.gamemoonchul.domain.model.vo.riot;

import lombok.Builder;

@Builder
public record ParticipantRecord(
        int kills,
        int deaths,
        int assists,
        int champLevel,
        int championId,
        String championName,
        int damageDealtToBuildings,
        int damageDealtToObjectives,
        int damageDealtToTurrets,
        int damageSelfMitigated,
        int item0,
        int item1,
        int item2,
        int item3,
        int item4,
        int item5,
        int item6,
        int neutralMinionsKilled,
        int participantId,
        int physicalDamageDealtToChampions,
        int magicDamageDealtToChampions,
        int totalDamageDealtToChampions,
        String puuid,
        String riotIdName,
        String riotIdTagline,
        String role,
        int sightWardsBoughtInGame,
        int spell1Casts,
        int spell2Casts,
        int spell3Casts,
        int spell4Casts,
        int summoner1Casts,
        int summoner1Id,
        int summoner2Casts,
        int summoner2Id,
        String summonerId,
        int summonerLevel,
        String summonerName,
        int teamId,
        String teamPosition,
        boolean win
) {
}