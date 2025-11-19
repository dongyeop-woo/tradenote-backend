package com.share.share.community;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    List<PostComment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    long countByAuthorId(Long authorId);
}


