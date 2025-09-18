import React, { useState, useEffect } from 'react';
// App.cssを共有して使うため、パスを修正してインポート
import '../App.css';

function HomePage() {
  const [posts, setPosts] = useState([]);
  const [content, setContent] = useState('');

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
    if (parts.length === 1) return parts[0].slice(0,2).toUpperCase();
    return (parts[0][0] + parts[parts.length-1][0]).toUpperCase();
  };

  // ユーティリティ：日時をパースして見やすい形式に変換
  const formatDate = (post) => {
    const raw = post.createdAt || post.created_at || post.timestamp || post.time || post.date;
    if (!raw) return '';
    const d = new Date(raw);
    if (isNaN(d.getTime())) return String(raw);
    return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  };

  const fetchPosts = () => {
    fetch('http://localhost:8080/api/posts')
      .then(response => response.json())
      .then(data => setPosts(data.sort((a, b) => b.id - a.id)));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    fetch('http://localhost:8080/api/posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ content: content }),
      credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('投稿に失敗しました。ログインしているか確認してください。');
        }
        return response.json();
    })
    .then(() => {
      setContent('');
      fetchPosts();
    })
    .catch(error => {
        alert(error.message);
    });
  };

  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/posts/${id}`, {
      method: 'DELETE',
    })
    .then(() => {
      fetchPosts();
    });
  };

  return (
    <div className="home-page">
      <div className="container">
        <h1>社内SNS</h1>
        <form onSubmit={handleSubmit} className="compose">
          <input
            type="text"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="いまどうしてる？"
          />
          <button type="submit">投稿</button>
        </form>

        <ul className="post-list">
          {posts.map(post => {
            const avatarUrl = getAvatarUrl(post);
            const author = getAuthorName(post);
            const initials = getInitials(author);
            return (
              <li key={post.id} className="post-item">
                <div className="post-main">
                  {avatarUrl ? (
                    <img
                      src={avatarUrl}
                      alt={`${author} のアイコン`}
                      className="post-avatar"
                      onError={(e) => { e.currentTarget.style.display = 'none'; }}
                    />
                  ) : (
                    <div className="post-avatar placeholder" aria-hidden>{initials}</div>
                  )}

                  <div className="post-left">
                    <span className="post-content">{post.content}</span>
                    <div className="post-meta">
                      <span className="post-author">{author}</span>
                      {formatDate(post) && <span className="post-date">・{formatDate(post)}</span>}
                    </div>
                  </div>
                </div>

                <button onClick={() => handleDelete(post.id)} className="delete-button" aria-label={`削除 ${post.id}`}>削除</button>
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