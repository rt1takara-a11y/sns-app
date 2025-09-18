package com.example.snsapp;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users") // テーブル名を "users" に指定
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 空ではなく、重複も許さない
    private String username;

    @Column(nullable = false)
    private String password;

    // 追加フィールド: アイコン画像 URL（nullable）
    @Column(name = "avatar_url", length = 1024)
    private String iconUrl;

    // 追加フィールド: 生年月日
    @Column(name = "birthdate")
    private LocalDate birthdate;

    // 追加フィールド: 自己紹介
    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    // 追加: AuthController が使用している命名(getBirthday/setBirthday)のエイリアス
    public LocalDate getBirthday() {
        return this.birthdate;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthdate = birthday;
    }
}