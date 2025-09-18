import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import '../App.css';

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                login(data.token);
                navigate('/');
            } else {
                alert('ログインに失敗しました。ユーザー名またはパスワードを確認してください。');
            }
        } catch (error) {
            alert('エラーが発生しました。');
        }
    };

    return (
        <div className="home-page">
            <div className="container">
                <div className="auth-card">
                    <h2>ログイン</h2>
                    <form onSubmit={handleLogin}>
                        <div>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="ユーザー名"
                                required
                            />
                        </div>
                        <div>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="パスワード"
                                required
                            />
                        </div>
                        <button type="submit">ログイン</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;