package com.gamemoonchul.domain.entity;

import com.gamemoonchul.domain.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "POST")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter
    @JoinColumn(name = "memberId", referencedColumnName = "id")
    private Member member;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private List<VoteOptions> voteOptions;

    private String videoUrl;

    private String thumbnailUrl;

    private String title;

    private String content;
    @Builder.Default
    private Long viewCount = 0L;
    @Builder.Default
    private Long commentCount = 0L;
    @Builder.Default
    private Long voteCount = 0L;

    public void addVoteOptions(List<VoteOptions> voteOptions) {
        if (this.voteOptions == null) {
            this.voteOptions = new ArrayList<VoteOptions>();
        }
        this.voteOptions.addAll(voteOptions);
    }

    public boolean isHot() {
        List<Integer> votesCount = voteOptions
                .stream()
                .map(voteOptions -> {
                    return voteOptions.getVotes().size();
                })
                .toList();
        if (votesCount.get(0) == 0 || votesCount.get(1) == 0) {
            return false;
        }

        int votesTotalCount = votesCount.get(0) + votesCount.get(1);
        double firstVotingPercentage = (double) votesCount.get(0) / (double) votesTotalCount * 100;
        return firstVotingPercentage >= 45 && firstVotingPercentage <= 55;
    }
}
