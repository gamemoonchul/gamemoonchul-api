package com.gamemoonchul.domain.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gamemoonchul.domain.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "comment")
@Getter
@Setter
@Table(indexes = {
    @Index(name = "idx_member_id", columnList = "member_id"),
    @Index(name = "idx_post_id", columnList = "post_id"),
})
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    private Member member;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "post_id")
    private Post post;

    private String content;

    public Boolean parentExist() {
        return this.parentId != null;
    }
}
