package com.share.share.community;

import com.share.share.user.User;
import com.share.share.user.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;

    @Transactional
    public Post createPost(User author, String category, String stock, String title, String content) {
        Post post = new Post();
        post.setAuthor(author);
        post.setCategory(category);
        post.setStock(stock);
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<Post> listPosts(String category, int page, int size) {
        if (category != null && !category.isBlank()) {
            // 핫이슈 카테고리는 추천수 10개 이상만 표시
            if ("hot".equals(category)) {
                return postRepository.findByCategoryAndUpVotesGreaterThanEqualOrderByCreatedAtDesc(
                    category, 10, PageRequest.of(page, size));
            }
            return postRepository.findByCategoryOrderByCreatedAtDesc(category, PageRequest.of(page, size));
        }
        return postRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<PostComment> listComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public PostComment addComment(User author, Long postId, String content) {
        Post post = getPost(postId);
        PostComment comment = new PostComment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public Post votePost(User user, Long postId, boolean up) {
        Post post = getPost(postId);
        
        // 이미 투표했는지 확인
        Optional<PostVote> existingVote = postVoteRepository.findByPostIdAndUserId(postId, user.getId());
        
        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();
            // 같은 투표면 에러, 다른 투표면 변경
            if (vote.isUpvote() == up) {
                throw new IllegalStateException("이미 투표하셨습니다.");
            } else {
                // 투표 변경: 기존 투표 취소하고 새 투표
                if (vote.isUpvote()) {
                    post.setUpVotes(post.getUpVotes() - 1);
                    post.setDownVotes(post.getDownVotes() + 1);
                } else {
                    post.setDownVotes(post.getDownVotes() - 1);
                    post.setUpVotes(post.getUpVotes() + 1);
                }
                vote.setUpvote(up);
                postVoteRepository.save(vote);
            }
        } else {
            // 새 투표
            PostVote vote = new PostVote();
            vote.setPost(post);
            vote.setUser(user);
            vote.setUpvote(up);
            postVoteRepository.save(vote);
            
            if (up) {
                post.setUpVotes(post.getUpVotes() + 1);
            } else {
                post.setDownVotes(post.getDownVotes() + 1);
            }
        }
        
        return postRepository.save(post);
    }

    @Transactional
    public PostComment voteComment(User user, Long commentId, boolean up) {
        PostComment comment = commentRepository.findById(commentId).orElseThrow();
        
        // 이미 투표했는지 확인
        Optional<CommentVote> existingVote = commentVoteRepository.findByCommentIdAndUserId(commentId, user.getId());
        
        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            // 같은 투표면 에러, 다른 투표면 변경
            if (vote.isUpvote() == up) {
                throw new IllegalStateException("이미 투표하셨습니다.");
            } else {
                // 투표 변경: 기존 투표 취소하고 새 투표
                if (vote.isUpvote()) {
                    comment.setUpVotes(comment.getUpVotes() - 1);
                    comment.setDownVotes(comment.getDownVotes() + 1);
                } else {
                    comment.setDownVotes(comment.getDownVotes() - 1);
                    comment.setUpVotes(comment.getUpVotes() + 1);
                }
                vote.setUpvote(up);
                commentVoteRepository.save(vote);
            }
        } else {
            // 새 투표
            CommentVote vote = new CommentVote();
            vote.setComment(comment);
            vote.setUser(user);
            vote.setUpvote(up);
            commentVoteRepository.save(vote);
            
            if (up) {
                comment.setUpVotes(comment.getUpVotes() + 1);
            } else {
                comment.setDownVotes(comment.getDownVotes() + 1);
            }
        }
        
        return commentRepository.save(comment);
    }
    
    @Transactional(readOnly = true)
    public boolean hasUserVotedPost(Long postId, Long userId) {
        return postVoteRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }
    
    @Transactional(readOnly = true)
    public boolean hasUserVotedComment(Long commentId, Long userId) {
        return commentVoteRepository.findByCommentIdAndUserId(commentId, userId).isPresent();
    }
    
    @Transactional(readOnly = true)
    public Boolean getUserPostVote(Long postId, Long userId) {
        return postVoteRepository.findByPostIdAndUserId(postId, userId)
            .map(PostVote::isUpvote)
            .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Boolean getUserCommentVote(Long commentId, Long userId) {
        return commentVoteRepository.findByCommentIdAndUserId(commentId, userId)
            .map(CommentVote::isUpvote)
            .orElse(null);
    }
    
    @Transactional
    public PostComment updateComment(User user, Long commentId, String content) {
        PostComment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("댓글을 수정할 권한이 없습니다.");
        }
        comment.setContent(content);
        return commentRepository.save(comment);
    }
    
    @Transactional
    public void deleteComment(User user, Long commentId) {
        PostComment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
    
    @Transactional(readOnly = true)
    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Post> getUserCommentedPosts(Long userId) {
        List<PostComment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        return comments.stream()
            .map(PostComment::getPost)
            .distinct()
            .toList();
    }
    
    @Transactional(readOnly = true)
    public long getUserPostCount(Long userId) {
        return postRepository.countByAuthorId(userId);
    }
    
    @Transactional(readOnly = true)
    public long getUserCommentCount(Long userId) {
        return commentRepository.countByAuthorId(userId);
    }
    
    @Transactional
    public void createSamplePosts(User author) {
        // 자유게시판 샘플 글
        createPost(author, "free", "AAPL", "애플 주식 전망에 대한 의견", 
            "애플 주식이 최근 상승세를 보이고 있습니다. 기술적 분석과 펀더멘털을 종합해보면...");
        createPost(author, "free", "TSLA", "테슬라 주식 투자 후기", 
            "테슬라에 투자한 지 1년이 지났습니다. 변동성이 크지만 장기적으로는 긍정적입니다.");
        createPost(author, "free", null, "주식 투자 초보자를 위한 팁", 
            "주식 투자를 시작하는 분들을 위해 기본적인 팁을 공유합니다.");
        createPost(author, "free", "NVDA", "엔비디아 주식 분석", 
            "AI 붐으로 엔비디아 주식이 급등했습니다. 현재 가치 평가와 향후 전망을 분석해봅니다.");
        createPost(author, "free", "MSFT", "마이크로소프트 주식 전망", 
            "클라우드 사업부의 성장세가 두드러집니다. 장기 투자 관점에서 분석합니다.");
        
        // 핫이슈 샘플 글 (추천수 10개 이상으로 설정)
        Post hot1 = createPost(author, "hot", "AAPL", "애플 신제품 발표 임박, 주가 영향은?", 
            "애플의 신제품 발표가 임박했습니다. 주가에 미칠 영향을 분석합니다.");
        hot1.setUpVotes(15);
        postRepository.save(hot1);
        
        Post hot2 = createPost(author, "hot", "TSLA", "테슬라 실적 발표, 시장 반응 주목", 
            "테슬라의 분기별 실적 발표가 예정되어 있습니다. 시장의 반응이 주목됩니다.");
        hot2.setUpVotes(12);
        postRepository.save(hot2);
        
        Post hot3 = createPost(author, "hot", null, "금리 인하 기대감, 주식 시장 영향", 
            "중앙은행의 금리 인하 기대감이 높아지면서 주식 시장에 긍정적 영향을 미치고 있습니다.");
        hot3.setUpVotes(18);
        postRepository.save(hot3);
        
        Post hot4 = createPost(author, "hot", "NVDA", "엔비디아 실적 발표, AI 수요 급증", 
            "엔비디아의 최근 실적이 시장 기대를 뛰어넘었습니다. AI 수요가 계속 증가하고 있습니다.");
        hot4.setUpVotes(20);
        postRepository.save(hot4);
        
        // 공지사항 샘플 글
        createPost(author, "notice", null, "TradeNote 서비스 이용 안내", 
            "TradeNote를 이용해주셔서 감사합니다. 서비스 이용 시 주의사항을 안내드립니다.");
        createPost(author, "notice", null, "커뮤니티 이용 규칙", 
            "모든 사용자가 편안하게 이용할 수 있도록 커뮤니티 이용 규칙을 준수해주시기 바랍니다.");
    }
}


