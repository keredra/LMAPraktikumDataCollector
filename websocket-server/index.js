const port = 8000;
const express = require('express'); 
const http = require('http');
const fs = require('fs');

const app = express();
const server = http.createServer(app);
const io = require('socket.io')(server);



const clients = [];

io.on('connection', (socket) => {
    console.log("Client connected!");
    socket.on('LocationData', (message, callback) => {
        console.log("Message: " + JSON.stringify(message));

        var file = 'data/'+message.context+'.json'
        fs.readFile(file, function (err, data) {
            var json = [];
            
            if (!err) {
                json = JSON.parse(data);
            }
            
            json.push(message.data);
            
            fs.writeFile(file, JSON.stringify(json), function(err){
              if (err) throw err;
            });
        })
    });
});

server.listen(port);
console.log("Server listening on Port " + port);
