package com.share.share.web;

import com.share.share.community.CommunityService;
import com.share.share.community.Post;
import com.share.share.community.PostComment;
import com.share.share.security.UserPrincipal;
import com.share.share.user.User;
import com.share.share.web.dto.CommunityDtos.CreateCommentRequest;
import com.share.share.web.dto.CommunityDtos.CreatePostRequest;
import com.share.share.web.dto.CommunityDtos.PostDetail;
import com.share.share.web.dto.CommunityDtos.PostSummary;
import com.share.share.web.dto.CommunityDtos.UpdateCommentRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/posts")
    public ResponseEntity<PostSummary> createPost(
            @RequestBody CreatePostRequest req,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        Post post = communityService.createPost(
                user,
                safe(req.getCategory(), "free"),
                nullToEmpty(req.getStock()),
                safe(req.getTitle(), ""),
                safe(req.getContent(), ""));
        return ResponseEntity.status(HttpStatus.CREATED).body(PostSummary.from(post));
    }

    @GetMapping("/posts")
    public Page<PostSummary> listPosts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest
    ) {
        Long userId = null;
        try {
            User user = getSessionUser(httpRequest);
            userId = user.getId();
        } catch (Exception e) {
            // 로그인하지 않은 경우
        }
        final Long finalUserId = userId;
        return communityService.listPosts(category, page, size).map(post -> {
            PostSummary summary = PostSummary.from(post);
            if (finalUserId != null) {
                Boolean voteType = communityService.getUserPostVote(post.getId(), finalUserId);
                summary.setUserVote(voteType != null ? (voteType ? "up" : "down") : null);
            }
            return summary;
        });
    }

    @GetMapping("/posts/{id}")
    public PostDetail getPost(@PathVariable Long id, HttpServletRequest httpRequest) {
        Post post = communityService.getPost(id);
        List<PostComment> comments = communityService.listComments(id);
        
        Long userId = null;
        try {
            User user = getSessionUser(httpRequest);
            userId = user.getId();
        } catch (Exception e) {
            // 로그인하지 않은 경우
        }
        final Long finalUserId = userId;
        
        PostDetail detail = PostDetail.from(post, comments);
        
        // 게시글 투표 상태
        if (finalUserId != null) {
            Boolean voteType = communityService.getUserPostVote(id, finalUserId);
            detail.setUserVote(voteType != null ? (voteType ? "up" : "down") : null);
            
            // 댓글 투표 상태
            detail.getComments().forEach(comment -> {
                Boolean commentVoteType = communityService.getUserCommentVote(comment.getId(), finalUserId);
                comment.setUserVote(commentVoteType != null ? (commentVoteType ? "up" : "down") : null);
            });
        }
        
        return detail;
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody CreateCommentRequest req,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        PostComment comment = communityService.addComment(user, id, safe(req.getContent(), ""));
        return ResponseEntity.status(HttpStatus.CREATED).body(comment.getId());
    }

    @PostMapping("/posts/{id}/vote")
    public PostSummary votePost(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "up") String type,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        boolean up = !"down".equalsIgnoreCase(type);
        Post post = communityService.votePost(user, id, up);
        return PostSummary.from(post);
    }

    @PostMapping("/comments/{id}/vote")
    public ResponseEntity<?> voteComment(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "up") String type,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        boolean up = !"down".equalsIgnoreCase(type);
        PostComment updated = communityService.voteComment(user, id, up);
        return ResponseEntity.ok(updated.getUpVotes() - updated.getDownVotes());
    }
    
    @GetMapping("/posts/{id}/vote-status")
    public ResponseEntity<?> getPostVoteStatus(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            User user = getSessionUser(httpRequest);
            Boolean voteType = communityService.getUserPostVote(id, user.getId());
            return ResponseEntity.ok(voteType != null ? (voteType ? "up" : "down") : null);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }
    }
    
    @GetMapping("/comments/{id}/vote-status")
    public ResponseEntity<?> getCommentVoteStatus(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            User user = getSessionUser(httpRequest);
            Boolean voteType = communityService.getUserCommentVote(id, user.getId());
            return ResponseEntity.ok(voteType != null ? (voteType ? "up" : "down") : null);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }
    }
    
    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody UpdateCommentRequest req,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        PostComment updated = communityService.updateComment(user, id, safe(req.getContent(), ""));
        return ResponseEntity.ok(updated.getId());
    }
    
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        User user = getSessionUser(httpRequest);
        communityService.deleteComment(user, id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/sample-data")
    public ResponseEntity<?> createSampleData(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        communityService.createSamplePosts(user);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my/posts")
    public List<PostSummary> getMyPosts(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        List<Post> posts = communityService.getUserPosts(user.getId());
        return posts.stream().map(PostSummary::from).toList();
    }
    
    @GetMapping("/my/comments")
    public List<PostSummary> getMyCommentedPosts(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        List<Post> posts = communityService.getUserCommentedPosts(user.getId());
        return posts.stream().map(PostSummary::from).toList();
    }
    
    @GetMapping("/my/stats")
    public ResponseEntity<?> getMyStats(HttpServletRequest httpRequest) {
        User user = getSessionUser(httpRequest);
        long postCount = communityService.getUserPostCount(user.getId());
        long commentCount = communityService.getUserCommentCount(user.getId());
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("postCount", postCount);
            put("commentCount", commentCount);
        }});
    }

    private static String safe(String val, String fallback) {
        return val == null ? fallback : val;
    }

    private static String nullToEmpty(String val) {
        return val == null ? "" : val;
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


