const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const authRoutes = require("./routes/authRoutes");

const app = express();



app.use(helmet());
app.use(cors());
app.use(express.json());



app.get("/", (req, res) => {
    res.status(200).json({
        success: true,
        message: "Smart Community API is running"
     });

});

// Authentication routes
app.use("/api/auth", authRoutes);

module.exports = app;