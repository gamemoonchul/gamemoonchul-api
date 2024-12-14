package com.gamemoonchul.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gamemoonchul.domain.entity.riot.MatchUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;


@Builder
@Getter
@Entity(name = "vote_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "vote_option",
    indexes = {
        @Index(name = "idx_post_id", columnList = "post_id"),
    })
public class VoteOptions {
    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "voteOption", cascade = CascadeType.ALL, orphanRemoval = true)
    final private List<Vote> votes = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_user_id")
    private MatchUser matchUser;

    public void addVote(Vote vote) {
        this.votes.add(vote);
    }

    public void deleteVote(Vote vote) {
        this.votes.remove(vote);
    }
}
