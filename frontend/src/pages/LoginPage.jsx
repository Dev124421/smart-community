import React, { useState } from 'react';
import './LoginPage.css'; 

const LoginPage = (props) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault(); 
    console.log("Login Data Submitted: ", { email, password });
    alert("Login Successful! (Check console for data)");
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        
        <div className="left-panel">
          <div className="blob blob-main"></div>
          <div className="blob blob-secondary"></div>
        </div>

        <div className="right-panel">
          <div className="header-section">
            <h1>Welcome Back!</h1>
            <p>
              Log in to your Smart Community account<br />
              and stay connected.
            </p>
          </div>

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <label>Email Address</label>
              <input 
                type="email" 
                placeholder="you@example.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <label>Password</label>
              <input 
                type="password" 
                placeholder="Enter your password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="form-options">
              <label className="remember-me">
                <input type="checkbox" />
                <span>Remember me</span>
              </label>
              <a href="#" className="forgot-password">Forgot password?</a>
            </div>

            <button type="submit" className="btn-primary">Log In</button>
          </form>

          <div className="divider">
            <span>OR</span>
          </div>

          <button type="button" className="btn-google">
            Continue with Google
          </button>

          <p className="signup-text">
            Don't have an account? 
            <a href="#" onClick={(e) => { e.preventDefault(); props.onFormSwitch('signup'); }}>Sign Up</a>
          </p>

        </div>
      </div>
    </div>
  );
};

export default LoginPage;