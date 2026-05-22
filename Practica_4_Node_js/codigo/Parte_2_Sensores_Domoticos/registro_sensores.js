const http = require('node:http');
const {join} = require('node:path');
const {readFile} = require('node:fs');
const {MongoClient} = require('mongodb');
const {Server} = require('socket.io');

const TelegramBot = require('node-telegram-bot-api');

// ===========================================================================
//                  Configuración y variables globales.
// ===========================================================================

// ------- Configuración básica: puerto y conexión a MongoDB. -------
const PORT = process.env.PORT || 8080;
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017';
const MONGODB_DB_NAME = process.env.MONGODB_DB_NAME || 'domotica';

// ------- Objetos "freeze" para el estado de los actuadores -------

const EstadoAC = Object.freeze({
    APAGADO: 'APAGADO',
    ENCENDIDO: 'ENCENDIDO'
});

const EstadoPersiana = Object.freeze({
    ABIERTA: 'ABIERTA',
    CERRADA: 'CERRADA'
});

// ------- Variables globales compartidas para reutilizar la conexion y la coleccion a la base de datos. -------
let mongoClient;        // Cliente de MongoDB
let sensoresCollection; // Colección de MongoDB donde se almacenarán los sensores registrados. 
let historialCollection; // Colección de MongoDB donde se almacenarán las mediciones de los sensores.

let estadoActuadores = {  // Estado actual de los actuadores, compartido para que el agente de control y el dashboard puedan acceder a él.
    aireAcondicionado: {
        "Salon": EstadoAC.APAGADO,
        "Dormitorio 1": EstadoAC.APAGADO,
        "Cocina": EstadoAC.APAGADO,
    },
    persiana: {
        "Salon": EstadoPersiana.ABIERTA,
        "Dormitorio 1": EstadoPersiana.ABIERTA,
        "Dormitorio 2": EstadoPersiana.ABIERTA,
        "Cocina": EstadoPersiana.ABIERTA,
        "Baño": EstadoPersiana.ABIERTA
    }
};

// ------- Constantes/Umbrales para el agente de control -------
const MAX_TEMP = 30.0; // grados Celsius
const MIN_TEMP = 17.0; // grados Celsius
const MAX_LUMINOSIDAD = 700; // Lux

// ------- Configuración del bot de Telegram -------
const TELEGRAM_TOKEN = '8702302349:AAHUwKTa4EM73Qedbv9qeDpPYKl3dvwu2sc';
const telegramBot = new TelegramBot(TELEGRAM_TOKEN, {polling: true});

// --- AÑADE ESTO PARA VER SI HAY ERRORES DE TELEGRAM ---
telegramBot.on('polling_error', (error) => {
    console.log('[ERROR TELEGRAM]', error.message);
});

let miChatId = null; // Variable para almacenar el chat ID del usuario que interactúa con el bot

// Cuando el cliente hable por primera vez con el bot, guardamos su chat ID para enviarle alertas posteriormente.
telegramBot.onText(/\/start/, (msg) => {
    miChatId = msg.chat.id;
    telegramBot.sendMessage(miChatId, '¡Hola! Soy tu asistente de domótica. Te enviaré alertas importantes sobre el estado de tu hogar.');
    console.log(`[TELEGRAM] Usuario registrado para alertas: ${miChatId}`);
});

// ===========================================================================
//                  Funciones para manejar MongoDB.
// ===========================================================================

// Abre la conexion con MongoDB y guarda la coleccion a utilizar.
async function connectToMongo() {
	//Inicializamos el cliente de la base de datos
    mongoClient = new MongoClient(MONGODB_URI);

	//Esperamos a que conecte
    await mongoClient.connect();

	//Obtenemos la base de datos y creamos/cargamos las colecciones necesarias
    const db = mongoClient.db(MONGODB_DB_NAME);
    sensoresCollection = db.collection('sensores');
    historialCollection = db.collection('historial');
}


// Inserta un sensor nuevo en MongoDB y devuelve el documento insertado.
async function registerSensor(sensorData) {
	//Creamos la entrada de la tabla a partir de los datos pasados en el parametro sensorData
    const sensor = {
        name: sensorData.name,
        location: sensorData.location,
        type: sensorData.type,
        registeredAt: new Date()
    };

	//Insertamos la entrada en la coleccion "sensores"
    await sensoresCollection.insertOne(sensor);
    return sensor;
}


// Muestra por consola todos los sensores registrados.
async function logRegisteredSensors() {
	//Obtenemos todas las entradas de la coleccion "sensores"
    const sensors = await sensoresCollection
        .find({}, {projection: {_id: 0}})
        .sort({registeredAt: -1})
        .toArray();

	//Las mostramos por consola en formato tabla
    console.log('Sensores registrados actualmente en la base de datos:');
    console.table(sensors);
}

// ===========================================================================
//                       SERVIDOR HTTP (Servir archivos)
// ===========================================================================

// Creamos el servidor http.
const httpServer = http.createServer((request, response) => {
    const {method, url} = request;
    let filePath = null;

	//Para el home y la pagina sensores, cargar sensor.html, para la pagina dashboard, cargar dashboard.html
    if(method === 'GET' && (url === '/' || url === '/sensor.html')) {
        filePath = join(process.cwd(), 'sensor.html');

    } else if( method === 'GET' && url === '/dashboard.html') {
        filePath = join(process.cwd(), 'dashboard.html');
    }

    if(filePath) {
        //Leemos el contenido de sensor.html y lo guardamos en data
        readFile(filePath, (err, data) => {
            if(err) {
                response.writeHead(500, {'Content-Type': 'text/plain; charset=utf-8'});
                response.end('No se pudo cargar el HTML.');
            } else {
                //Devolvemos al navegador una página creada a partir de data
                response.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});
                response.end(data);
            }
            response.end();
        });    
    } else {
        //En caso de que la ruta no sea ni '/' ni '/dashboard.html', devolvemos un error 404
        response.writeHead(404, {'Content-Type': 'text/plain; charset=utf-8'});
        response.end('Ruta no encontrada.');
        response.end();
    }
});

// ===========================================================================
//                  SERVIDOR EN TIEMPO REAL (Socket.IO)
// ===========================================================================

// Iniciamos el servidor HTTP y Socket.IO.
const io = new Server(httpServer); // Engancha Socket.io al servidor HTTP, abriendo el canal para WebSockets

// -------- Inicializamos la infraestructura del servidor ---------
async function startServer() {
    try {
		//Levantamos mongo
        await connectToMongo();

		// Levantamos el servidor web en el puerto
        httpServer.listen(PORT, () => {
            console.log(`Servidor basico escuchando en http://localhost:${PORT}`);
        });
    } catch(err) {
        console.error('No se pudo iniciar el servidor:', err.message);
        process.exit(1);
    }
}

// Manejar la conexion de Socket.IO
io.on('connection', (socket) => {
    console.log('Nuevo nodo conectado: ' + socket.id);

    // Al conectarse el Dashboard, le enviamos el estado actual de los actuadores para que se muestre correctamente en la interfaz.
    socket.emit('dashboard:update-actuadores', estadoActuadores);

    // [Evento 1]: Registro de sensores 
    socket.on('sensor:register', async (payload) => { // payload son los datos del formulario enviados por el cliente
        const sensorData = {
            //Extraemos los datos del payload, asegurándonos de que sean strings y eliminando espacios innecesarios.
            name: String(payload?.name || '').trim(),
            location: String(payload?.location || '').trim(),
            type: String(payload?.type || '').trim()
        };

        // Validamos que se hayan proporcionado los campos obligatorios.
        if(!sensorData.name || !sensorData.location || !sensorData.type) {
            // Emitimos un evento de error al cliente que intenta registrar el sensor.
			socket.emit('sensor:error', 'Faltan campos obligatorios.');
		} else {
			try {
				//Intentamos incluir el sensor en la BD
				const sensor = await registerSensor(sensorData);

				//Mostramos la colección de sensores completa
				await logRegisteredSensors();
				socket.emit('sensor:registered', sensor);

			} catch(err) {
				//En caso de error, emitir evento de error al sensor.
				socket.emit('sensor:error', 'Error registrando el sensor en la base de datos.');
			}
		}
    });

    // [Evento 2]: Recepción de medidas del sensor
    socket.on('sensor:data', async (payload) => {
        try {
            // Creamos el documento 
            const medidaNueva = {
                sensorId: payload.sensorId, 
                name: payload.name,
                type: payload.type,
                location: payload.location,
                value: parseFloat(payload.value), 
                timestamp: new Date() 
            };

            // 1. Insertamos el documento en la colección "historial"
            await historialCollection.insertOne(medidaNueva);
            console.log('[BD] Nueva medida registrada:', medidaNueva);
            
            // 2. Reenviamos (en tiempo real) la medida a todos los clientes conectados (incluido el que la envió) para que puedan actualizar su dashboard.
            io.sockets.emit('dashboard:update-sensor', medidaNueva);

            // 3. Llamamos al agente
            ejecutarAgente(medidaNueva);

        } catch(err) {
            console.error('[ERROR] Error registrando la medida en la base de datos:', err.message);
        }
    });

    // ===========================================================================
    //                  CONTROL MANUAL DESDE EL DASHBOARD
    // ===========================================================================
    // [Evento 3]: Control manual del AC desde el dashboard
    socket.on('ui:toggle-ac', (ubicacion) => {
        if(!ubicacion)
            return; // Si no se especifica una ubicación, no hacemos nada

        estadoActuadores.aireAcondicionado[ubicacion] = 
            (estadoActuadores.aireAcondicionado[ubicacion] === EstadoAC.APAGADO) ? EstadoAC.ENCENDIDO : EstadoAC.APAGADO;
        
        console.log(`[MANUAL] A/C de ${ubicacion} cambiado a: ${estadoActuadores.aireAcondicionado[ubicacion]}`);
        io.sockets.emit('dashboard:update-actuadores', estadoActuadores);
    });

    // [Evento 4]: Control manual de la persiana desde el dashboard
    socket.on('ui:toggle-blind', (ubicacion) => {
        if(!ubicacion)
            return; // Si no se especifica una ubicación, no hacemos nada

        estadoActuadores.persiana[ubicacion] = 
            (estadoActuadores.persiana[ubicacion] === EstadoPersiana.ABIERTA) ? EstadoPersiana.CERRADA : EstadoPersiana.ABIERTA;
            
        console.log(`[MANUAL] Persiana de ${ubicacion} cambiada a: ${estadoActuadores.persiana[ubicacion]}`);
        io.sockets.emit('dashboard:update-actuadores', estadoActuadores);
    });

    // [Evento 5]: Enviar sensores existentes al abrir la página
    socket.on('sensor:request-sensors', async () => {
        try {
            const listaSensores = await sensoresCollection.find().toArray();
            // Solamente los enviamos al cliente que lo ha solicitado
            socket.emit('sensor:response-sensors', listaSensores);
        } catch(err) {
            console.error('[ERROR] Error obteniendo la lista de sensores de la base de datos:', err.message);
        }
    });

    // [Evento X]: Manejar la desconexión del cliente.
    socket.on('disconnect', () => {
        console.log('El nodo ' + socket.id + ' se ha desconectado.');
    });
});

// ===========================================================================
//                  AGENTE DE CONTROL
// ===========================================================================
function notificarAlerta(mensaje) {
    // 1. Enviar mensaje de alerta al dashboard
    io.sockets.emit('dashboard:alert', mensaje);

    // 2. Enviar mensaje de alerta por Telegram (si el usuario se ha registrado)
    if (miChatId) {
        telegramBot.sendMessage(miChatId, mensaje, {parse_mode: 'Markdown'});
    } else {
        console.log('[TELEGRAM] La Alerta ha sido retenida, no se ha inciado el bot con /start');
    }
}

function ejecutarAgente(medida) {
    
    const valor = medida.value;
    const tipo = medida.type;
    const ubicacion = medida.location;

    // Reglas de control para el aire acondicionado basadas en la temperatura
    if(tipo === 'Temperatura') {

        // Regla 1: Si la temperatura es mayor que MAX_TEMP -> Encender el A/C
        if(valor > MAX_TEMP && estadoActuadores.aireAcondicionado[ubicacion] === EstadoAC.APAGADO) {
            estadoActuadores.aireAcondicionado[ubicacion] = EstadoAC.ENCENDIDO;
            io.sockets.emit('dashboard:update-actuadores', estadoActuadores);

            // Notificamos la alerta tanto en el dashboard como por Telegram
            notificarAlerta(`⚠️ ¡ALERTA! Temperatura máxima superada (${valor}°C)` + 
                ` en ${ubicacion}. Encendiendo A/C automáticamente.`);
        }

        // Regla 2: Si la temperatura es menor que MIN_TEMP -> Apagar el A/C
        if(valor < MIN_TEMP && estadoActuadores.aireAcondicionado[ubicacion] === EstadoAC.ENCENDIDO) {
            estadoActuadores.aireAcondicionado[ubicacion] = EstadoAC.APAGADO;
            io.sockets.emit('dashboard:update-actuadores', estadoActuadores);
            
            notificarAlerta(`⚠️ ¡ALERTA! Temperatura por debajo del mínimo(${valor}°C)` + 
                ` en ${ubicacion}. Apagando A/C automáticamente.`);
        }
    }
    

    // Regla de control para la persiana
    if(tipo === 'Luminosidad') {

        // Regla 3: Si la luminosidad es mayor que MAX_LUMINOSIDAD -> Bajar la persiana
        if(valor > MAX_LUMINOSIDAD && estadoActuadores.persiana[ubicacion] === EstadoPersiana.ABIERTA) {
            estadoActuadores.persiana[ubicacion] = EstadoPersiana.CERRADA;
            io.sockets.emit('dashboard:update-actuadores', estadoActuadores);
            
            notificarAlerta(`⚠️ ¡ALERTA! Luminosidad máxima superada (${valor} lux)` + 
                ` en ${ubicacion}. Bajando persiana automáticamente.`);

        }
    }
}



// ===========================================================================
//   Cierre limpio de MongoDB al detener el proceso (Ctrl+C). 
// ===========================================================================
// Detecta cuando pulsas Ctrl+C en la terminal para apagar el servidor 
// y se asegura de cerrar la conexión con MongoDB limpiamente antes de matar el proceso.
process.on('SIGINT', async () => {
	//Si la conexión con el servidor de mongoDB está activa, la cierra
    if(mongoClient) {
        await mongoClient.close();
    }

	//Mata el proceso del servidor
    process.exit(0);
});

// ===========================================================================
//                  Arrancamos el sistema por completo
// ===========================================================================
startServer();
