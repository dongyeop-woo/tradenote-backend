package com.share.share.community;

import com.share.share.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comment_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
}, indexes = {
    @Index(name = "idx_comment_votes_comment_user", columnList = "comment_id,user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CommentVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private PostComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_upvote", nullable = false)
    private boolean isUpvote; // true = 추천, false = 비추천
}

