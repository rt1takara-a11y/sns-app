package com.example.snsapp;

// ...既存のimport文...
import org.springframework.security.core.Authentication;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 追加したimport（コンパイルに必要）
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;

@RestController
@RequestMapping("/api/posts") // PostControllerは/api/postsを担当
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository; // UserRepositoryを追加
    private final JwtUtil jwtUtil; // フォールバック用に再追加

    // コンストラクタを修正：JwtUtil を受け取る
    public PostController(PostRepository postRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // 変更: Postエンティティを直接返さず、表示用DTOに変換して返す
    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
            .map(p -> new PostResponse(
                p.getId(),
                // フィールド名は実装に合わせて調整してください（例: getContent()）
                // nullチェックを入れて安全に取得
                p.getContent(),
                p.getUser() != null ? p.getUser().getUsername() : null,
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null
            ))
            .collect(Collectors.toList());
    }

    // ▼▼▼ createPostメソッドを修正 ▼▼▼
    // 変更: createPost は Authentication のみを優先し、なければ Authorization ヘッダーからフォールバック
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post newPost,
                                        Authentication authentication,
                                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 追加: 受信状況をログに出す（デバッグ用）
        boolean authPresent = (authentication != null && authentication.isAuthenticated());
        logger.debug("createPost called: authPresent={}, principal={}, authorizationHeaderPresent={}",
                     authPresent,
                     authPresent ? authentication.getName() : null,
                     authorizationHeader != null);

        String username = null;
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
            logger.info("createPost: authenticated via SecurityContext, user={}", username);
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(token);
                logger.info("createPost: authenticated via Authorization header fallback, user={}", username);
            } catch (Exception e) {
                logger.warn("createPost: invalid token in Authorization header", e);
                return ResponseEntity.status(401).body("無効なトークンです。/api/posts/debug/auth-header でヘッダー到達状況を確認してください。");
            }
        } else {
            logger.warn("createPost: no authentication provided; advise calling /api/posts/debug/auth-header to inspect headers");
            return ResponseEntity.status(401).body("ログインしていません。フロントが Authorization ヘッダーを送っているか /api/posts/debug/auth-header で確認してください。");
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("ユーザーが見つかりません。");
        }

        User user = userOptional.get();
        newPost.setUser(user); // 投稿にユーザー情報をセット

        Post saved = postRepository.save(newPost);

        PostResponse response = new PostResponse(
            saved.getId(),
            saved.getContent(),
            saved.getUser() != null ? saved.getUser().getUsername() : null,
            saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null
        );

        return ResponseEntity.ok(response);
    }

    // 追加: 投稿削除エンドポイント
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            return ResponseEntity.status(404).body("投稿が見つかりません。");
        }

        Post post = postOpt.get();

        // 認証チェック
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("ログインしてください。");
        }

        String username = authentication.getName();
        // 投稿者のみ削除可能にする（投稿の user が null の場合は拒否）
        if (post.getUser() == null || !username.equals(post.getUser().getUsername())) {
            return ResponseEntity.status(403).body("削除権限がありません。");
        }

        postRepository.delete(post);
        return ResponseEntity.ok().body("deleted");
    }

    // デバッグ用: Authorization ヘッダーと SecurityContext の状態を返す（開発時のみ使用）
    @GetMapping("/debug/auth-header")
    public ResponseEntity<?> debugAuthHeader(Authentication authentication,
                                             @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        boolean authPresent = (authentication != null && authentication.isAuthenticated());
        String username = authPresent ? authentication.getName() : null;

        // トークンをそのまま返すのはセキュリティ上好ましくないためマスクして返す
        String maskedToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            int len = Math.min(8, token.length());
            maskedToken = token.substring(0, len) + "..." + (token.length() > len ? "(len=" + token.length() + ")" : "");
        }

        // 簡易ログ（サーバ側での確認用）
        logger.debug("debugAuthHeader: authPresent={}, username={}, authorizationHeaderPresent={}", authPresent, username, authorizationHeader != null);

        return ResponseEntity.ok(Map.of(
            "securityContextAuthenticated", authPresent,
            "username", username,
            "authorizationHeaderPresent", authorizationHeader != null,
            "authorizationHeaderMasked", maskedToken
        ));
    }

    // 表示用DTO（Java 21 の record を利用）
    public record PostResponse(Long id, String content, String username, String createdAt) {}
}