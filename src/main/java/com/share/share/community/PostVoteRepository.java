package com.share.share.community;

import com.share.share.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostIdAndIsUpvote(Long postId, boolean isUpvote);
}

