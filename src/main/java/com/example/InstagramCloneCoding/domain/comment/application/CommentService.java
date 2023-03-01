package com.example.InstagramCloneCoding.domain.comment.application;

import com.example.InstagramCloneCoding.domain.comment.dao.CommentRepository;
import com.example.InstagramCloneCoding.domain.comment.domain.Comment;
import com.example.InstagramCloneCoding.domain.comment.dto.CommentDto;
import com.example.InstagramCloneCoding.domain.comment.dto.CommentResponseDto;
import com.example.InstagramCloneCoding.domain.member.domain.Member;
import com.example.InstagramCloneCoding.domain.post.dao.PostRepository;
import com.example.InstagramCloneCoding.domain.post.domain.Post;
import com.example.InstagramCloneCoding.global.error.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;

import static com.example.InstagramCloneCoding.domain.comment.error.CommentErrorCode.COMMENT_NOT_FOUND;
import static com.example.InstagramCloneCoding.domain.comment.error.CommentErrorCode.UNAVAILABLE_COMMENT_REQUEST;
import static com.example.InstagramCloneCoding.domain.post.error.PostErrorCode.POST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    public CommentResponseDto writeComment(Member member, int postId, CommentDto commentDto) {
        int ref, refStep;
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RestApiException(POST_NOT_FOUND));

        // 대댓글인 경우 (CommentDto의 commentId가 null이 아닌 경우)
        if (commentDto.getCommentId() != null) {
            Comment comment = commentRepository.findById(commentDto.getCommentId())
                    .orElseThrow(() -> new RestApiException(COMMENT_NOT_FOUND));
            if (comment.getPost().getPostId() != postId)
                throw new RestApiException(UNAVAILABLE_COMMENT_REQUEST);

            ref = comment.getRef();
            refStep = commentRepository.findByPostAndRef(post, comment.getRef()).size();
        }
        // 대댓글이 아닌 경우 (CommentDto의 commentId가 null인 경우)
        else {
            List<Comment> comments = post.getComments();

            // 첫 댓글인 경우
            if (comments.size() == 0) {
                ref = 0;
                refStep = 0;
            }
            else {
                ref = comments.get(comments.size() - 1).getRef() + 1;
                refStep = 0;
            }
        }

        Comment comment = Comment.builder()
                .content(commentDto.getComment())
                .member(member)
                .post(post)
                .ref(ref)
                .refStep(refStep)
                .build();
        commentRepository.save(comment);

        return comment.commentToResponseDto();
    }
}
