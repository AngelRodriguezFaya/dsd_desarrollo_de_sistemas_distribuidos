//import http          from 'node:http';
//import {join}        from 'node:path';
//import {readFile}    from 'node:fs';
//import {Server}      from 'socket.io';
//import {MongoClient} from 'mongodb';

const http         = require('node:http');
const {join}        = require('node:path');
const {readFile}    = require('node:fs');
const {Server}      = require('socket.io');
const {MongoClient} = require('mongodb');
const crypto = require('crypto');

const httpServer = http
    .createServer((request, response) => {
        let {url} = request;
        if(url == '/') {
            url = '/mongo-test.html';
            const filename = join(process.cwd(), url);

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

// Conexión a MongoDB y configuración del servidor Socket.IO
MongoClient.connect("mongodb://localhost:27017/").then((db) => {
    const dbo = db.db("baseDatosTest"); // Base de datos
    const collection = dbo.collection("test"); // Colección para almacenar los datos

    // El servidor se queda a la escucha de conexiones de clientes, cada vez que un cliente se 
    // conecta, se ejecuta la función que se le pasa como argumento a 'connection', esta función recibe un 
    // objeto 'client' que representa al cliente conectado
    const io = new Server(httpServer); // Engancha Socket.io al servidor HTTP, esto abre el canal para WebSockets
    io.sockets.on('connection', (client) => {
        // El servidor emite el evento 'my-address' y le pasa como segundo argumento un 
        // objeto de JavaScript que contiene la IP (host) y el puerto (port).
        client.emit('my-address', {host:client.request.socket.remoteAddress, port:client.request.socket.remotePort});

        // El servidor recibe el evento 'poner' del cliente, que contiene un objeto con los datos a almacenar en MongoDB 
        client.on('poner', (data) => {
            collection.insertOne(data, {safe:true}).then((result) => {});
        });

        // El servidor recibe el evento 'obtener' del cliente, que sirve para obtener los datos almacenados en MongoDB
        client.on('obtener', (data) => {
            collection.find(data).toArray().then((results) => {
                // Aquí el servidor emite el evento 'obtener' y mete en el paquete la variable results 
                // (que es un array o lista con todas las conexiones guardadas en la base de datos). 
                // Cuando ese evento llega al HTML de tu navegador, esa lista de la base de datos se 
                // vuelca en tu variable data. Al ejecutar actualizarLista(data), le estás pasando esa 
                // lista de MongoDB a la función encargada de borrar la pantalla y dibujar los nuevos elementos.
                client.emit('obtener', results);
            });
        });
    });
    httpServer.listen(8080);
}).catch((err) => {console.error(err);});

console.log('Servicio MongoDB iniciado');