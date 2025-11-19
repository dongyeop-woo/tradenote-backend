package com.share.share.trade;

import com.share.share.user.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserAndDate(User user, LocalDate date);
    List<Trade> findByUserAndDateBetween(User user, LocalDate startInclusive, LocalDate endInclusive);
    List<Trade> findByUserOrderByDateDesc(User user);
}


