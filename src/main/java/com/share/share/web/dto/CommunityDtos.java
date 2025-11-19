package com.share.share.web.dto;

import com.share.share.community.Post;
import com.share.share.community.PostComment;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommunityDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePostRequest {
        private String category;
        private String stock;
        private String title;
        private String content;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PostSummary {
        private Long id;
        private String category;
        private String stock;
        private String title;
        private String authorNickname;
        private OffsetDateTime createdAt;
        private int commentCount;
        private int upVotes;
        private int downVotes;
        private String userVote; // "up", "down", or null

        public static PostSummary from(Post post) {
            return PostSummary.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .stock(post.getStock())
                    .title(post.getTitle())
                    .authorNickname(post.getAuthor().getDisplayName())
                    .createdAt(post.getCreatedAt())
                    .commentCount(post.getComments() != null ? post.getComments().size() : 0)
                    .upVotes(post.getUpVotes())
                    .downVotes(post.getDownVotes())
                    .userVote(null) // will be set by controller
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CommentResponse {
        private Long id;
        private String authorNickname;
        private Long authorId;
        private String content;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private int upVotes;
        private int downVotes;
        private String userVote; // "up", "down", or null

        public static CommentResponse from(PostComment comment) {
            return CommentResponse.builder()
                    .id(comment.getId())
                    .authorNickname(comment.getAuthor().getDisplayName())
                    .authorId(comment.getAuthor().getId())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .upVotes(comment.getUpVotes())
                    .downVotes(comment.getDownVotes())
                    .userVote(null) // will be set by controller
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCommentRequest {
        private String content;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PostDetail {
        private Long id;
        private String category;
        private String stock;
        private String title;
        private String content;
        private String authorNickname;
        private OffsetDateTime createdAt;
        private List<CommentResponse> comments;
        private int upVotes;
        private int downVotes;
        private String userVote; // "up", "down", or null

        public static PostDetail from(Post post, List<PostComment> comments) {
            return PostDetail.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .stock(post.getStock())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorNickname(post.getAuthor().getDisplayName())
                    .createdAt(post.getCreatedAt())
                    .comments(comments.stream().map(CommentResponse::from).toList())
                    .upVotes(post.getUpVotes())
                    .downVotes(post.getDownVotes())
                    .userVote(null) // will be set by controller
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCommentRequest {
        private String content;
    }
}


