package com.example.snsapp;

// ...既存のimport文...
import org.springframework.security.core.Authentication; // Authenticationをインポート
import java.util.Optional; // Optionalをインポート

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

@RestController
@RequestMapping("/api/posts") // PostControllerは/api/postsを担当
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository; // UserRepositoryを追加

    // コンストラクタを修正（既存のまま有効）
    public PostController(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
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
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post newPost, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("ログインしていません。");
        }
        
        // 認証情報からユーザー名を取得
        String username = authentication.getName();
        // ユーザー名でデータベースからUserエンティティを検索
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

        // TODO: 将来的には認証済みかつ投稿者のみ削除可能にする。
        // 例:
        // if (authentication == null) return ResponseEntity.status(401).body("ログインしてください。");
        // String username = authentication.getName();
        // if (post.getUser() != null && !username.equals(post.getUser().getUsername()))
        //     return ResponseEntity.status(403).body("削除権限がありません。");

        postRepository.delete(post);
        return ResponseEntity.ok().body("deleted");
    }

    // 表示用DTO（Java 21 の record を利用）
    public record PostResponse(Long id, String content, String username, String createdAt) {}
}