package com.gamemoonchul.application.converter;

import com.gamemoonchul.common.util.StringUtils;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.domain.entity.redis.RedisPostDetail;
import com.gamemoonchul.infrastructure.web.dto.request.PostUploadRequest;
import com.gamemoonchul.infrastructure.web.dto.response.CommentResponse;
import com.gamemoonchul.infrastructure.web.dto.response.MatchUserResponse;
import com.gamemoonchul.infrastructure.web.dto.response.PostDetailResponse;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import com.gamemoonchul.infrastructure.web.dto.response.VoteRatioResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PostConverter {
    public static Post requestToEntity(PostUploadRequest request, Member member) {
        Post entity = Post.builder()
            .title(request.title())
            .videoUrl(request.videoUrl())
            .thumbnailUrl(request.thumbnailUrl())
            .content(request.content())
            .tags(request.tags())
            .member(member)
            .build();
        return entity;
    }

    public static RedisPostDetail toCache(Post post, Long commentCount) {
        return RedisPostDetail.builder()
            .id(post.getId())
            .author(MemberConverter.toResponseDto(post.getMember()))
            .videoUrl(post.getVideoUrl())
            .thumbnailUrl(post.getThumbnailUrl())
            .commentCount(commentCount)
            .title(post.getTitle())
            .content(post.getContent())
            .timesAgo(StringUtils.getTimeAgo(post.getCreatedAt()))
            .viewCount(post.getViewCount())
            .voteDetail(getVoteDetail(post))
            .build();
    }

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
                Optional.ofNullable(vo.getVotes())
                    .ifPresent(votes -> {
                        votes.forEach(v -> voteRatioMap.put(v.getId(), 0.0));
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
                MatchUserResponse matchUserResponse = MatchUserConverter.toResponseVoId(vo.getMatchUser(), vo.getId());
                return new VoteRatioResponse(matchUserResponse, voteRatio);
            })
            .toList();

        return result;
    }

    public static PostMainPageResponse entityToResponse(Post entity, Integer commentCount) {
        List<Double> voteRatio = List.of(100 - entity.getVoteRatio(), entity.getVoteRatio());
        return PostMainPageResponse.builder()
            .id(entity.getId())
            .member(MemberConverter.toResponseDto(entity.getMember()))
            .videoUrl(entity.getVideoUrl())
            .thumbnailUrl(entity.getThumbnailUrl())
            .title(entity.getTitle())
            .content(entity.getContent())
            .commentCount(commentCount)
            .timesAgo(StringUtils.getTimeAgo(entity.getCreatedAt()))
            .voteRatio(voteRatio)
            .build();
    }
}
