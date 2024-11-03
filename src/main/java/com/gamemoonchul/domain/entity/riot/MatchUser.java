package com.gamemoonchul.domain.entity.riot;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "game_id", referencedColumnName = "game_id")
    private MatchGame matchGame;
    private String puuid;
    /**
     * summonerId + riotIdTagline
     */
    private String nickname;
    @Column(name = "champion_name")
    private String championName;
    private boolean win;

    public void setMatchGame(MatchGame matchGame) {
        if (this.matchGame != null) {
            this.matchGame.getMatchUsers()
                    .remove(this);
        }
        this.matchGame = matchGame;
        matchGame.addMatchUser(this);
    }
}
