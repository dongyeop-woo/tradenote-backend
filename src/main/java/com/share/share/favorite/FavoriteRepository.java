package com.share.share.favorite;

import com.share.share.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favorite> findByUserAndSymbol(User user, String symbol);
    void deleteByUserAndSymbol(User user, String symbol);
    boolean existsByUserAndSymbol(User user, String symbol);
}

