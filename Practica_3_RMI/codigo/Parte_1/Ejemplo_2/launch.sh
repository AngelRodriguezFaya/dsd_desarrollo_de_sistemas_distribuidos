#!/bin/sh -e
# ejecutar = Macro para compilación y ejecución del programa ejemplo
# en una sola maquina Unix de nombre localhost.

KEYSTORE=server-keystore.p12
TRUSTSTORE=client-truststore.p12
STOREPASS=changeit
RMI_PORT=2099

if [ ! -f "$KEYSTORE" ]; then
	echo
	echo "Generando keystore TLS de ejemplo ..."
	keytool -genkeypair \
		-alias rmi-server \
		-keyalg RSA \
		-keysize 2048 \
		-validity 3650 \
		-dname "CN=localhost, OU=RMI, O=DSD, L=Local, ST=Local, C=ES" \
		-storetype PKCS12 \
		-keystore "$KEYSTORE" \
		-storepass "$STOREPASS" \
		-keypass "$STOREPASS" \
		-noprompt
fi

if [ ! -f "$TRUSTSTORE" ]; then
	echo
	echo "Generando truststore TLS de ejemplo ..."
	keytool -exportcert \
		-alias rmi-server \
		-keystore "$KEYSTORE" \
		-storetype PKCS12 \
		-storepass "$STOREPASS" \
		-rfc \
		-file rmi-server.crt

	keytool -importcert \
		-alias rmi-server \
		-file rmi-server.crt \
		-storetype PKCS12 \
		-keystore "$TRUSTSTORE" \
		-storepass "$STOREPASS" \
		-noprompt

	rm -f rmi-server.crt
fi

# Si quedo un proceso previo en el puerto, lo cerramos para evitar conflictos.
if command -v fuser >/dev/null 2>&1; then
	fuser -k "${RMI_PORT}/tcp" >/dev/null 2>&1 || true
fi

echo
echo "Compilando con javac ..."
javac *.java

sleep 2

echo
echo "Lanzando el servidor Ejemplo 2"
java -cp . \
	-Djava.rmi.server.hostname=localhost \
	-Djavax.net.ssl.keyStore="$KEYSTORE" \
	-Djavax.net.ssl.keyStorePassword="$STOREPASS" \
	-Djavax.net.ssl.keyStoreType=PKCS12 \
	-Djavax.net.ssl.trustStore="$TRUSTSTORE" \
	-Djavax.net.ssl.trustStorePassword="$STOREPASS" \
	-Djavax.net.ssl.trustStoreType=PKCS12 \
	Ejemplo &

sleep 2

echo
echo "Lanzando el cliente multi-hebra (12 hebras)"
java -cp . \
	-Djavax.net.ssl.trustStore="$TRUSTSTORE" \
	-Djavax.net.ssl.trustStorePassword="$STOREPASS" \
	-Djavax.net.ssl.trustStoreType=PKCS12 \
	Cliente_Ejemplo_Multi_Threaded localhost 12