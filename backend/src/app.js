const express = require("express");
const app = express();

// 1. Parse incoming JSON requests FIRST
app.use(express.json());

// 2. Import routes
const communityRoutes = require("./routes/communityRoutes");
const authRoutes = require("./routes/authRoutes");

// 3. Register routes AFTER express.json()
app.use("/api/communities", communityRoutes);
app.use("/api/auth", authRoutes);

app.get("/", (req, res) => {
res.status(200).json({
message: "Welcome to the CSI Auth System API"
});
});

app.get("/api/health", (req, res) => {
res.status(200).json({
message: "Backend is healthy"
});
});

module.exports = app;
