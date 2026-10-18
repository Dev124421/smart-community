import React, { useState } from 'react';
import './SignupPage.css';

const SignupPage = (props) => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if(password !== confirmPassword) {
      alert("Passwords do not match!");
      return;
    }
    console.log("Signup Data Submitted: ", { fullName, email, password });
    alert("Signup Successful! (Check console for data)");
  };

  return (
    <div className="signup-wrapper">
      <div className="signup-card">
        
        <div className="left-panel">
        </div>

        <div className="right-panel">
          <div className="header-section">
            <h1>Create Your Account</h1>
            <p>
              Join Smart Community and a part of<br />
              something bigger
            </p>
          </div>

          <form className="signup-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <label>Full Name</label>
              <input 
                type="text" 
                placeholder="Enter your full name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>

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
                placeholder="Create a password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <label>Confirm Password</label>
              <input 
                type="password" 
                placeholder="Confirm your password" 
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn-primary">Sign Up</button>
          </form>

          <div className="divider">
            <span>OR</span>
          </div>

          <button type="button" className="btn-google">
            Continue with Google
          </button>

          <p className="login-text">
            Already have an account? 
            <a href="#" onClick={(e) => { e.preventDefault(); props.onFormSwitch('login'); }}>Log In?</a>
          </p>

        </div>
      </div>
    </div>
  );
};

export default SignupPage;