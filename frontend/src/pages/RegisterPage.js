import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function RegisterPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [birthdate, setBirthdate] = useState(''); // 追加
    const [bio, setBio] = useState(''); // 追加
    const [avatarFile, setAvatarFile] = useState(null); // 追加
    const [avatarPreview, setAvatarPreview] = useState(null); // 追加
    const [isSubmitting, setIsSubmitting] = useState(false); // 追加
    const navigate = useNavigate();

    useEffect(() => {
        // プレビュー用オブジェクトURLのクリーンアップ
        return () => {
            if (avatarPreview) URL.revokeObjectURL(avatarPreview);
        };
    }, [avatarPreview]);

    const handleAvatarChange = (e) => {
        const file = e.target.files && e.target.files[0];
        if (file) {
            if (avatarPreview) URL.revokeObjectURL(avatarPreview);
            setAvatarFile(file);
            setAvatarPreview(URL.createObjectURL(file));
        } else {
            setAvatarFile(null);
            setAvatarPreview(null);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            let response;
            if (avatarFile) {
                // 画像を含む場合は FormData で送信
                const form = new FormData();
                form.append('username', username);
                form.append('password', password);
                form.append('birthdate', birthdate);
                form.append('bio', bio);
                form.append('avatar', avatarFile);
                response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    body: form,
                    credentials: 'include' // 追加: クッキー送受信用
                });
            } else {
                // 画像なしは JSON で送信
                response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password, birthdate, bio }),
                    credentials: 'include' // 追加: クッキー送受信用
                });
            }

            if (response.ok) {
                alert('登録が完了しました。ログインしてください。');
                navigate('/login');
            } else {
                // エラーボディを取得して詳細を表示
                let detail = '';
                try {
                    const ct = response.headers.get('content-type') || '';
                    if (ct.includes('application/json')) {
                        const json = await response.json();
                        detail = JSON.stringify(json);
                    } else {
                        detail = await response.text();
                    }
                } catch (err) {
                    detail = 'レスポンス解析エラー';
                }
                alert(`登録に失敗しました (status: ${response.status}). ${detail ? '詳細: ' + detail : ''}`);
            }
        } catch (err) {
            alert('エラーが発生しました。' + (err && err.message ? ` (${err.message})` : ''));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="home-page">
            <div className="container">
                <div className="auth-card">
                    <h2>新規登録</h2>
                    <form onSubmit={handleRegister}>
                        <label className="avatar-upload">
                            <input type="file" accept="image/*" onChange={handleAvatarChange} />
                            <div className="avatar-preview">
                                {avatarPreview ? (
                                    <img src={avatarPreview} alt="avatar preview" />
                                ) : (
                                    <div className="avatar-placeholder">アイコン</div>
                                )}
                            </div>
                            <div className="avatar-help">プロフィール画像（任意）</div>
                        </label>

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

                        <div>
                            <input
                                type="date"
                                value={birthdate}
                                onChange={(e) => setBirthdate(e.target.value)}
                                placeholder="生年月日"
                            />
                        </div>

                        <div>
                            <textarea
                                value={bio}
                                onChange={(e) => setBio(e.target.value)}
                                placeholder="自由記入欄（自己紹介など）"
                                rows={4}
                            />
                        </div>

                        <button type="submit" disabled={isSubmitting}>{isSubmitting ? '登録中...' : '登録'}</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;