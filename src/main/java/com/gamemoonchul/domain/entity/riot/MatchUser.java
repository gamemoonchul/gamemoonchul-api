package com.gamemoonchul.domain.entity.riot;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "match_user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchUser {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id")
    private String matchGameId;

    private String puuid;
    /**
     * summonerId + riotIdTagline
     */
    private String nickname;
    @Column(name = "champion_name")
    private String championName;
    private boolean win;
}
