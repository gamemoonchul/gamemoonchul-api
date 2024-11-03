package com.gamemoonchul.domain.entity.redis;

import com.gamemoonchul.infrastructure.web.dto.response.CommentResponse;
import com.gamemoonchul.infrastructure.web.dto.response.MemberResponse;
import com.gamemoonchul.infrastructure.web.dto.response.VoteRatioResponse;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "post-detail", timeToLive = 600L) // 10분간 살아 있음
public class RedisPostDetail {
    @Id
    @Indexed
    private Long id;
    private MemberResponse author;
    private String videoUrl;
    private String thumbnailUrl;
    private String title;
    private String content;
    private String timesAgo;
    private Long viewCount;
    private Long commentCount;
    private List<CommentResponse> comments;
    private List<VoteRatioResponse> voteDetail;

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
}
