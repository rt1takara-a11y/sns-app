package com.example.snsapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
// 開発用にフロントのオリジンを許可（本番では厳格に制限してください）
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // 登録用リクエスト（POJO に変更、avatarUrl は /api/uploads の結果を渡す想定）
    public static class RegisterRequest {
        private String username;
        private String password;
        private String avatarUrl;
        private String birthday; // ISO: YYYY-MM-DD を想定
        private String bio;

        public RegisterRequest() {}
        // getters / setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getBirthday() { return birthday; }
        public void setBirthday(String birthday) { this.birthday = birthday; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
    }

    // 登録時にユーザー情報（id, username, avatarUrl, birthdate, bio）を返す POJO
    public static class RegisterResponse {
        private Long id;
        private String username;
        private String avatarUrl;
        private String birthdate;
        private String bio;

        public RegisterResponse() {}
        public RegisterResponse(Long id, String username, String avatarUrl, String birthdate, String bio) {
            this.id = id;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.birthdate = birthdate;
            this.bio = bio;
        }
        // getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getBirthdate() { return birthdate; }
        public String getBio() { return bio; }
    }

    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        logger.info("POST /api/auth/register called, username={}", req.getUsername());

        // 基本バリデーション
        if (req.getUsername() == null || req.getUsername().isBlank() || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username と password は必須です"));
        }

        Optional<User> existing = userRepository.findByUsername(req.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "ユーザー名は既に使われています"));
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setIconUrl(req.getAvatarUrl()); // DB のカラム名は avatar_url / entity は iconUrl

        if (req.getBirthday() != null && !req.getBirthday().isBlank()) {
            try {
                user.setBirthday(LocalDate.parse(req.getBirthday()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "birthday は YYYY-MM-DD 形式で送ってください"));
            }
        }

        // 長すぎる bio は切る / またはバリデーションする（ここでは最大2000と仮定）
        if (req.getBio() != null && req.getBio().length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("error", "bio は最大2000文字です"));
        }
        user.setBio(req.getBio());

        User saved = userRepository.save(user);

        RegisterResponse resp = new RegisterResponse(
            saved.getId(),
            saved.getUsername(),
            saved.getIconUrl(),
            saved.getBirthday() != null ? saved.getBirthday().toString() : null,
            saved.getBio()
        );

        logger.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            // セッションを作成（クッキーでJSESSIONIDが返る）
            request.getSession(true);
            return ResponseEntity.ok(Map.of("message", "ログイン成功"));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "認証に失敗しました"));
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        // getters / setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
