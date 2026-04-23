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
	// los pares son: <nombreDonante, cantidadDonacionesRealizadas>
    private final java.util.Map<String, List<Integer>> donantes = new java.util.HashMap<>();
	
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
	// 			ACCIONES QUE PUEDEN INVOCAR LOS CLIENTES
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
					donantes.putIfAbsent(donante, new ArrayList<>()); // Registramos de forma segura
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
     * @brief Permite dar de baja a un donante.
     * @pre El donante debe estar registrado previamente.
     * @param donante El nombre del donante a dar de baja.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
	@Override
    public synchronized void darDeBaja(String donante) throws RemoteException{
		// Comprobamos que el donante esté registrado en esta réplica
		if(this.estaRegistrado(donante)){
			donantes.remove(donante);
			System.out.println("[INFO.] El donante " + donante
				+ " ha sido dado de baja en " + this.getNombre() + ".");
		} else {
			System.out.println("[ERROR.] El donante " + donante
				+ " ha intentado darse de baja en " + this.getNombre()
				+ " pero no está registrado en esta réplica.");
		}
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
			donantes.get(donante).add(cantidad); // Añadimos la cantidad donada al historial del donante
			System.out.println("[INFO.] El donante " + donante 
				+ " ha realizado una donación de " + cantidad 
				+ "€");
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
	 * @brief Permite al cliente consultar el total de donaciones realizadas por todos los donantes en todo el sistema.
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
			if(this.estaRegistrado(donante) && !donantes.get(donante).isEmpty()) {
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
			if(this.estaRegistrado(donante) && !donantes.get(donante).isEmpty()) {
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

	/**
     * @brief Devuelve el total de donaciones realizadas por un donante específico en todo el sistema,
     * sumando las donaciones realizadas.
     * @pre El donante debe estar registrado previamente.
     * @param donante El nombre del donante 
     * @return int. El total de donaciones realizadas por el donante en todo el sistema.
     * @throws RemoteException
     */
	@Override
    public int getTotalDonacionesDonante(String donante) throws RemoteException {
		int total = 0;
		boolean encontrado = false;

		// 1. Comprobamos de forma segura si el donante está en esta réplica
		synchronized(this) {
			encontrado = this.estaRegistrado(donante);
			if (encontrado) { // Está registrado en esta réplica.
				for (Integer cantidad : donantes.get(donante)) {
					total += cantidad;
				}
			}
		}

		if(!encontrado) {
			// 2. Si no está en esta réplica local, buscamos en qué réplica (peer) está registrado
			for (iDonacion peer : peers.values()) {
				if (peer.estaRegistrado(donante)) {
					// Si el donante está registrado en esta réplica peer, 
					// consultamos su total de donaciones en esa réplica peer
					total = peer.getTotalDonacionesDonante(donante);
					encontrado = true;
					break;
				}
			}
		} 

		// Si llegados este punto no lo ha encontrado, es que no está registrado
		if(!encontrado)
			System.out.println("[ERROR.] El donante " + donante + " no está registrado en el sistema.");

		return total;
	}

	/**
     * @brief Devuelve el nombre del donante que ha donado la mayor cantidad de dinero 
     * desde que se registró en el sistema. Si hay varios donantes con la misma cantidad, 
     * se devuelve el que primero se encuentre en la lista de donantes registrados.
     * @return String. El nombre del donante.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
	@Override
	public String mayorDonanteGlobal() throws RemoteException {
		// 1. Obtenemos TODOS los donantes del sistema 
		// Primero obtenemos los donantes registrados en esta réplica.
		List<String> todosLosDonantes = this.listarDonantesLocal();
		for(iDonacion peer : peers.values()) {
			// Ahora obtenemos los donantes registrados en las otras réplicas.
			todosLosDonantes.addAll(peer.listarDonantesLocal());
		}

		String mayorDonante = null;
		int maxCantidad = -1;

		// 2. Iteramos para encontrar el donante 
		for(String d : todosLosDonantes) {
			int totalDeEsteDonante = this.getTotalDonacionesDonante(d); // Consulta su total 
			
			// Si la cantidad es estrictamente MAYOR, se actualiza el máximo.
			// Si es IGUAL, no se actualiza, por lo que Conservamos el que aparece antes en la lista.
			if(totalDeEsteDonante > maxCantidad) {
				maxCantidad = totalDeEsteDonante;
				mayorDonante = d;
			}
		}
		
		return mayorDonante;
	}

	/**
     * @brief Permite a un donante consultar su historial de donaciones.
     * @pre El donante debe estar registrado previamente y haber realizado al menos una donación.
     * @param donante El nombre del donante que quiere consultar su historial.
     * @return List<Integer>. Lista con las cantidades donadas.
     * @throws RemoteException Si ocurre un error de comunicación remota.
     */
	@Override
    public List<Integer> obtenerHistorialDonaciones(String donante) throws RemoteException {
		List<Integer> historial = null;
		boolean encontrado = false;

		// 1. Comprobamos de forma segura si el donante está en esta réplica
		synchronized(this) {
			encontrado = this.estaRegistrado(donante);
			if (encontrado) {
				// Comprobamos la precondición de que haya realizado al menos una donación
				if (!donantes.get(donante).isEmpty()) {
					// Devolvemos una copia por seguridad
					historial = new ArrayList<>(donantes.get(donante)); 
				} else {
					System.out.println("[ERROR.] El donante " 
					+ donante + " no ha realizado ninguna donación.");
				}
			}
		}

		if(!encontrado) {
			// 2. Si no está en esta réplica local, buscamos en qué réplica (peer) 
			// está registrado
			for (iDonacion peer : peers.values()) {
				if (peer.estaRegistrado(donante)) {
					// Si lo encontramos en un peer, le pedimos la información 
					// y cortamos la búsqueda
					historial = peer.obtenerHistorialDonaciones(donante);
					encontrado = true;
					break;
				}
			}
		} 

		// Si llegados este punto no lo ha encontrado en ninguna réplica
		if(!encontrado) {
			System.out.println("[ERROR.] El donante " 
			+ donante + " no está registrado en el sistema.");
		}

		return historial;
	}
}