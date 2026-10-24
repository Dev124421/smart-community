import { useState } from 'react';
import './ForgotPasswordPage.css';

// Exact asset names
import logo from "../assets/logo.png";
import logoName from "../assets/logo name.png";
import vectorUpper from "../assets/Vector.png";
import vectorLower from "../assets/Vector (1).png";
import characterForgot from "../assets/Group 35450.png";
import whiteWave from "../assets/White wave.png";

const ForgotPasswordPage = (props) => {
    const [email, setEmail] = useState('');
    const [showModal, setShowModal] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!email) return;

        // Yahan API call lagani hogi password reset ki
        console.log("Reset link sent to: ", email);

        // Modal open karna (Success popup)
        setShowModal(true);
    };

    const handleSocialLogin = (provider) => {
        console.log(`Social Login initiated: ${provider}`);
    };

    const closeModal = () => {
        setShowModal(false);
        if (props.onFormSwitch) {
            props.onFormSwitch('login'); // Modal close hone par login page par bhej do
        }
    };

    return (
        <div className="forgot-wrapper">
            {/* Brand logo & name at top-left corner */}
            <div
                className="top-logo"
                onClick={() => props.onFormSwitch && props.onFormSwitch('login')}
                role="button"
                tabIndex={0}
            >
                <img src={logo} alt="Smart Community Logo" className="logo-icon" />
                <img src={logoName} alt="Yappy" className="logo-name" />
            </div>

            {/* Main Content Area */}
            <div className="content-container">

                {/* Centered White Card */}
                <div className="forgot-card">
                    <div className="header-section">
                        <h1>Forgot Password?</h1>
                    </div>

                    <form className="forgot-form" onSubmit={handleSubmit}>
                        <div className="input-group">
                            <label htmlFor="forgot-email">Email Address</label>
                            <div className="input-wrapper">
                                <input
                                    id="forgot-email"
                                    type="email"
                                    placeholder="Enter your email address"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn-primary">
                            Reset Password
                        </button>
                    </form>

                    <div className="divider">
                        <span>Or continue with</span>
                    </div>

                    {/* Social login buttons: Google and Apple */}
                    <div className="social-login-group">
                        <button
                            type="button"
                            className="btn-social"
                            onClick={() => handleSocialLogin('Google')}
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
                            aria-label="Continue with Apple"
                        >
                            <svg viewBox="0 0 170 170" width="18" height="18" fill="#000000">
                                <path d="M150.37 130.25c-2.45 5.66-5.35 10.87-8.71 15.66-4.58 6.53-8.33 11.05-11.22 13.56-4.48 4.12-9.28 6.23-14.42 6.35-3.69 0-8.14-1.05-13.32-3.18-5.19-2.12-9.97-3.17-14.34-3.17-4.58 0-9.49 1.05-14.75 3.17-5.26 2.13-9.5 3.24-12.74 3.35-4.35.13-9.16-1.9-14.42-6.08-3.7-3.04-7.59-7.71-11.66-14.01-6.19-9.56-11.08-20.91-14.67-34.05-3.59-13.14-5.38-25.79-5.38-37.95 0-14.12 3.37-26.06 10.11-35.81 6.74-9.76 15.64-14.7 26.7-14.82 4.9 0 10.3 1.25 16.2 3.75 5.9 2.5 9.77 3.75 11.6 3.75 1.62 0 5.66-1.35 12.12-4.05 6.46-2.7 11.97-3.9 16.53-3.6 12.38.6 22.4 4.8 30.07 12.6-10.74 6.5-16.01 15.5-15.81 27 .2 9.5 3.9 17.5 11.1 24 7.2 6.5 15.8 10.1 25.8 10.8-2.2 6.6-4.9 13.5-8.1 20.7zm-29.23-102.3c0-6.8 2.5-13.2 7.5-19.2 5-6 11.3-9.8 18.9-11.4.3 1.3.4 2.5.4 3.6 0 6.7-2.6 13.2-7.8 19.5-5.2 6.3-11.4 10.1-18.6 11.4-.2-1.3-.4-2.6-.4-3.9z" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>

            {/* Success Modal Popup */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-box">
                        <button className="close-btn" onClick={closeModal}>✕</button>
                        <h2>Check your email for a login link</h2>
                        <p>Tap the link in your email to sign in instantly - no password needed.</p>
                        <button className="okay-btn" onClick={closeModal}>Okay</button>
                    </div>
                </div>
            )}

            {/* Layered grey and white waves with character mascot */}
            <div className="waves-container">
                <img src={vectorUpper} alt="" className="wave-layer wave-upper" />
                <img src={vectorLower} alt="" className="wave-layer wave-middle" />
                {/* Naya Character */}
                <img src={characterForgot} alt="Mascot" className="wave-character" />
                <img src={whiteWave} alt="" className="wave-layer wave-bottom" />
            </div>
        </div>
    );
};

export default ForgotPasswordPage;