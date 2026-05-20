//import http from 'node:http';
const http = require('node:http');

function calcular(operacion, val1, val2) {
    if (operacion == 'sumar') return val1+val2;
    else if (operacion == 'restar') return val1-val2;
    else if (operacion == 'producto') return val1*val2;
    else if (operacion == 'dividir') return val1/val2;
    else return 'Error: Parámetros no válidos';
}

http.createServer((request, response) => {
        let {url} = request;
        url = url.slice(1); // Elimina el primer caracter '/' de la URL para facilitar el parseo de los parámetros
        const params = url.split('/'); // Separa la URL en partes utilizando el caracter '/' como delimitador, obteniendo así un array de parámetros
        let output='';
        if (params.length >= 3) {
            // parseFloat() deserializa los datos para poder operar con ellos numericamente
            const val1 = parseFloat(params[1]); 
            const val2 = parseFloat(params[2]);
            const result = calcular(params[0], val1, val2);
            output = result.toString();
        }
        else output = 'Error: El número de parámetros no es válido';

        response.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});
        response.write(output);
        response.end();
    })
    .listen(8080);

console.log('Servicio HTTP iniciado');

