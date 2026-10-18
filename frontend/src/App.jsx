import React, { useState } from 'react';
import LoginPage from './pages/LoginPage'; 
import SignupPage from './pages/SignupPage';

function App() {
  const [currentForm, setCurrentForm]=useState("login");

  const toggleForm =(formName)=>{
    setCurrentForm(formName);
  }

  return (
    <>
      {currentForm === 'login' ? (
        <LoginPage onFormSwitch={toggleForm} />
      ) : (
        <SignupPage onFormSwitch={toggleForm} />
      )}
    </>
  );
}

export default App;