package com.share.share.community;

import com.share.share.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
}, indexes = {
    @Index(name = "idx_post_votes_post_user", columnList = "post_id,user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_upvote", nullable = false)
    private boolean isUpvote; // true = 추천, false = 비추천
}

