package rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dao.HibernateDAO;
import dto.MapaDeRutaDTO;
import dto.PedidoDTO;
import dto.RutaDTO;
import dto.SucursalDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;

public class RemoteObjectHelper {

	private static HibernateDAO hbtDAO;
	public static MapaDeRutaDTO mapadeRuta;

	public RemoteObjectHelper() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
		cargarMapaDeRuta();
	}

	public static void cargarMapaDeRuta() {
		List<RutaDTO> rutas = hbtDAO.obtenerRutas();
		mapadeRuta = new MapaDeRutaDTO();
		mapadeRuta.setRutas(rutas);
	}

	public static RutaDTO obtenerMejorRuta(SucursalDTO origen, SucursalDTO destino) {

		RutaDTO mejorRuta = null;
		float precioMin = -1;
		int kmMin = -1;

		cargarMapaDeRuta();
		for (RutaDTO ruta : mapadeRuta.getRutas()) {
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
	
	public static Date calcularMejorFechaLlegada(int sucursalOrigen, int sucursalDestino) {
		return calcularMejorFechaLlegada(obtenerSucursal(sucursalOrigen) , obtenerSucursal(sucursalOrigen));
	}

	public static Date calcularMejorFechaLlegada(SucursalDTO sucursalOrigen, SucursalDTO sucursalDestino) {

		RutaDTO mejorRuta = obtenerMejorRuta(sucursalOrigen, sucursalDestino);
		float tiempo = 0;

		for (TrayectoDTO trayecto : mejorRuta.getTrayectos()) {
			tiempo = trayecto.getTiempo() + tiempo;
		}

		Date fechaActual = Calendar.getInstance().getTime();
		long minutosRuta = (long) tiempo * 60000;
		return new Date(fechaActual.getTime() + minutosRuta);
	}

	public static SucursalDTO obtenerSucursal(int sucursalId) {
		List<SucursalDTO> sucursales = hbtDAO.obtenerSucursales();
		for (SucursalDTO sucursal : sucursales) {
			if (sucursal.getIdSucursal() == sucursalId) {
				return sucursal;
			}
		}
		return null;
	}

	public static float calcularPrecio(SucursalDTO sucursalOrigen, SucursalDTO sucursalDestino) {
		RutaDTO mejorRuta = obtenerMejorRuta(sucursalOrigen, sucursalDestino);
		float precio = 0;
		for (TrayectoDTO t : mejorRuta.getTrayectos()) {
			precio = precio + t.getPrecio();
		}
		return precio;
	}

	public static ArrayList<PedidoDTO> obtenerCombinacionPedidosPendientesMasConveniente(ArrayList<PedidoDTO> pedidos,
			float volumenIdeal, float volumenTotal) {
		ArrayList<ArrayList<PedidoDTO>> respuestasPosibles = new ArrayList<ArrayList<PedidoDTO>>();
		obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(pedidos, volumenIdeal, volumenTotal,
				new ArrayList<PedidoDTO>(), respuestasPosibles);
		respuestasPosibles = obtenerRespuestasQueCompartenSucursalDestino(respuestasPosibles);
		return obtenerRespuestaMasGrande(respuestasPosibles);
	}

	private static ArrayList<ArrayList<PedidoDTO>> obtenerRespuestasQueCompartenSucursalDestino(
			ArrayList<ArrayList<PedidoDTO>> respuestasPosibles) {

		ArrayList<ArrayList<PedidoDTO>> respuestasPosiblesConDestinoCompartido = new ArrayList<ArrayList<PedidoDTO>>();

		for (ArrayList<PedidoDTO> respuesta : respuestasPosibles) {

			PedidoDTO pedidoDto = respuesta.get(0);
			SucursalDTO sucursalActual = obtenerSucursal(pedidoDto.getSucursalActualId());
			SucursalDTO sucursalDestino = obtenerSucursal(pedidoDto.getSucursalDestinoId());
			RutaDTO mejorRuta = obtenerMejorRuta(sucursalActual, sucursalDestino);
			int proximoDestinoId = mejorRuta.getNextSucursal(sucursalActual).getIdSucursal();

			boolean todosCompartenElMismoDestino = true;

			for (PedidoDTO pedido : respuesta) {

				SucursalDTO sucursalPedidoActual = obtenerSucursal(pedido.getSucursalActualId());
				SucursalDTO sucursalPedidoDestino = obtenerSucursal(pedido.getSucursalDestinoId());
				RutaDTO mejorRutaActual = obtenerMejorRuta(sucursalPedidoActual, sucursalPedidoDestino);
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

	private static ArrayList<PedidoDTO> obtenerRespuestaMasGrande(ArrayList<ArrayList<PedidoDTO>> respuestasPosibles) {
		int max = -1;
		ArrayList<PedidoDTO> respuestaMax = new ArrayList<PedidoDTO>();
		for (ArrayList<PedidoDTO> respuesta : respuestasPosibles) {
			if (max == -1 || max < respuesta.size()) {
				max = respuesta.size();
				respuestaMax = respuesta;
			}
		}
		return respuestaMax;
	}

	private static void obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(ArrayList<PedidoDTO> pedidos,
			float volumenIdeal, float volumenTotal, ArrayList<PedidoDTO> respuestaParcial,
			ArrayList<ArrayList<PedidoDTO>> respuestas) {

		int volumenActual = 0;

		for (PedidoDTO pedido : respuestaParcial) {
			volumenActual += pedido.getVolumenoTotalCargas();
		}

		if (volumenActual >= volumenIdeal && volumenActual < volumenTotal) {
			respuestas.add(respuestaParcial);
		}

		if (volumenActual >= volumenTotal) {
			return;
		}

		for (int i = 0; i < pedidos.size(); i++) {

			ArrayList<PedidoDTO> pedidosFaltantes = new ArrayList<PedidoDTO>();
			PedidoDTO ultimoPedido = pedidos.get(i);

			for (int j = i + 1; j < pedidos.size(); j++) {
				pedidosFaltantes.add(pedidos.get(j));
			}

			ArrayList<PedidoDTO> respuestaParcial_rec = new ArrayList<PedidoDTO>(respuestaParcial);
			respuestaParcial_rec.add(ultimoPedido);
			obtenerCombinacionPedidosPendientesMasConvenienteRecursivo(pedidosFaltantes, volumenIdeal, volumenTotal,
					respuestaParcial_rec, respuestas);
		}
	}

	public static List<VehiculoDTO> obtenerVehiculosDisponiblesEnSucursal(int sucursalId) {
		List<VehiculoDTO> vehiculosDisponibles = new ArrayList<VehiculoDTO>();
		List<VehiculoDTO> vehiculos = hbtDAO.obtenerVehiculos();
		for (VehiculoDTO vehiculo : vehiculos) {
			if (vehiculo.getEstado().equals("Libre") && vehiculo.getSucursalIdActual() == sucursalId) {
				vehiculosDisponibles.add(vehiculo);
			}
		}
		return vehiculosDisponibles;
	}

	public static List<PedidoDTO> ordenarPedidosPorPrioridad(List<PedidoDTO> pedidos) {

		List<PedidoDTO> pedidosAux = new ArrayList<PedidoDTO>();

		// Ordeno pedidos por fecha de carga
		Collections.sort(pedidos, new Comparator<PedidoDTO>() {
			public int compare(PedidoDTO pedido1, PedidoDTO pedido2) {
				Date fechaCargaPedido1 = pedido1.getFechaCarga();
				Date fechaCargaPedido2 = pedido2.getFechaCarga();
				return fechaCargaPedido1.before(fechaCargaPedido2) ? -1 : 1;
			}
		});

		// ELimino vacios
		for (int i = 0; i < pedidos.size(); i++) {
			PedidoDTO pedido = pedidos.get(i);
			if (pedido != null) {
				pedidosAux.add(pedido);
			}
		}

		return pedidosAux;
	}

	public static ArrayList<PedidoDTO> obtenerPedidosConMismoSucursalActual(List<PedidoDTO> pedidosPendientes,
			int sucursalActual) {
		ArrayList<PedidoDTO> pedidosDeSucursalActual = new ArrayList<PedidoDTO>();
		for (PedidoDTO pedido : pedidosPendientes) {
			if (sucursalActual == pedido.getSucursalActualId()) {
				pedidosDeSucursalActual.add(pedido);
			}
		}
		return pedidosDeSucursalActual;
	}

}
