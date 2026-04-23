/**
 * @file iDonacion.java
 * @brief Define la interfaz remota iDonacion. 
 * Define que pueden hacer los clientes y que pueden pedirse los servidores entre ellos. 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface iDonacion extends Remote {

    /**
     * @brief Devuelve el nombre del nodo, que es su identificador único. 
     * Esto es útil para que los servidores puedan conocerse entre ellos y replicarse.
     * @return String. El nombre del nodo del servidor.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    String getNombre() throws RemoteException;

    /**
     * @brief Permite que un servidor conozca a otro servidor (peer) para que 
     * puedan replicarse entre ellos.
     * @param peer El servidor peer que se va a conocer.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
	void setPeer(iDonacion peer) throws RemoteException;

    /**
     * @brief Permite al cliente consultar el total de donantes registrados.
     * @return int. El total de donantes registrados.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    int totalDonantes() throws RemoteException;

    /**
     * @brief Permite al cliente consultar si un donante está registrado.
     * @param donante Nombre del donante a consultar.
     * @return boolean. true si el donante está registrado, false en caso contrario.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    boolean estaRegistrado(String donante) throws RemoteException;

    /**
     * @brief Devuelve el número total de donaciones realizadas en esta réplica.
     * @return int. El número total de donaciones realizadas.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    int getTotalDonacionesLocal() throws RemoteException;

    /**
     * @brief Devuelve una lista con los nombres de todos los donantes registrados en esta réplica.
     * @return List<String>. Una lista con los nombres de todos los donantes registrados.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    java.util.List<String> listarDonantesLocal() throws RemoteException;


    //=============================================================================
	// 			OPERACIONES QUE PUEDEN INVOCAR LOS CLIENTES
	//=============================================================================

    /**
     * @brief Permite a un cliente registrar un donante. Si el donante ya estaba 
     * registrado, no hace nada. 
     * @param donante Nombre del donante a registrar.
     * @return iDonacion. Devuelve una referencia al servidor donde se ha registrado el donante,
     * para que el cliente pueda invocar a sus métodos.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    iDonacion registrar(String donante) throws RemoteException;

    /**
     * @brief Permite a un cliente registrar un donante y realizar una donación.
     * @param donante El nombre del donante (debe estar registrado previamente).
     * @param cantidad La cantidad a donar.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    void donar(String donante,int cantidad) throws RemoteException;

    
    //=============================================================================
	// 					OPERACIONES DE CONSULTA
	//=============================================================================

    /**
     * @brief Permite al cliente consultar el total de donaciones realizadas por un donante.      
     * @param donante Nombre del donante (debe estar registrado previamente).
     * @return int. El total de donaciones realizadas por el donante.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    int totalDonaciones(String donante) throws RemoteException;

    /**
     * @brief Permite al cliente listar todos los donantes registrados.
     * @param donante Nombre del donante (debe estar registrado previamente).
     * @return List<String>. Una lista con los nombres de todos los donantes registrados.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
    java.util.List<String> listarDonantes(String donante) throws RemoteException;

    
}