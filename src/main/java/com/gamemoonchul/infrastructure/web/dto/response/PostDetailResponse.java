package com.gamemoonchul.infrastructure.web.dto.response;

import com.gamemoonchul.application.member.MemberConverter;
import com.gamemoonchul.common.util.StringUtils;
import com.gamemoonchul.domain.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private MemberResponseDto author;
    private String videoUrl;
    private String thumbnailUrl;
    private String title;
    private String content;
    private String timesAgo;
    private Long viewCount;
    private Long commentCount;
    private List<CommentResponse> comments;
    private List<VoteRatioResponse> voteDetail;

    public static PostDetailResponse toResponse(Post post, List<CommentResponse> comments) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .author(MemberConverter.toResponseDto(post.getMember()))
                .videoUrl(post.getVideoUrl())
                .thumbnailUrl(post.getThumbnailUrl())
                .commentCount(post.getCommentCount())
                .title(post.getTitle())
                .content(post.getContent())
                .timesAgo(StringUtils.getTimeAgo(post.getCreatedAt()))
                .viewCount(post.getViewCount())
                .comments(comments)
                .voteDetail(getVoteDetail(post))
                .build();
    }

    public static List<VoteRatioResponse> getVoteDetail(Post post) {
        HashMap<Long, Double> voteRatioMap = new HashMap<>();
        post.getVoteOptions()
                .forEach(vo -> {
                    voteRatioMap.put(vo.getId(), 0.0);
                    vo.getVotes()
                            .forEach(v -> {
                                Double cnt = voteRatioMap.get(v.getId());
                                cnt++;
                                voteRatioMap.put(v.getId(), cnt);
                            });
                });

        Double totalVoteCnt = voteRatioMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        voteRatioMap.forEach((k, v) -> {
            if (totalVoteCnt == 0) {
                voteRatioMap.put(k, 0.0);
            } else {
                voteRatioMap.put(k, (v / totalVoteCnt) * 100);
            }
        });

        List<VoteRatioResponse> result = post.getVoteOptions()
                .stream()
                .map(vo -> {
                    Double voteRatio = voteRatioMap.get(vo.getId());
                    MatchGameResponse.MatchUserResponse matchUserResponse = MatchGameResponse.MatchUserResponse.toResponse(vo.getMatchUser());
                    return new VoteRatioResponse(matchUserResponse, voteRatio);
                })
                .toList();

        return result;
    }
}
