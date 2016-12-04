package rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dao.HibernateDAO;
import dto.VehiculoTerceroDTO;
import entities.Pedido;
import entities.Ruta;
import entities.Sucursal;
import entities.Vehiculo;
import entities.VehiculoTercero;

public class RemoteObjectHelper {

	private static HibernateDAO hbtDAO;

	public RemoteObjectHelper() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
	}

	/**
	 * Devuelve una entity sucursal buscada por el id
	 */
	public static Sucursal getSucursalPorIdEntity(int idSucursal) {
		List<Sucursal> sucursales = hbtDAO.obtenerSucursales();
		for (Sucursal sucursal : sucursales) {
			if (sucursal.getIdSucursal() == idSucursal) {
				return sucursal;
			}
		}
		System.out.println("-----No esxiste sucursal en el sistema-----");
		return null;
	}

	/**
	 * Calcula la mejor fecha de llegada desde una sucursal de origen a la
	 * sucursal de destino utilizando los ids de las mismas
	 */
	public static Date calcularMejorFechaLlegada(int sucursalOrigen, int sucursalDestino) {
		return calcularMejorFechaLlegada(getSucursalPorIdEntity(sucursalOrigen),
				getSucursalPorIdEntity(sucursalOrigen));
	}

	/**
	 * Calcula la mejor fecha de llegada desde una sucursal de origen a la
	 * sucursal de destino
	 */
	public static Date calcularMejorFechaLlegada(Sucursal sucursalOrigen, Sucursal sucursalDestino) {
		Ruta mejorRuta = obtenerMejorRuta(sucursalOrigen, sucursalDestino);
		if (mejorRuta != null) {
			float tiempo = mejorRuta.getTiempoTotal();
			Date fechaActual = Calendar.getInstance().getTime();
			long minutosRuta = (long) tiempo * 60000;
			return new Date(fechaActual.getTime() + minutosRuta);
		}
		return null;
	}

	/**
	 * Busca la mejor ruta de una sucursal de origen a la sucursal de destino
	 */
	public static Ruta obtenerMejorRuta(Sucursal origen, Sucursal destino) {

		Ruta mejorRuta = null;
		float precioMin = -1;
		int kmMin = -1;

		List<Ruta> rutas = hbtDAO.obtenerRutas();
		for (Ruta ruta : rutas) {
			if (ruta.getOrigen().getIdSucursal() == origen.getIdSucursal()
					&& ruta.getDestino().getIdSucursal() == destino.getIdSucursal()) {

				int kmRuta = ruta.calcularKm();
				float precioRuta = ruta.getPrecio();

				if (precioMin == -1 && kmMin == -1) {
					mejorRuta = ruta;
					precioMin = precioRuta;
					kmMin = kmRuta;

				} else if (kmRuta < kmMin) {
					precioMin = precioRuta;
					kmMin = kmRuta;
					mejorRuta = ruta;

				} else if (kmRuta == kmMin) {
					if (precioRuta < precioMin) {
						precioMin = precioRuta;
						kmMin = kmRuta;
						mejorRuta = ruta;
					}
				}
			}
		}

		return mejorRuta;
	}

	/**
	 * Busca la combinacion de pedidos mas convienente para enviar
	 */
	public static ArrayList<Pedido> obtenerCombinacionPedidosPendientesMasConveniente(ArrayList<Pedido> pedidos,
			float volumenIdeal, float volumenTotal) {
		ArrayList<ArrayList<Pedido>> respuestasPosibles = new ArrayList<ArrayList<Pedido>>();
		obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(pedidos, volumenIdeal, volumenTotal,
				new ArrayList<Pedido>(), respuestasPosibles);
		respuestasPosibles = obtenerRespuestasQueCompartenSucursalDestino(respuestasPosibles);
		return obtenerRespuestaMasGrande(respuestasPosibles);
	}

	/**
	 * 
	 */
	private static ArrayList<ArrayList<Pedido>> obtenerRespuestasQueCompartenSucursalDestino(
			ArrayList<ArrayList<Pedido>> respuestasPosibles) {

		ArrayList<ArrayList<Pedido>> respuestasPosiblesConDestinoCompartido = new ArrayList<ArrayList<Pedido>>();

		for (ArrayList<Pedido> respuesta : respuestasPosibles) {

			Pedido pedido = respuesta.get(0);
			Sucursal sucursalActual = getSucursalPorIdEntity(pedido.getSucursalActualId());
			Sucursal sucursalDestino = getSucursalPorIdEntity(pedido.getSucursalDestinoId());
			Ruta mejorRuta = obtenerMejorRuta(sucursalActual, sucursalDestino);
			int proximoDestinoId = mejorRuta.getNextSucursal(sucursalActual).getIdSucursal();

			boolean todosCompartenElMismoDestino = true;

			for (Pedido pedidoAux : respuesta) {

				Sucursal sucursalPedidoActual = getSucursalPorIdEntity(pedidoAux.getSucursalActualId());
				Sucursal sucursalPedidoDestino = getSucursalPorIdEntity(pedidoAux.getSucursalDestinoId());
				Ruta mejorRutaActual = obtenerMejorRuta(sucursalPedidoActual, sucursalPedidoDestino);
				int proximoDestinoActualId = mejorRutaActual.getNextSucursal(sucursalPedidoActual).getIdSucursal();

				if (proximoDestinoActualId != proximoDestinoId) {
					todosCompartenElMismoDestino = false;
					break;
				}
			}

			if (todosCompartenElMismoDestino) {
				respuestasPosiblesConDestinoCompartido.add(respuesta);
			}
		}

		return respuestasPosiblesConDestinoCompartido;
	}

	/**
	 * 
	 */
	private static ArrayList<Pedido> obtenerRespuestaMasGrande(ArrayList<ArrayList<Pedido>> respuestasPosibles) {
		int max = -1;
		ArrayList<Pedido> respuestaMax = new ArrayList<Pedido>();
		for (ArrayList<Pedido> respuesta : respuestasPosibles) {
			if (max == -1 || max < respuesta.size()) {
				max = respuesta.size();
				respuestaMax = respuesta;
			}
		}
		return respuestaMax;
	}

	/**
	 * 
	 */
	private static void obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(ArrayList<Pedido> pedidos,
			float volumenIdeal, float volumenTotal, ArrayList<Pedido> respuestaParcial,
			ArrayList<ArrayList<Pedido>> respuestas) {

		int volumenActual = 0;

		for (Pedido pedido : respuestaParcial) {
			volumenActual += pedido.getVolumenTotalDeCargas();
		}

		if (volumenActual >= volumenIdeal && volumenActual < volumenTotal) {
			respuestas.add(respuestaParcial);
		}

		if (volumenActual >= volumenTotal) {
			return;
		}

		for (int i = 0; i < pedidos.size(); i++) {

			ArrayList<Pedido> pedidosFaltantes = new ArrayList<Pedido>();
			Pedido ultimoPedido = pedidos.get(i);

			for (int j = i + 1; j < pedidos.size(); j++) {
				pedidosFaltantes.add(pedidos.get(j));
			}

			ArrayList<Pedido> respuestaParcial_rec = new ArrayList<Pedido>(respuestaParcial);
			respuestaParcial_rec.add(ultimoPedido);
			obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(pedidosFaltantes, volumenIdeal, volumenTotal,
					respuestaParcial_rec, respuestas);
		}
	}

	// Vehiculos
	/**
	 * Devuelve una lista de dto vehiculos disponibles en una sucursal dada
	 */
	public static List<Vehiculo> obtenerVehiculosDisponiblesEnSucursal(int sucursalId) {
		return obtenerVehiculosDisponiblesEnSucursalEntity(sucursalId);
	}

	/**
	 * Devuelve una lista de entities vehiculos disponibles en una sucursal dada
	 */
	public static List<Vehiculo> obtenerVehiculosDisponiblesEnSucursalEntity(int sucursalId) {
		List<Vehiculo> vehiculosDisponibles = new ArrayList<Vehiculo>();
		List<Vehiculo> vehiculos = hbtDAO.obtenerVehiculos();
		for (Vehiculo vehiculo : vehiculos) {
			if (vehiculo.isLibre() && vehiculo.isInSucursal(sucursalId)) {
				vehiculosDisponibles.add(vehiculo);
			}
		}
		return vehiculosDisponibles;
	}

	// Vehiculos de Tercero
	/**
	 * Convierte una lista de VehiculosTercero entity a una lista de
	 * VehiculosTerceroDTO
	 */
	private static List<VehiculoTerceroDTO> vehiculosTercerosToDTO(List<VehiculoTercero> vehiculos) {
		List<VehiculoTerceroDTO> vehiculosDto = new ArrayList<VehiculoTerceroDTO>();
		for (VehiculoTercero vehiculo : vehiculos) {
			vehiculosDto.add(vehiculo.toDTO());
		}
		return vehiculosDto;
	}

	/**
	 * Devuelve una lista de entities vehiculos de tercero disponibles
	 */
	public static List<VehiculoTercero> obtenerVehiculosTerceroDisponiblesEntity() {
		List<VehiculoTercero> vehiculosTerceros = hbtDAO.listarVTerceros();
		List<VehiculoTercero> disponibles = new ArrayList<VehiculoTercero>();
		for (VehiculoTercero vehiculo : vehiculosTerceros) {
			if (vehiculo.isLibre()) {
				disponibles.add(vehiculo);
			}
		}
		return disponibles;
	}

	/**
	 * Devuelve una lista de dto vehiculos de tercero disponibles
	 */
	public static List<VehiculoTerceroDTO> obtenerVehiculosTercerosDisponibles() {
		return vehiculosTercerosToDTO(obtenerVehiculosTerceroDisponiblesEntity());
	}

	// Pedidos
	/**
	 * Devuelve una lista de pedidos ordenado por su prioridad
	 */
	public static List<Pedido> ordenarPedidosPorPrioridad(List<Pedido> pedidos) {

		List<Pedido> pedidosAux = new ArrayList<Pedido>();

		// Ordeno pedidos por fecha de carga
		Collections.sort(pedidos, new Comparator<Pedido>() {
			public int compare(Pedido pedido1, Pedido pedido2) {
				Date fechaCargaPedido1 = pedido1.getFechaCarga();
				Date fechaCargaPedido2 = pedido2.getFechaCarga();
				return fechaCargaPedido1.before(fechaCargaPedido2) ? -1 : 1;
			}
		});

		// ELimino vacios
		for (int i = 0; i < pedidos.size(); i++) {
			Pedido pedido = pedidos.get(i);
			if (pedido != null) {
				pedidosAux.add(pedido);
			}
		}

		return pedidosAux;
	}

	/**
	 * Devuelve la lista de pedidos que se encuentran en la misma sucursal
	 */
	public static ArrayList<Pedido> obtenerPedidosConMismoSucursalActual(List<Pedido> pedidosPendientes,
			int sucursalActual) {
		ArrayList<Pedido> pedidosDeSucursalActual = new ArrayList<Pedido>();
		for (Pedido pedido : pedidosPendientes) {
			if (sucursalActual == pedido.getSucursalActualId()) {
				pedidosDeSucursalActual.add(pedido);
			}
		}
		return pedidosDeSucursalActual;
	}

}
