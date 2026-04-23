#!/bin/bash

# =========================================================================
# Script de lanzamiento para Sistema de Donaciones Replicado con RMI SSL
# =========================================================================

echo "==============================================="
echo " LIMPIANDO ENTORNO                             "
echo "==============================================="
# Matamos posibles servidores RMI anteriores de esta práctica para que el puerto 2099 quede libre
pkill -f "java.*Servidor"
rm -f *.class

echo "==============================================="
echo " CONFIGURANDO CLAVES SSL (KEYSTORE/TRUSTSTORE) "
echo "==============================================="
# Como el código utiliza SslRMIServerSocketFactory, Java necesita certificados
# para establecer las conexiones seguras. Si no existen, los generamos con keytool.
if [ ! -f "keystore.jks" ]; then
    echo "[!] Generando nuevo almacén de claves (keystore.jks)..."
    keytool -genkeypair -alias miClaveSSL -keyalg RSA -validity 365 \
            -keystore keystore.jks -storepass contrasena -keypass contrasena \
            -dname "CN=localhost, OU=DSD, O=UGR, L=Granada, ST=Andalucia, C=ES"

    echo "[!] Exportando certificado público..."
    keytool -export -alias miClaveSSL -keystore keystore.jks -storepass contrasena -file certificado.cer

    echo "[!] Generando almacén de confianza (truststore.jks) para los clientes..."
    keytool -import -alias miClaveSSL -file certificado.cer -keystore truststore.jks -storepass contrasena -noprompt
    
    # Limpiamos el certificado suelto porque ya está exportado al truststore
    rm -f certificado.cer
else
    echo "Los certificados SSL ya existen."
fi

echo "==============================================="
echo " COMPILANDO ARCHIVOS                           "
echo "==============================================="
javac *.java
if [ $? -ne 0 ]; then
    echo "[ERROR] Error de compilación. Abortando."
    exit 1
fi
echo "Compilación completada."

echo "==============================================="
echo " LANZANDO SERVIDORES (RÉPLICAS)                "
echo "==============================================="
# NOTA: Los servidores interactúan entre ellos (se hacen de clientes de sus peers) 
# por lo que necesitan pasar como parámetro el keystore (para recibir peticiones) 
# y el truststore (para enviar peticiones).

echo "Lanzando Réplica 1 (establecerá el RMI Registry)..."
java -Djavax.net.ssl.keyStore=keystore.jks \
     -Djavax.net.ssl.keyStorePassword=contrasena \
     -Djavax.net.ssl.trustStore=truststore.jks \
     -Djavax.net.ssl.trustStorePassword=contrasena Servidor &
sleep 2 # Le damos tiempo a la Réplica 1 para crear el Registry en el puerto 2099

echo "Lanzando Réplica 2..."
java -Djavax.net.ssl.keyStore=keystore.jks \
     -Djavax.net.ssl.keyStorePassword=contrasena \
     -Djavax.net.ssl.trustStore=truststore.jks \
     -Djavax.net.ssl.trustStorePassword=contrasena Servidor &
sleep 1 # Le damos 1 segundo

# echo "Lanzando Réplica 3..."
# java -Djavax.net.ssl.keyStore=keystore.jks \
#      -Djavax.net.ssl.keyStorePassword=contrasena \
#      -Djavax.net.ssl.trustStore=truststore.jks \
#      -Djavax.net.ssl.trustStorePassword=contrasena Servidor &
# sleep 1

echo "==============================================="
echo " LANZANDO CLIENTE INTERACTIVO                  "
echo "==============================================="
# El cliente solo hace peticiones, por lo que solo necesita el truststore 
# para confiar en los servidores a los que se conecta.
java -Djavax.net.ssl.trustStore=truststore.jks \
     -Djavax.net.ssl.trustStorePassword=contrasena Cliente

# Cuando el cliente finaliza (Opción 5. Salir), cerramos los servidores que estaban en segundo plano.
echo "==============================================="
echo " CERRANDO SERVIDORES Y LIMPIANDO               "
echo "==============================================="
pkill -f "java.*Servidor"
echo "¡Hasta pronto!"
