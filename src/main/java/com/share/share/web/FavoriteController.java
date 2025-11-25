package com.share.share.web;

import com.share.share.favorite.Favorite;
import com.share.share.favorite.FavoriteRepository;
import com.share.share.security.UserPrincipal;
import com.share.share.user.User;
import com.share.share.web.dto.FavoriteDtos.FavoriteRequest;
import com.share.share.web.dto.FavoriteDtos.FavoriteResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getFavorites(HttpServletRequest request) {
        User user = getSessionUser(request);
        
        List<Favorite> favorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        List<FavoriteResponse> responses = favorites.stream()
                .map(fav -> new FavoriteResponse(fav.getSymbol(), fav.getName()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> addFavorite(
            @RequestBody @Valid FavoriteRequest request,
            HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        
        // 이미 존재하는지 확인
        if (favoriteRepository.existsByUserAndSymbol(user, request.getSymbol())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 즐겨찾기에 추가된 종목입니다.");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setSymbol(request.getSymbol());
        favorite.setName(request.getName());
        
        favorite = favoriteRepository.save(favorite);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FavoriteResponse(favorite.getSymbol(), favorite.getName()));
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String symbol,
            HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        
        favoriteRepository.deleteByUserAndSymbol(user, symbol);
        
        return ResponseEntity.noContent().build();
    }

    private User getSessionUser(HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        Object securityContext = session.getAttribute(
                org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        org.springframework.security.core.context.SecurityContext context = 
            (org.springframework.security.core.context.SecurityContext) securityContext;
        org.springframework.security.core.Authentication authentication = context.getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return ((com.share.share.security.UserPrincipal) principal).getUser();
    }
}

