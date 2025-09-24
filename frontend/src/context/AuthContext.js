import React, { createContext, useState, useEffect } from 'react';
import { removeToken } from '../api';

// 1. Contextを作成
export const AuthContext = createContext();

// 2. Contextを提供するコンポーネントを作成
export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // アプリ起動時にローカルストレージからトークンを読み込む
    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            setToken(storedToken);
            setIsLoggedIn(true);
        }
    }, []);

    // ログイン処理
    const login = (newToken) => {
        setToken(newToken);
        setIsLoggedIn(true);
        localStorage.setItem('token', newToken);
    };

    // ログアウト処理
    const logout = () => {
        setToken(null);
        setIsLoggedIn(false);
        removeToken(); // api.jsのヘルパーを使用
    };

    // 3. Contextの値を子コンポーネントに渡す
    return (
        <AuthContext.Provider value={{ token, isLoggedIn, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};