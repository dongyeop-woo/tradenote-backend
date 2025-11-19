package com.share.share.web;

import com.share.share.security.UserPrincipal;
import com.share.share.trade.Trade;
import com.share.share.trade.TradeService;
import com.share.share.user.User;
import com.share.share.web.dto.TradeDtos.CreateTradeRequest;
import com.share.share.web.dto.TradeDtos.TradeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<TradeResponse> create(
            @RequestBody CreateTradeRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        Trade saved = tradeService.create(user, request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeResponse.from(saved));
    }

    @GetMapping("/today")
    public List<TradeResponse> today(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        return tradeService.findToday(user).stream().map(TradeResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/month")
    public List<TradeResponse> byMonth(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        YearMonth ym = YearMonth.of(year, month);
        return tradeService.findByMonth(user, ym).stream().map(TradeResponse::from).collect(Collectors.toList());
    }

    @GetMapping
    public List<TradeResponse> all(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        return tradeService.findAll(user).stream().map(TradeResponse::from).collect(Collectors.toList());
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (!(securityContext instanceof SecurityContext context)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return principal.getUser();
    }
}


