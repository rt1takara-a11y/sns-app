package com.example.snsapp;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity: このクラスがデータベースのテーブルに対応する「エンティティ」であることを示します。
// Spring Boot(JPA)がこのクラスを見つけて、データベースの設計図として認識します。
@Entity
// @Table: このエンティティが対応するテーブル名を「posts」に指定します。
// これがないとクラス名と同じ「post」テーブルが作られます。
@Table(name = "posts")
public class Post {

    // @Id: このフィールドがテーブルの「主キー」(Primary Key)であることを示します。
    // 主キーは、各データを一意に識別するための番号です。
    @Id
    // @GeneratedValue: 主キーの値をデータベースが自動で生成する方法を指定します。
    // GenerationType.IDENTITYは、新しいデータが追加されるたびに番号が1ずつ増える設定です。
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column: テーブルのカラム(列)に関する設定を行います。
    // nullable = false は、このカラムが空であってはならない(必須項目)ことを意味します。
    // columnDefinition = "TEXT" は、このカラムのデータ型をTEXT型(長い文章を保存できる)に指定します。
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // @CreationTimestamp: このエンティティが新しく作成(保存)される時に、
    // 現在の日時を自動的にこのフィールドに設定してくれます。
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Getters and Setters ---
    // privateなフィールドに外部からアクセスするための決まり文句(お作法)です。
    // Spring Bootが内部でこれらのメソッドを使って値の読み書きを行います。

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}