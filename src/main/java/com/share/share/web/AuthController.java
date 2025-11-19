package com.share.share.web;

import com.share.share.security.UserPrincipal;
import com.share.share.user.User;
import com.share.share.user.UserService;
import com.share.share.web.dto.AuthResponse;
import com.share.share.web.dto.ChangePasswordRequest;
import com.share.share.web.dto.LoginRequest;
import com.share.share.web.dto.RegisterRequest;
import com.share.share.web.dto.UpdateDisplayNameRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest) {
        User user = userService.register(request);
        Authentication authentication = authenticateAndStoreSession(user.getUsername(), request.getPassword(), httpRequest);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(principal.getUser()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        Authentication authentication =
                authenticateAndStoreSession(request.getUsername(), request.getPassword(), httpRequest);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(AuthResponse.from(principal.getUser()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        org.springframework.security.core.context.SecurityContext context = 
            (org.springframework.security.core.context.SecurityContext) securityContext;
        Authentication authentication = context.getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        User user = ((UserPrincipal) principal).getUser();
        return ResponseEntity.ok(AuthResponse.from(user));
    }

    @PutMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        
        // 세션 재인증
        Authentication authentication = authenticateAndStoreSession(
            user.getUsername(), 
            request.getNewPassword(), 
            httpRequest
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(AuthResponse.from(principal.getUser()));
    }

    @PutMapping("/display-name")
    public ResponseEntity<AuthResponse> updateDisplayName(
            @RequestBody @Valid UpdateDisplayNameRequest request,
            HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        User updated = userService.updateDisplayName(user, request.getDisplayName());
        
        // 세션 업데이트
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (securityContext instanceof org.springframework.security.core.context.SecurityContext context) {
                Authentication auth = context.getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                    principal.getUser().setDisplayName(request.getDisplayName());
                }
            }
        }
        
        return ResponseEntity.ok(AuthResponse.from(updated));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        userService.deleteUser(user);
        
        // 세션 무효화
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        return ResponseEntity.noContent().build();
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        org.springframework.security.core.context.SecurityContext context = 
            (org.springframework.security.core.context.SecurityContext) securityContext;
        Authentication authentication = context.getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return ((UserPrincipal) principal).getUser();
    }

    private Authentication authenticateAndStoreSession(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username.toLowerCase(), password);
        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        return authentication;
    }
}

