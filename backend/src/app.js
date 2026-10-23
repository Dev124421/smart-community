//const express = require("express");
//const cors = require("cors");
//const helmet = require("helmet");
//const authRoutes = require("./routes/authRoutes");
//
//const app = express();
//
//
//
//app.use(helmet());
//app.use(cors());
//app.use(express.json());
//
//
//
//app.get("/", (req, res) => {
//    res.status(200).json({
//        success: true,
//        message: "Smart-Community API is running"
//     });
//
//});
//
//
//app.get("/health", (req, res) => {
//    res.status(200).json({
//        success: true,
//        message: "running, OK"
//     });
//
//});
//
//
//// https://smart.onrender.com/health
//
//const communityRoutes = require("./routes/communityRoutes");
//app.use("/api/communities", communityRoutes);
//
//// Authentication routes
//app.use("/api/auth", authRoutes);
//
//module.exports = app;



const express = require("express");
const app = express();

// Authentication routes
const authRoutes = require("./routes/authRoutes");


app.use(express.json());

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