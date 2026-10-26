const jwt = require("jsonwebtoken");

const generateAccessToken = (userId) => {
    return jwt.sign(
        {
            sub: userId.toString(),
            tokenType: "access"
        },
        process.env.ACCESS_TOKEN_SECRET,
        {
            algorithm: "HS256",
            expiresIn: "15m",
            issuer: "smart-community",
            audience: "smart-community-client"
        }
    );
};

const generateRefreshToken = (userId, sessionId) => {
    return jwt.sign(
        {
            sub: userId.toString(),
            sid: sessionId,
            tokenType: "refresh"
        },
        process.env.REFRESH_TOKEN_SECRET,
        {
            algorithm: "HS256",
            expiresIn: "7d",
            issuer: "smart-community",
            audience: "smart-community-client"
        }
    );
};

module.exports = {
    generateAccessToken,
    generateRefreshToken
};