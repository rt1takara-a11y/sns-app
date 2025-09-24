// JWT認証のヘルパー関数と共通API設定

// トークン管理
export const getToken = () => localStorage.getItem('token');

export const setToken = (token) => {
    if (token) {
        localStorage.setItem('token', token);
    } else {
        localStorage.removeItem('token');
    }
};

export const removeToken = () => {
    localStorage.removeItem('token');
};

// 認証付きfetch関数
export const authFetch = (url, options = {}) => {
    const token = getToken();
    
    // デフォルトのヘッダーを設定
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    // トークンがあればAuthorizationヘッダーを追加
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    return fetch(url, {
        ...options,
        headers,
    });
};

// 通常のfetch（認証不要の場合）
export const publicFetch = (url, options = {}) => {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    return fetch(url, {
        ...options,
        headers,
    });
};

// API エンドポイント定数
export const API_BASE_URL = 'http://localhost:8080/api';

// よく使うAPI呼び出し
export const api = {
    // 認証関連
    login: (credentials) => publicFetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        body: JSON.stringify(credentials)
    }),
    
    register: (userData) => publicFetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        body: JSON.stringify(userData)
    }),

    // 投稿関連（認証が必要）
    getPosts: () => authFetch(`${API_BASE_URL}/posts`),
    
    createPost: (postData) => authFetch(`${API_BASE_URL}/posts`, {
        method: 'POST',
        body: JSON.stringify(postData)
    }),
    
    deletePost: (postId) => authFetch(`${API_BASE_URL}/posts/${postId}`, {
        method: 'DELETE'
    })
};