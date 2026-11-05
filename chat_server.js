const WebSocket = require('ws');

const PORT = process.env.PORT || 5001;
const wss = new WebSocket.Server({ port: PORT });

console.log(`Smart Community Real-Time Chat WebSocket server running on port ${PORT}`);
console.log(`Local WebSocket URL: ws://localhost:${PORT}`);

wss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    console.log(`Client connected from ${ip}. Total connected clients: ${wss.clients.size}`);

    ws.on('message', (message) => {
        const text = message.toString();
        console.log(`Received message: "${text}" - broadcasting to other clients...`);

        wss.clients.forEach((client) => {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
                client.send(text);
            }
        });
    });

    ws.on('close', () => {
        console.log(`Client disconnected. Remaining clients: ${wss.clients.size}`);
    });

    ws.on('error', (error) => {
        console.error(`WebSocket error: ${error.message}`);
    });
});
