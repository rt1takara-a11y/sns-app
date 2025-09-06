import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [posts, setPosts] = useState([]);
  // 新しい投稿フォームの入力内容を記憶するためのstateを追加
  const [content, setContent] = useState('');

  useEffect(() => {
    fetchPosts();
  }, []);

  // 投稿を取得する処理を関数として独立させる
  const fetchPosts = () => {
    fetch('http://localhost:8080/api/posts')
      .then(response => response.json())
      .then(data => setPosts(data.sort((a, b) => b.id - a.id))); // IDの降順(新しいものが上)に並び替え
  };

  // フォームが送信された時の処理
  const handleSubmit = (e) => {
    e.preventDefault(); // フォーム送信時のデフォルトの画面リロードを防ぐ
    
    // バックエンドにPOSTリクエストを送信
    fetch('http://localhost:8080/api/posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ content: content }), // 入力内容をJSON形式で送信
    })
    .then(() => {
      setContent(''); // フォームを空にする
      fetchPosts();   // 投稿リストを再取得して画面を更新
    });
  };

  // 投稿を削除する処理
  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/posts/${id}`, {
      method: 'DELETE',
    })
    .then(() => {
      fetchPosts(); // 投稿リストを再取得して画面を更新
    });
  };

  return (
    <div className="App">
      <h1>社内SNS</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={content}
          onChange={(e) => setContent(e.target.value)} // 入力に合わせてstateを更新
          placeholder="いまどうしてる？"
        />
        <button type="submit">投稿</button>
      </form>

      <ul className="post-list">
        {posts.map(post => (
          <li key={post.id} className="post-item">
            <span>{post.content}</span>
            {/* 削除ボタンを追加 */}
            <button onClick={() => handleDelete(post.id)} className="delete-button">削除</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;