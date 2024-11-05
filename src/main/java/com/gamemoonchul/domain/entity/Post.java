package com.gamemoonchul.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gamemoonchul.application.converter.JsonStringListConverter;
import com.gamemoonchul.domain.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post", indexes = {
    @Index(name = "idx_vote_ratio_vote_count", columnList = "vote_ratio DESC, vote_count DESC"),
    @Index(name = "idx_post_created_at_desc", columnList = "created_at DESC"),
    @Index(name = "idx_view_count", columnList = "view_count DESC"),
    @Index(name = "idx_member_id", columnList = "member_id")
})
public class Post extends BaseTimeEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOptions> voteOptions;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private String title;

    private String content;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> tags;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @Builder.Default
    @Column(name = "vote_count")
    private Long voteCount = 0L;

    @Builder.Default
    @Column(name = "vote_ratio")
    private Double voteRatio = 0.0;

    @Version
    private Integer version;

    public void viewCountUp() {
        this.viewCount++;
    }

    public void addVoteOptions(List<VoteOptions> voteOptions) {
        if (this.voteOptions == null) {
            this.voteOptions = new ArrayList<VoteOptions>();
        }
        this.voteOptions.addAll(voteOptions);
    }

    @JsonIgnore
    public Double getMinVoteRatio() {
        int totalVoteCount = voteOptions.stream()
            .mapToInt(voteOption -> voteOption.getVotes()
                .size())
            .sum();
        if (totalVoteCount == 0) {
            return 0.0;
        }
        double firstIndexVoteRatio = (double) voteOptions.get(0)
            .getVotes()
            .size() / (double) totalVoteCount * 100;
        return Math.min(100.0 - firstIndexVoteRatio, firstIndexVoteRatio);
    }

    public void updateVoteRatio(double voteRatio) {
        this.voteRatio = voteRatio;
    }
}
