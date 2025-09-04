package com.example.snsapp;

import org.springframework.data.jpa.repository.JpaRepository;

// public interface ...: これは「インターフェース」と呼ばれる特殊なクラスの設計図です。
// extends JpaRepository<Post, Long>: ここが最も強力な部分です！
// Spring Data JPAが提供する「JpaRepository」を継承するだけで、
// データベース操作に必要な基本的なメソッド(保存、検索、削除など)が
// すべて自動的に使えるようになります。私たちはSQLを1行も書く必要がありません。
//
// <Post, Long>の意味:
// - Post: このリポジトリが扱うエンティティは「Post」クラスです。
// - Long: そのPostエンティティの主キー(id)の型は「Long」です。
public interface PostRepository extends JpaRepository<Post, Long> {
}