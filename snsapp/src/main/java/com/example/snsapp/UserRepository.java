package com.example.snsapp;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // ユーザー名でユーザーを検索するためのメソッドを追加
    Optional<User> findByUsername(String username);
}
