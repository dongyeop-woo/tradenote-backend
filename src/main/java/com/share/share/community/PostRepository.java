package com.share.share.community;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.upVotes >= 10 ORDER BY p.createdAt DESC")
    Page<Post> findByCategoryAndUpVotesGreaterThanEqualOrderByCreatedAtDesc(
        @Param("category") String category, 
        @Param("upVotes") int minUpVotes, 
        Pageable pageable
    );
    
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    long countByAuthorId(Long authorId);
}


