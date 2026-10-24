import { useState } from 'react';
import './SignupPage.css';

// Exact asset names as per your folder image
import logo from "../assets/logo.png";
import logoName from "../assets/logo name.png";
import vectorUpper from "../assets/Vector.png";
import vectorLower from "../assets/Vector (1).png";
import characterSignup from "../assets/Group 35454.png";
import whiteWave from "../assets/White wave.png";


const SignupPage = (props) => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = () => {
    const newErrors = {};

    if (!fullName.trim()) {
      newErrors.fullName = "Full name is required";
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      newErrors.email = "Email address is required";
    } else if (!emailRegex.test(email.trim())) {
      newErrors.email = "Please enter a valid email address";
    }

    if (!password) {
      newErrors.password = "Password is required";
    } else if (password.length < 6) {
      newErrors.password = "Password must be at least 6 characters";
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = "Confirm password is required";
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting) return; // Prevent duplicate submissions

    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setErrors({});
    setIsSubmitting(true);

    try {
      console.log("Signup Data Submitted: ", { fullName, email, password });

      // Attempt registration using existing authService if connected
      /*
      if (authService && typeof authService.register === 'function') {
        try {
          const data = await authService.register({ fullName, email, password });
          console.log("Registration API Response: ", data);
        } catch (apiErr) {
          console.warn("Backend registration endpoint not live yet:", apiErr);
        }
      }
      */

      alert("Account created successfully! Please verify your email before logging in.");

      if (props.onFormSwitch) {
        props.onFormSwitch('login');
      }
    } catch (err) {
      console.error("Signup error: ", err);
      setErrors({ general: "An error occurred during registration. Please try again." });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSocialLogin = (provider) => {
    console.log(`Social Login initiated: ${provider}`);
  };

  return (
    <div className="signup-wrapper">
      {/* Brand logo & name at top-left corner */}
      <div
        className="signup-top-logo"
        onClick={() => props.onFormSwitch && props.onFormSwitch('dashboard')}
        role="button"
        tabIndex={0}
        aria-label="Go to Dashboard"
      >
        <img src={logo} alt="Smart Community Logo" className="logo-icon" />
        <img src={logoName} alt="Yappy" className="logo-name" />
      </div>

      {/* Centered White Signup Card */}
      <div className="signup-card">
        <div className="header-section">
          <h1>Create Your Account</h1>
          <p>Join Yappy and be a part of something bigger</p>
        </div>

        {errors.general && (
          <div className="form-error-banner" role="alert">
            {errors.general}
          </div>
        )}

        <form className="signup-form" onSubmit={handleSubmit} noValidate>
          {/* 1. Full Name */}
          <div className="input-group">
            <label htmlFor="signup-name">Full Name</label>
            <div className="input-wrapper">
              <input
                id="signup-name"
                type="text"
                placeholder="Enter your full name"
                value={fullName}
                onChange={(e) => {
                  setFullName(e.target.value);
                  if (errors.fullName) setErrors(prev => ({ ...prev, fullName: '' }));
                }}
                className={errors.fullName ? 'input-error' : ''}
                autoComplete="name"
              />
            </div>
            {errors.fullName && <span className="field-error">{errors.fullName}</span>}
          </div>

          {/* 2. Email Address */}
          <div className="input-group">
            <label htmlFor="signup-email">Email Address</label>
            <div className="input-wrapper">
              <input
                id="signup-email"
                type="email"
                placeholder="Enter your email address"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (errors.email) setErrors(prev => ({ ...prev, email: '' }));
                }}
                className={errors.email ? 'input-error' : ''}
                autoComplete="email"
              />
            </div>
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>

          {/* 3. Create Password */}
          <div className="input-group">
            <label htmlFor="signup-password">Create Password</label>
            <div className="input-wrapper password-wrapper">
              <input
                id="signup-password"
                type={showPassword ? "text" : "password"}
                placeholder="Create a password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errors.password) setErrors(prev => ({ ...prev, password: '' }));
                }}
                className={errors.password ? 'input-error' : ''}
                autoComplete="new-password"
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <svg className="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#71717a" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                ) : (
                  <svg className="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#71717a" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                )}
              </button>
            </div>
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          {/* 4. Confirm Password */}
          <div className="input-group">
            <label htmlFor="signup-confirm-password">Confirm Password</label>
            <div className="input-wrapper password-wrapper">
              <input
                id="signup-confirm-password"
                type={showConfirmPassword ? "text" : "password"}
                placeholder="Confirm your password"
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value);
                  if (errors.confirmPassword) setErrors(prev => ({ ...prev, confirmPassword: '' }));
                }}
                className={errors.confirmPassword ? 'input-error' : ''}
                autoComplete="new-password"
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                aria-label={showConfirmPassword ? "Hide password" : "Show password"}
              >
                {showConfirmPassword ? (
                  <svg className="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#71717a" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                ) : (
                  <svg className="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#71717a" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                )}
              </button>
            </div>
            {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
          </div>

          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Creating Account..." : "Create Account \u2192"}
          </button>
        </form>

        <div className="divider">
          <span>Or continue with</span>
        </div>

        {/* Google and Apple sign-in icons */}
        <div className="social-login-group">
          <button
            type="button"
            className="btn-social"
            onClick={() => handleSocialLogin('Google')}
            title="Continue with Google"
            aria-label="Continue with Google"
          >
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z" />
              <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.25v3.15C3.26 21.36 7.34 24 12 24z" />
              <path fill="#FBBC05" d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.13-1.55.38-2.27V6.58H1.25C.45 8.18 0 9.99 0 12s.45 3.82 1.25 5.42l4.03-3.15z" />
              <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.34 0 3.26 2.64 1.25 6.58l4.03 3.15c.95-2.83 3.6-4.98 6.72-4.98z" />
            </svg>
          </button>
          <button
            type="button"
            className="btn-social"
            onClick={() => handleSocialLogin('Apple')}
            title="Continue with Apple"
            aria-label="Continue with Apple"
          >
            <svg viewBox="0 0 170 170" width="18" height="18" fill="#000000">
              <path d="M150.37 130.25c-2.45 5.66-5.35 10.87-8.71 15.66-4.58 6.53-8.33 11.05-11.22 13.56-4.48 4.12-9.28 6.23-14.42 6.35-3.69 0-8.14-1.05-13.32-3.18-5.19-2.12-9.97-3.17-14.34-3.17-4.58 0-9.49 1.05-14.75 3.17-5.26 2.13-9.5 3.24-12.74 3.35-4.35.13-9.16-1.9-14.42-6.08-3.7-3.04-7.59-7.71-11.66-14.01-6.19-9.56-11.08-20.91-14.67-34.05-3.59-13.14-5.38-25.79-5.38-37.95 0-14.12 3.37-26.06 10.11-35.81 6.74-9.76 15.64-14.7 26.7-14.82 4.9 0 10.3 1.25 16.2 3.75 5.9 2.5 9.77 3.75 11.6 3.75 1.62 0 5.66-1.35 12.12-4.05 6.46-2.7 11.97-3.9 16.53-3.6 12.38.6 22.4 4.8 30.07 12.6-10.74 6.5-16.01 15.5-15.81 27 .2 9.5 3.9 17.5 11.1 24 7.2 6.5 15.8 10.1 25.8 10.8-2.2 6.6-4.9 13.5-8.1 20.7zm-29.23-102.3c0-6.8 2.5-13.2 7.5-19.2 5-6 11.3-9.8 18.9-11.4.3 1.3.4 2.5.4 3.6 0 6.7-2.6 13.2-7.8 19.5-5.2 6.3-11.4 10.1-18.6 11.4-.2-1.3-.4-2.6-.4-3.9z" />
            </svg>
          </button>
        </div>

        <p className="login-text">
          Already have an account?{' '}
          <a href="#" onClick={(e) => { e.preventDefault(); props.onFormSwitch('login'); }}>
            Log in
          </a>
        </p>
      </div>

      {/* Layered grey and white waves with character mascot */}
      <div className="waves-container">
        {/* Layer 1: Upper grey wave  */}
        <img src={vectorUpper} alt="" className="wave-layer wave-upper" />

        {/* Layer 2: Remaining grey wave layer */}
        <img src={vectorLower} alt="" className="wave-layer wave-middle" />

        {/* Layer 3: Mascot character positioned between waves */}
        <img src={characterSignup} alt="Mascot" className="wave-character" />

        {/* Layer 4: Bottom white wave */}
        <img src={whiteWave} alt="" className="wave-layer wave-bottom" />
      </div>
    </div>
  );
};

export default SignupPage;