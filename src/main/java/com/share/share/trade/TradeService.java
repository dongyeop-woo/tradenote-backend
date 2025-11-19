package com.share.share.trade;

import com.share.share.user.User;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    @Transactional
    public Trade create(User user, Trade trade) {
        trade.setUser(user);
        return tradeRepository.save(trade);
    }

    @Transactional(readOnly = true)
    public List<Trade> findToday(User user) {
        return tradeRepository.findByUserAndDate(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Trade> findByMonth(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return tradeRepository.findByUserAndDateBetween(user, start, end);
    }

    @Transactional(readOnly = true)
    public List<Trade> findAll(User user) {
        return tradeRepository.findByUserOrderByDateDesc(user);
    }
}


