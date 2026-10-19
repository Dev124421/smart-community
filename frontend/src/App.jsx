import React, { useState } from 'react';
import LoginPage from './pages/LoginPage'; 
import SignupPage from './pages/SignupPage';
import Dashboard from './pages/Dashboard';

function App() {
  const [currentForm, setCurrentForm]=useState("dashboard");

  const toggleForm =(formName)=>{
    setCurrentForm(formName);
  }

  return (
    <>
      {currentForm === 'login' && <LoginPage onFormSwitch={toggleForm}/>}
      {currentForm === 'signup' && <SignupPage onFormSwitch={toggleForm} />}
      {currentForm === 'dashboard' && <Dashboard />}
      <button
      style={{
        position:'fixed',
        bottom:20,
        right:20,
        zIndex:1000,
        padding:'10px 15px',
        backgroundColor:'#604AB9',
        color:'white',
        border:'none',
        borderRadius:'8px',
        cursor:'pointer',
        boxShadow:'0 4px 6px rgb(0,0,0,0.1)'
      }}
        onClick={()=>toggleForm(currentForm==="dashboard" ? "login" : "dashboard")}>
          Go to {currentForm==="dashboard" ? 'Login' : 'Dashboard'}
      </button>
    </>
  );
}

export default App;