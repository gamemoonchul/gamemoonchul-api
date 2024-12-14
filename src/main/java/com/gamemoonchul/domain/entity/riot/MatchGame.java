package com.gamemoonchul.domain.entity.riot;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "match_game", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"game_id"})
},
    indexes = {
        @Index(name = "idx_match_game_game_id", columnList = "game_id")}
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchGame {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "game_creation")
    private String gameCreation;

    @Column(name = "game_duration")
    private long gameDuration;

    @Column(name = "game_mode")
    private String gameMode;

    @JsonManagedReference
    @OneToMany(mappedBy = "matchGame", fetch = FetchType.EAGER)
    private List<MatchUser> matchUsers;

    public void addMatchUser(MatchUser matchUser) {
        if (matchUsers == null) {
            matchUsers = new ArrayList<>();
        }
        matchUsers.add(matchUser);
    }
}
