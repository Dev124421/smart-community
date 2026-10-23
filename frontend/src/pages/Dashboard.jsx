import React from 'react';
import './Dashboard.css';

const Dashboard = () => {
  return (
    <div className="dashboard-container">

      <aside className="sidebar">
        <div className="sidebar-logo">
          <span style={{ color: '#fff' }}>❖</span> Nexora
        </div>
        
        <nav className="nav-menu">
          <div className="nav-item active">🏠 Dashboard</div>
          <div className="nav-item">👥 My community</div>
          <div className="nav-item">🧭 Discover</div>
          <div className="nav-item">➕ Create Community</div>
          <div className="nav-item">🔔 Notifications</div>
          <div className="nav-item">💬 Messages</div>
          <div className="nav-item">⚙️ Settings</div>
        </nav>
        <div className="sidebar-profile">
          <div className="avatar">U</div>
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>User</div>
            <div style={{ color: '#A3AED0', fontSize: '0.75rem' }}>B.Tech 3rd Sem</div>
          </div>
        </div>
      </aside>

      <main className="main-area">
        
        <header className="top-nav">
          <div className="search-bar">
            <span>🔍</span>
            <input type="text" placeholder="Search communities, members topics..." />
          </div>
          <div className="top-nav-actions">
            <span style={{ cursor: 'pointer' }}>🔔</span>
            <div className="sidebar-profile" style={{ border: 'none', padding: 0, marginTop: 0, color: '#1E234C' }}>
              <div className="avatar">U</div>
              <div>
                <div style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>User</div>
                <div style={{ color: '#8F9BBA', fontSize: '0.75rem' }}>B.Tech 3rd Sem</div>
              </div>
            </div>
          </div>
        </header>

        <div className="content-wrapper">
          
         
          <div className="left-column">
            
            <div className="welcome-banner">
              <h1>Good Morning, User!</h1>
              <p>Explore more communities, stay updated and be a part of meaningful discussions.</p>
            </div>

            {/* MY COMMUNITY SECTION */}
            <div>
              <div className="section-header">
                <h2>My community</h2>
                <a href="#" className="view-all">View All →</a>
              </div>
              <div className="card-grid">
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#E6E0FF', color: '#604AB9' }}>&lt;/&gt;</div>
                  <h3 className="card-title">java developers</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 58 members</span>
                    <button className="badge">Member</button>
                  </div>
                </div>
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#E0F5E9', color: '#2FA84B' }}>🍃</div>
                  <h3 className="card-title">web development</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 98 members</span>
                    <button className="badge">Member</button>
                  </div>
                </div>
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#E6F4FF', color: '#0070F3' }}>👥</div>
                  <h3 className="card-title">tech enthusiasts</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 58 members</span>
                    <button className="badge">Member</button>
                  </div>
                </div>
              </div>
            </div>

            {/* DISCOVER COMMUNITIES SECTION */}
            <div>
              <div className="section-header">
                <h2>Discover Communities</h2>
                <a href="#" className="view-all">View All →</a>
              </div>
              <div className="card-grid">
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#FFE5EC', color: '#E53E3E' }}>🧠</div>
                  <h3 className="card-title">AI & machine learning</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 58 members</span>
                    <button className="badge join">Join</button>
                  </div>
                </div>
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#FFF4E5', color: '#DD6B20' }}>✏️</div>
                  <h3 className="card-title">UI/UX design</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 98 members</span>
                    <button className="badge join">Join</button>
                  </div>
                </div>
                <div className="community-card">
                  <div className="card-icon" style={{ background: '#E6FFFA', color: '#319795' }}>&lt;/&gt;</div>
                  <h3 className="card-title">competitive programming</h3>
                  <div className="card-footer">
                    <span className="member-count">👥 58 members</span>
                    <button className="badge join">Join</button>
                  </div>
                </div>
              </div>
            </div>

          </div>

          <div className="right-column">
            
            {/* QUICK ACTIONS */}
            <div className="widget">
              <h2 className="section-header">Quick Actions</h2>
              <button className="btn-action btn-join">+ Join Community</button>
              <button className="btn-action btn-create">+ Create Community</button>
            </div>

            {/* COMMUNITY STATS */}
            <div className="widget">
              <div className="section-header">
                <h2>Community Stats</h2>
                <a href="#" className="view-all">View All →</a>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                  <span style={{ color: '#8F9BBA' }}>Total Communities</span>
                  <span style={{ fontWeight: 'bold', color: '#1E234C' }}>5</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                  <span style={{ color: '#8F9BBA' }}>Total Members</span>
                  <span style={{ fontWeight: 'bold', color: '#1E234C' }}>452</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', alignItems: 'center' }}>
                  <span style={{ color: '#8F9BBA' }}>Your role</span>
                  <button className="badge">Member</button>
                </div>
              </div>
            </div>

          </div>

        </div>
      </main>
    </div>
  );
};

export default Dashboard;