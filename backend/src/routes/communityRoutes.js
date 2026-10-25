
const express = require("express");
const router = express.Router();

const {
  getCommunities,
  createCommunity,
  joinCommunity,
} = require("../controllers/communityController");

const authMiddleware = require("../middleware/authMiddleware");

// All routes below require a valid access token.
router.use(authMiddleware);

router.get("/", getCommunities);
router.post("/", createCommunity);
router.post("/:id/join", joinCommunity);

module.exports = router;