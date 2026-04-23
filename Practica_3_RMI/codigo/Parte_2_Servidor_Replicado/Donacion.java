/**
 * @file Donacion.java
 * @brief Implementación de la interfaz remota iDonacion. Este es el objeto remoto que 
 * se va a publicar en el RMI Registry y que los clientes van a invocar.
 */

import java.rmi.*;
import java.util.ArrayList;
import java.util.List;


public class Donacion implements iDonacion {

	// Identificador único de la réplica del servidor
    private final String nombreNodo; 

	// Total de donaciones realizadas de forma local (en esta réplica).
    private int totalDonacionesLocal = 0;

	// Map con los donantes registrados EN ESTA RÉPLICA y la cantidad total que han donado.
    private final java.util.Map<String, Integer> donantes = new java.util.HashMap<>();
	
	// Map con los peers (otras réplicas del servidor) que esta réplica conoce.
	private final java.util.Map<String, iDonacion> peers = new java.util.HashMap<>();

	/**
	 * @brief Constructor de la clase Donacion. 
	 * @param nombreNodo El nombre del nodo, que es su identificador único. 
	 * Este nombre se genera dinámicamente en el servidor para asegurar que cada 
	 * instancia tenga un identificador único.
	 */
    public Donacion(String nombreNodo) {
        this.nombreNodo = nombreNodo;
    }

	/**
	 * @brief Devuelve el nombre del nodo, que es su identificador único. 
	 * Esto es útil para que los servidores puedan conocerse entre ellos y replicarse.
	 * 
	 * @return El nombre del nodo del servidor.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
    public String getNombre() throws RemoteException {
        return nombreNodo;
    }
	
	/**
	 * @brief Permite que un servidor conozca a otro servidor (peer) para que 
	 * puedan replicarse entre ellos. Cada servidor mantiene una lista de sus peers 
	 * y se actualizan mutuamente cuando se registran nuevos donantes o se realizan 
	 * nuevas donaciones.
	 * 
	 * @param peer El servidor peer que se va a conocer.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
	public synchronized void setPeer(iDonacion peer) throws RemoteException {
		peers.put(peer.getNombre(), peer);
	}

	/**
	 * @brief Devuelve el número total de donantes registrados en esta réplica.
	 * @return int. El número total de donantes registrados.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
	public synchronized int totalDonantes() throws RemoteException {
		return donantes.size();
    }

	/**
	 * @brief Permite consultar si un donante está registrado en esta réplica.
	 * @param donante Nombre del donante a consultar.
	 * @return boolean. true si el donante está registrado, false en caso contrario.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
	public synchronized boolean estaRegistrado(String donante) throws RemoteException {
		return donantes.containsKey(donante);
	}

	/**
     * @brief Devuelve el número total de donaciones realizadas en esta réplica.
     * @return int. El número total de donaciones realizadas.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
	@Override
	public synchronized int getTotalDonacionesLocal() throws RemoteException {
		return totalDonacionesLocal;
	}

	/**
	 * @brief Devuelve una lista con los nombres de todos los donantes registrados en esta réplica.
	 * @return List<String>. Una lista con los nombres de todos los donantes registrados.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
	public synchronized List<String> listarDonantesLocal() throws RemoteException {
		return new ArrayList<>(donantes.keySet());
	}


	//=============================================================================
	// 			OPERACIONES QUE PUEDEN INVOCAR LOS CLIENTES
	//=============================================================================

	/**
	 * @brief Permite registrar un donante. 
	 * 
	 * @param donante El nombre del donante a registrar. 
	 * @return iDonacion. El servidor (réplica) donde se ha registrado el donante.
	 * @throws RemoteException Si ocurre un error de comunicación remota. 
	 */
	@Override
	public iDonacion registrar(String donante) throws RemoteException {
		iDonacion peerRegistrado = null; // Réplica donde se ha registrado el donante

		// 1. Comprobar si el donante ya está registrado en esta réplica. 
		if(this.estaRegistrado(donante)) {
			peerRegistrado = this; // El donante ya está registrado en esta réplica
		} else {
			// 2. Comprobamos si no esta registrado en alguna de las otras réplicas
			for(iDonacion peer : peers.values()) {
				if(peer.estaRegistrado(donante)) {
					peerRegistrado = peer; 
					break; 
				}
			}
		}

		// Si el donante no está registrado en ninguna réplica, lo registramos en
		// la réplica con menos donantes registrados para balancear la carga entre las réplicas.
		if(peerRegistrado == null) {
			
			// 3. Encontramos la réplica con menos donantes registrados
			iDonacion mejorPeer = this;	// Mejor réplica encontrada para registrar el nuevo donante
			int minDonantes = this.totalDonantes(); // Minimo nro de donantes registrado en la mejorPeer

			for(iDonacion peer : peers.values()) {
				int nroDonantesPeer = peer.totalDonantes();
				if(nroDonantesPeer < minDonantes) {
					mejorPeer = peer;
					minDonantes = nroDonantesPeer;
				}
			}

			// 4. Registramos el nuevo donante en la mejor réplica encontrada
			if(mejorPeer.getNombre().equals(this.getNombre())) {
				synchronized(this) { // Lo hacemos de exclusión mutua para evitar condiciones de carrera
					donantes.putIfAbsent(donante, 0); // Registramos de forma segura
				}
				peerRegistrado = this;
			} else {
				// Registramos el nuevo donante en la mejor réplica encontrada
				// Como este método ya no bloquea globalmente, evitamos el deadlock
				peerRegistrado = mejorPeer.registrar(donante);
			}
		}

		// El cliente obtiene una referencia al stub del servidor donde se acaba 
		// de registrar el donante, y así puede invocar a sus métodos.
		return peerRegistrado;
    }

	/**
	 * @brief Permite a un donante realizar una donación. 
	 * @pre El donante debe estar registrado previamente en ESTA réplica.
	 * @param donante El nombre del donante que realiza la donación.
	 * @param cantidad La cantidad a donar.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
    public synchronized void donar(String donante, int cantidad) throws RemoteException {
		// Comprobamos que el donante esté registrado en esta réplica
		if(this.estaRegistrado(donante)){
			totalDonacionesLocal += cantidad; 
			donantes.put(donante, donantes.get(donante) + cantidad); 
			System.out.println("[INFO.] El donante " + donante 
				+ " ha realizado una donación de " + cantidad 
				+ "€ en " + this.getNombre() + ". Total donado por el donante: " 
				+ donantes.get(donante));
		} else {
			System.out.println("[ERROR.] El donante " + donante 
				+ " ha intentado realizar una donación"
			 	+ " en " + this.getNombre() 
				+ " pero no está registrado en esta réplica.");
		}
    }


	//=============================================================================
	// 					OPERACIONES DE CONSULTA GLOBAL
	//=============================================================================

	/**
	 * @brief Permite al cliente consultar el total de donaciones realizadas por un donante.
	 * @pre El donante debe estar registrado previamente en ESTA réplica y haber realizado al menos una donación.
	 * @param donante El nombre del donante a consultar.
	 * @return int. El total de donaciones realizadas todo el sistema (en todas las réplicas).
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
    public int totalDonaciones(String donante) throws RemoteException {
		int totalDonacionesGlobal = 0;
		boolean tienePermiso = false;

		//1. Hacemos de forma sincronizada sólamente la comprobación local
		synchronized(this) {
			if(this.estaRegistrado(donante) && donantes.get(donante) > 0) {
				tienePermiso = true;
			}
		}
		
		// 2. Si el donante cumple las precondiciones para consultar...
		if(tienePermiso) {
			// 3. Sumamos las donaciones realizadas en esta réplica.
			totalDonacionesGlobal += this.getTotalDonacionesLocal();

			// 4. Sumamos las donaciones realizadas en las otras réplicas.
			for(iDonacion peer : peers.values()) {
				totalDonacionesGlobal += peer.getTotalDonacionesLocal();
			}
		} else {
			// Si el donante no cumple las precondiciones, mostramos un mensaje de 
			// error indicando el motivo y de forma síncrona.
			synchronized(this) {
				if(!this.estaRegistrado(donante)) {
					System.out.println("[ERROR.] El donante " + donante 
						+ " no está registrado en la réplica:" + this.getNombre());
				} else {
					System.out.println("[ERROR.] El donante " + donante 
						+ " no ha realizado ninguna donación.");
				}
			}
			totalDonacionesGlobal = -1;
		}

		return totalDonacionesGlobal;
    }

	/**
	 * @brief Permite al cliente listar todos los donantes registrados en el sistema.
	 * @pre El donante debe estar registrado previamente en ESTA réplica y haber realizado al menos una donación.
	 * @param donante El nombre del donante que realiza la consulta.
	 * @return List<String>. Una lista con los nombres de todos los donantes registrados.
	 * @throws RemoteException Si ocurre un error de comunicación remota.
	 */
	@Override
    public List<String> listarDonantes(String donante) throws RemoteException {
		
		List<String> donantesRegistradosGlobal = null;
		boolean tienePermiso = false;

		// 1. Hacemos de forma sincronizada sólamente la comprobación local
		synchronized(this) {
			if(this.estaRegistrado(donante) && donantes.get(donante) > 0) {
				tienePermiso = true;
			}
		}

		// 2. Si el donante cumple las precondiciones para consultar...
		if(tienePermiso) {
			// 3. Obtenemos los donantes registrados en esta réplica.
			donantesRegistradosGlobal = this.listarDonantesLocal();

			// 4. Obtenemos los donantes registrados en las otras réplicas.
			for(iDonacion peer : peers.values()) {
				donantesRegistradosGlobal.addAll(peer.listarDonantesLocal());
			}
		} else { 
			// Si el donante no cumple las precondiciones, mostramos un mensaje de 
			// error indicando el motivo y de forma síncrona.
			synchronized(this) {
				if(!this.estaRegistrado(donante)) {
					System.out.println("[ERROR.] El donante " + donante 
						+ " no está registrado en la réplica:" + this.getNombre());
				} else {
					System.out.println("[ERROR.] El donante " + donante 
						+ " no ha realizado ninguna donación.");
				}
			}
		}
		
		return donantesRegistradosGlobal;
    }
}