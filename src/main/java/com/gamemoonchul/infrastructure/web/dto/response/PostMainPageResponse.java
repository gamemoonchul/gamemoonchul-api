package com.gamemoonchul.infrastructure.web.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PostMainPageResponse(
    Long id,
    MemberResponse member,
    String videoUrl,
    String thumbnailUrl,
    String title,
    String content,
    String timesAgo,
    Integer commentCount,
    List<Double> voteRatio
) {

}
