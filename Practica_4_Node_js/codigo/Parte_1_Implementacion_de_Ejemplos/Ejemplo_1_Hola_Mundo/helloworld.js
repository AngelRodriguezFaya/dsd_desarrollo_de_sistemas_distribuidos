//import http from 'node:http';
const http = require('node:http');

// Creamos un servidor HTTP que atiende las peticiones entrantes
// La función createServer() recibe una función de callback que 
// se ejecuta cada vez que llega una petición al servidor
// request -> es el mensaje de petición recibido en el servicio
// response -> es un mensaje de respuesta a la petición recibida, que el servicio enviará al cliente
http.createServer((request, response) => {
        console.log(request.headers);
        response.writeHead(200, {'Content-Type': 'text/plain'});
        response.write('Hola mundo');
        response.end();
    })
    .listen(8080);

console.log('Servicio HTTP iniciado');