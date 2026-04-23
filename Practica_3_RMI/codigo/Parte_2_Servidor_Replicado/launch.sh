#!/bin/bash

# Configuración de variables
KEYSTORE=server-keystore.p12
TRUSTSTORE=client-truststore.p12
STOREPASS=changeit
RMI_PORT=2099

echo "================================================="
echo "   Iniciando Sistema de Donaciones (RMI SSL)     "
echo "================================================="

# 1. Generación de certificados SSL (Si no existen)
if [ ! -f "$KEYSTORE" ]; then
    echo "[1/5] Generando keystore TLS para el servidor..."
    keytool -genkeypair \
        -alias rmi-server \
        -keyalg RSA \
        -keysize 2048 \
        -validity 365 \
        -dname "CN=localhost, OU=DSD, O=UGR, L=Granada, ST=Granada, C=ES" \
        -storetype PKCS12 \
        -keystore "$KEYSTORE" \
        -storepass "$STOREPASS" \
        -keypass "$STOREPASS" \
        -noprompt
fi

if [ ! -f "$TRUSTSTORE" ]; then
    echo "[2/5] Generando truststore TLS para el cliente..."
    keytool -exportcert \
        -alias rmi-server \
        -keystore "$KEYSTORE" \
        -storepass "$STOREPASS" \
        -file rmi-server.crt \
        -rfc
    keytool -importcert \
        -alias rmi-server \
        -file rmi-server.crt \
        -keystore "$TRUSTSTORE" \
        -storepass "$STOREPASS" \
        -noprompt
    rm rmi-server.crt
fi

# 2. Limpieza de procesos (Matar cualquier servidor previo en el puerto 2099)
echo "[3/5] Limpiando procesos previos en el puerto $RMI_PORT..."
fuser -k $RMI_PORT/tcp > /dev/null 2>&1

# 3. Compilación de todos los archivos
echo "[4/5] Compilando los archivos Java..."
javac *.java

if [ $? -ne 0 ]; then
    echo "Error en la compilación. Abortando ..."
    exit 1
fi

# 4. Lanzamiento de las Réplicas
echo "[5/5] Levantando las réplicas del servidor..."

# Lanzar Réplica 1 (Esta creará el Registry y se llamará Donacion_1)
java -cp . \
    -Djava.rmi.server.hostname=localhost \
    -Djavax.net.ssl.keyStore="$KEYSTORE" \
    -Djavax.net.ssl.keyStorePassword="$STOREPASS" \
    -Djavax.net.ssl.keyStoreType=PKCS12 \
    -Djavax.net.ssl.trustStore="$TRUSTSTORE" \
    -Djavax.net.ssl.trustStorePassword="$STOREPASS" \
    -Djavax.net.ssl.trustStoreType=PKCS12 \
    Servidor &

# Le damos 2 segundos para que arranque bien antes de lanzar la siguiente
sleep 2 

# Lanzar Réplica 2 (Esta detectará a la 1 y se llamará Donacion_2)
java -cp . \
    -Djava.rmi.server.hostname=localhost \
    -Djavax.net.ssl.keyStore="$KEYSTORE" \
    -Djavax.net.ssl.keyStorePassword="$STOREPASS" \
    -Djavax.net.ssl.keyStoreType=PKCS12 \
    -Djavax.net.ssl.trustStore="$TRUSTSTORE" \
    -Djavax.net.ssl.trustStorePassword="$STOREPASS" \
    -Djavax.net.ssl.trustStoreType=PKCS12 \
    Servidor &

# Le damos otros 2 segundos para que se conecten entre ellas
sleep 2 

echo "================================================="
echo "   Servidores listos. Lanzando el Cliente...     "
echo "================================================="

# 5. Lanzamiento del Cliente
java -cp . \
    -Djavax.net.ssl.trustStore="$TRUSTSTORE" \
    -Djavax.net.ssl.trustStorePassword="$STOREPASS" \
    -Djavax.net.ssl.trustStoreType=PKCS12 \
    Cliente localhost