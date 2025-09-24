import React, { useState, useEffect } from 'react';
// App.cssを共有して使うため、パスを修正してインポート
import '../App.css';
import { api } from '../api';

function HomePage() {
    const [posts, setPosts] = useState([]);
    // 投稿フォームの全項目をまとめて管理するstate
    const [newPost, setNewPost] = useState({
        title: '',
        projectSummary: '',
        problemStatement: '',
        content: '', // contentは「実施したこと・解決策」として使用
        lessonsLearned: ''
    });

    // フォームの入力値をまとめて処理する関数
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewPost(prevState => ({
            ...prevState,
            [name]: value
        }));
    };
    useEffect(() => {
        fetchPosts();
    }, []);

    // 投稿データからアバターURLを安全に取得（複数のキーに対応）
    const getAvatarUrl = (post) => {
        return post.avatarUrl
            || post.avatar
            || post.avatar_url
            || (post.author && (post.author.avatarUrl || post.author.avatar || post.author.avatar_url))
            || (post.user && (post.user.avatarUrl || post.user.avatar || post.user.avatar_url))
            || null;
    };

    // 投稿者名取得（既存ユーティリティがあればそれを利用）
    const getAuthorName = (post) => {
        return post.username
            || (post.author && (post.author.username || post.author.name))
            || (post.user && (post.user.username || post.user.name))
            || post.name
            || '匿名';
    };

    // ユーザー名からイニシャルを作る（プレースホルダ表示用）
    const getInitials = (name) => {
        if (!name) return '';
        const parts = name.trim().split(/\s+/);
        if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    };

    // ユーティリティ：日時をパースして見やすい形式に変換
    const formatDate = (post) => {
        const raw = post.createdAt || post.created_at || post.timestamp || post.time || post.date;
        if (!raw) return '';
        const d = new Date(raw);
        if (isNaN(d.getTime())) return String(raw);
        return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    };

    const fetchPosts = () => {
        api.getPosts()
            .then(response => {
                if (!response.ok) {
                    throw new Error('投稿の取得に失敗しました。');
                }
                return response.json();
            })
            .then(data => setPosts(data.sort((a, b) => b.id - a.id)))
            .catch(error => {
                console.error('投稿取得エラー:', error);
            });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        api.createPost(newPost)
            .then(response => {
                if (!response.ok) {
                    throw new Error('投稿に失敗しました。ログインしているか確認してください。');
                }
                return response.json();
            })
            .then(() => {
                // フォーム全体をリセット
                setNewPost({
                    title: '',
                    projectSummary: '',
                    problemStatement: '',
                    content: '',
                    lessonsLearned: ''
                });
                fetchPosts();
            })
            .catch(error => {
                alert(error.message);
            });
    };

const handleDelete = (id) => {
        api.deletePost(id)
            .then((response) => {
                if (!response.ok) {
                    throw new Error('削除に失敗しました。権限を確認してください。');
                }
                fetchPosts();
            })
            .catch(error => {
                alert(error.message);
            });
    };

    return (
        <div className="home-page">
            <div className="container">
                <h1>社内SNS</h1>
                <form onSubmit={handleSubmit} className="compose-form">
                    <input
                        type="text"
                        name="title"
                        value={newPost.title}
                        onChange={handleInputChange}
                        placeholder="タイトル *"
                        required
                    />
                    <textarea
                        name="projectSummary"
                        value={newPost.projectSummary}
                        onChange={handleInputChange}
                        placeholder="プロジェクト概要"
                    />
                    <textarea
                        name="problemStatement"
                        value={newPost.problemStatement}
                        onChange={handleInputChange}
                        placeholder="課題・目的"
                    />
                    <textarea
                        name="content"
                        value={newPost.content}
                        onChange={handleInputChange}
                        placeholder="実施したこと・解決策 *"
                        required
                    />
                    <textarea
                        name="lessonsLearned"
                        value={newPost.lessonsLearned}
                        onChange={handleInputChange}
                        placeholder="学んだこと・反省点"
                    />
                    <button type="submit">投稿</button>
                </form>

                <ul className="post-list">
                    {posts.map(post => {
                        const author = getAuthorName(post);
                        const avatarUrl = getAvatarUrl(post); 
                        const initials = getInitials(author); 
                        return (
                            <li key={post.id} className="post-item structured">
                                <div className="post-header">
                                    {/* ▼▼▼ ここからが追加部分 ▼▼▼ */}
                                    <div className="post-avatar-wrapper">
                                        {avatarUrl ? (
                                            <img
                                                src={avatarUrl}
                                                alt={`${author} のアイコン`}
                                                className="post-avatar"
                                            />
                                        ) : (
                                            <div className="post-avatar placeholder" aria-hidden>{initials}</div>
                                        )}
                                    </div>
                                    {/* ▲▲▲ ここまでが追加部分 ▲▲▲ */}

                                    <div className="post-title-meta">
                                        <h3>{post.title}</h3>
                                        <div className="post-meta">
                                            <span className="post-author">{author}</span>
                                            <span className="post-date">・{formatDate(post)}</span>
                                        </div>
                                    </div>
                                </div>
                                <div className="post-body">
                                    {post.projectSummary &&
                                        <section>
                                            <h4>プロジェクト概要</h4>
                                            <p>{post.projectSummary}</p>
                                        </section>
                                    }
                                    {post.problemStatement &&
                                        <section>
                                            <h4>課題・目的</h4>
                                            <p>{post.problemStatement}</p>
                                        </section>
                                    }
                                    {post.content &&
                                        <section>
                                            <h4>実施したこと・解決策</h4>
                                            <p>{post.content}</p>
                                        </section>
                                    }
                                    {post.lessonsLearned &&
                                        <section>
                                            <h4>学んだこと・反省点</h4>
                                            <p>{post.lessonsLearned}</p>
                                        </section>
                                    }
                                </div>
                                <div className="post-actions">
                                    <button onClick={() => handleDelete(post.id)} className="delete-button" aria-label={`削除 ${post.id}`}>削除</button>
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </div>
        </div>
    );
}

// ファイルの最後で必ずエクスポートする
export default HomePage;