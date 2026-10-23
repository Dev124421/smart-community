const jwt = require("jsonwebtoken");

const authMiddleware = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        // Check whether the access token was provided
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return res.status(401).json({
                success: false,
                message: "Access token is required"
            });
        }

        // Extract the token from the Authorization header
        const token = authHeader.split(" ")[1];

        
        const decoded = jwt.verify(
            token,
            process.env.ACCESS_TOKEN_SECRET,
            {
                algorithms: ["HS256"],
                issuer: "smart-community",
                audience: "smart-community-client"
            }
        );

        // Ensure this is an access token, not a refresh token
        if (decoded.tokenType !== "access") {
            return res.status(401).json({
                success: false,
                message: "Invalid access token"
            });
        }

        // Make the authenticated user's ID available to the next handler
        req.user = {
            id: decoded.sub
        };

        next();

    } catch (error) {
        return res.status(401).json({
            success: false,
            message: "Invalid or expired access token"
        });
    }
};

module.exports = authMiddleware;