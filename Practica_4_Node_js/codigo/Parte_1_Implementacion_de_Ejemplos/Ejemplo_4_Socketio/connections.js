//import http         from 'node:http';
//import {join}       from 'node:path';
//import {readFile}   from 'node:fs';
//import {Server}     from 'socket.io';

const http = require('node:http');
const { join } = require('node:path');
const { readFile } = require('node:fs');
const { Server } = require('socket.io');

const httpServer = http
    .createServer((request, response) => {
        let {url} = request;
        if(url == '/') {
            url = '/connections.html';
            const filename = join(process.cwd(), url); // cwd() devuelve el directorio de trabajo actual

            readFile(filename, (err, data) => {
                if(!err) {
                    response.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});
                    response.write(data);
                } else {
                    response.writeHead(500, {"Content-Type": "text/plain"});
                    response.write(`Error en la lectura del fichero: ${url}`);
                }
                response.end();
            });
        }
        else {
            console.log('Peticion invalida: ' + url);
            response.writeHead(404, {'Content-Type': 'text/plain'});
            response.write('404 Not Found\n');
            response.end();
        }
    });
    
let allClients = new Array(); // Array de objetos con la información de los clientes conectados (dirección IP y puerto)

const io = new Server(httpServer); // Engancha Socket.io al servidor HTTP, esto abre el canal para WebSockets

// El servidor se queda a la escucha de conexiones de clientes, cada vez que un cliente se 
// conecta, se ejecuta la función que se le pasa como argumento a 'connection', esta función recibe un 
// objeto 'client' que representa al cliente conectado
io.sockets.on('connection', (client) => {
        // Gestionamos el registro de la nueva conexión
        const cAddress = client.request.socket.remoteAddress; // obtenemos la dirección IP del cliente 
        const cPort = client.request.socket.remotePort; // obtenemos el puerto del cliente

        // Registramos la nueva conexión en el array de clientes conectados
        allClients.push({address:cAddress, port:cPort});
        console.log(`Nueva conexión de ${cAddress}:${cPort}`);

        // ESTO ES IMPORTANTE. Esta linea es como un megáfono
        // Avisa a todos los clientes conectados que hay una nueva conexión, y les envía el array de clientes conectados
        io.sockets.emit('all-connections', allClients);

        // El servidor se queda a la escucha de mensajes del cliente.
        client.on('output-evt', (data) => {
            // Cuando el servidor recibe un mensaje del cliente, lo muestra por consola 
            // y le contesta exclusivamente a ese cliente con un mensaje de respuesta
            client.emit('output-evt', 'Hola Cliente!');
        });

        // Gestionamos la desconexión del cliente. Este evento salta automáticamente
        // cuando el cliente se desconecta.
        client.on('disconnect', () => {
            console.log(`El usuario ${cAddress}:${cPort} se va a desconectar`);

            // Busca ese usuario en el array de clientes conectados, 
            const index = allClients.findIndex(cli => cli.address == cAddress && cli.port == cPort);

            // Si lo encuentra, lo borra del array de clientes conectados y avisa a todos los 
            // clientes conectados de que hay una desconexión, enviándoles el nuevo array de clientes conectados
            if (index != -1) {
                allClients.splice(index, 1);
                io.sockets.emit('all-connections', allClients);
            } else {
                console.log('¡No se ha encontrado al usuario!')
            }
            console.log(`El usuario ${cAddress}:${cPort} se ha desconectado`);
        });
    }
);

httpServer.listen(8080);
console.log('Servicio Socket.io iniciado');
