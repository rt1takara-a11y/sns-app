import React, { createContext, useState, useEffect } from 'react';

// 1. Contextを作成
export const AuthContext = createContext();

// 2. Contextを提供するコンポーネントを作成
export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(null);

    // アプリ起動時にローカルストレージからトークンを読み込む
    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            setToken(storedToken);
        }
    }, []);

    // ログイン処理
    const login = (newToken) => {
        setToken(newToken);
        localStorage.setItem('token', newToken);
    };

    // ログアウト処理
    const logout = () => {
        setToken(null);
        localStorage.removeItem('token');
    };

    // 3. Contextの値を子コンポーネントに渡す
    return (
        <AuthContext.Provider value={{ token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};