package com.gamemoonchul.infrastructure.web;

import com.gamemoonchul.application.CommentService;
import com.gamemoonchul.common.annotation.MemberId;
import com.gamemoonchul.common.annotation.MemberSession;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.infrastructure.web.dto.request.CommentSaveRequest;
import com.gamemoonchul.infrastructure.web.common.RestControllerWithEnvelopPattern;
import com.gamemoonchul.infrastructure.web.dto.request.CommentFixRequest;
import com.gamemoonchul.infrastructure.web.dto.request.CommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/comments")
@RestControllerWithEnvelopPattern
public class CommentApiController {
    private final CommentService commentService;

    @PostMapping
    public void save(@RequestBody CommentRequest request, @MemberSession Member member) {
        CommentSaveRequest saveDto = new CommentSaveRequest(null, request.content(), request.postId());
        commentService.save(saveDto, member);
    }

    @PostMapping("/{parentId}")
    public void save(
        @PathVariable(name = "parentId") Long parentId,
        @RequestBody CommentRequest request,
        @MemberSession Member member) {
        CommentSaveRequest saveDto = new CommentSaveRequest(parentId, request.content(), request.postId());
        commentService.save(saveDto, member);
    }

    @PatchMapping
    public void fix(@RequestBody CommentFixRequest request, @MemberId Long requestMemberId) {
        commentService.fix(request, requestMemberId);
    }

    @DeleteMapping("/{id}")
    public void del(@PathVariable(name = "id") Long id, @MemberId Long requestMemberId) {
        commentService.delete(id, requestMemberId);
    }
}
