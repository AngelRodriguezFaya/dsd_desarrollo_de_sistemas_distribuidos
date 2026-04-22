// Fichero: contador.java
// Es el objeto remoto que implementa la interfaz remota

import java.rmi.RemoteException;

public class contador implements icontador {
    private int suma;

    public contador() {
        suma = 0;
    }

    public int sumar() throws RemoteException {
        return suma;
    }

    public void sumar(int valor) throws RemoteException {
        suma = valor;
    }

    public int incrementar() throws RemoteException {
        suma++;
        return suma;
    }
}