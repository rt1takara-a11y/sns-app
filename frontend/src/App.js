import React, { useContext } from 'react'; // useContextをインポート
import { Routes, Route, Link } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import { AuthContext } from './context/AuthContext'; // AuthContextをインポート
import './App.css';

function App() {
  const { token, logout } = useContext(AuthContext); // AuthContextからtokenとlogout関数を取得

  return (
    <div className="App">
      <nav className="topbar">
        <Link to="/">ホーム</Link>
        {token ? (
          <>
            <button onClick={logout} className="logout-button">ログアウト</button>
          </>
        ) : (
          <>
            <Link to="/login">ログイン</Link>
            <Link to="/register">登録</Link>
          </>
        )}
      </nav>
      <div className="container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;