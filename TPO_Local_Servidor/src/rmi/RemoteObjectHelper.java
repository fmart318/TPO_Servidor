package rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dao.HibernateDAO;
import dto.EnvioDTO;
import dto.MapaDeRutaDTO;
import dto.PedidoDTO;
import dto.RutaDTO;
import dto.SucursalDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;
import dto.ViajeDTO;

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

	public static ArrayList<EnvioDTO> obtenerCombinacionEnviosPendientesMasConveniente(ArrayList<EnvioDTO> envios, float volumenIdeal, float volumenTotal) {
		ArrayList<ArrayList<EnvioDTO>> respuestasPosibles = new ArrayList<ArrayList<EnvioDTO>>();
		obtenerCombinacionEnviosPendientesMasConvenienteRecursivo(envios, volumenIdeal, volumenTotal, new ArrayList<EnvioDTO>(), respuestasPosibles);
		respuestasPosibles = obtenerRespuestasQueCompartenSucursalDestino(respuestasPosibles);
		return obtenerRespuestaMasGrande(respuestasPosibles);
	}

	private static ArrayList<ArrayList<EnvioDTO>> obtenerRespuestasQueCompartenSucursalDestino(
			ArrayList<ArrayList<EnvioDTO>> respuestasPosibles) {
		
		ArrayList<ArrayList<EnvioDTO>> respuestasPosiblesConDestinoCompartido = new ArrayList<ArrayList<EnvioDTO>>();
		
		for (ArrayList<EnvioDTO> respuesta : respuestasPosibles) {
			EnvioDTO envioDto = respuesta.get(0);
			SucursalDTO sucursalDestino = envioDto.getSucursalDestino();
			boolean todosCompartenElMismoDestino = true;
			for (EnvioDTO envio : respuesta) {
				SucursalDTO sucursalDestinoActual = envioDto.getSucursalDestino();
				if (sucursalDestinoActual.getIdSucursal() != sucursalDestino.getIdSucursal()) {
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

	private static ArrayList<EnvioDTO> obtenerRespuestaMasGrande(ArrayList<ArrayList<EnvioDTO>> respuestasPosibles) {
		int max = -1;
		ArrayList<EnvioDTO> respuestaMax = new ArrayList<EnvioDTO>();
		for (ArrayList<EnvioDTO> respuesta : respuestasPosibles) {
			if (max == -1 || max < respuesta.size()) {
				max = respuesta.size();
				respuestaMax = respuesta;
			}
		}
		return respuestaMax;
	}

	private static void obtenerCombinacionEnviosPendientesMasConvenienteRecursivo(ArrayList<EnvioDTO> envios, float volumenIdeal, float volumenTotal, ArrayList<EnvioDTO> respuestaParcial,
			ArrayList<ArrayList<EnvioDTO>> respuestas) {
		
		int volumenActual = 0;

		for (EnvioDTO envio : respuestaParcial) {
			volumenActual += envio.getPedido().getVolumenoTotalCargas();
		}

		if (volumenActual >= volumenIdeal && volumenActual < volumenTotal) {
			respuestas.add(respuestaParcial);
		}

		if (volumenActual >= volumenTotal) {
			return;
		}

		for (int i = 0; i < envios.size(); i++) {
			
			ArrayList<EnvioDTO> enviosFaltantes = new ArrayList<EnvioDTO>();
			EnvioDTO ultimoEnvio = envios.get(i);
			
			for (int j = i + 1; j < envios.size(); j++) {
				enviosFaltantes.add(envios.get(j));
			}
			
			ArrayList<EnvioDTO> respuestaParcial_rec = new ArrayList<EnvioDTO>(respuestaParcial);
			respuestaParcial_rec.add(ultimoEnvio);
			obtenerCombinacionEnviosPendientesMasConvenienteRecursivo(enviosFaltantes, volumenIdeal, volumenTotal, respuestaParcial_rec, respuestas);
		}
	}
	
	public static List<VehiculoDTO> obtenerVehiculosDisponibles() {
		List<VehiculoDTO> vehiculosDisponibles = new ArrayList<VehiculoDTO>();
		List<VehiculoDTO> vehiculos = hbtDAO.obtenerVehiculos();
		for (VehiculoDTO vehiculo : vehiculos) {
			if (vehiculo.getEstado().equals("Libre")) {
				vehiculosDisponibles.add(vehiculo);
			}
		}
		return vehiculosDisponibles;
	}
	
	public static ViajeDTO obtenerViajeDelEnvio(EnvioDTO envioDto) {
		List<ViajeDTO> viajes = hbtDAO.obtenerViajes();
		ViajeDTO viajeDelEnvio = null;
		for (ViajeDTO viaje : viajes) {
			for (EnvioDTO envio : viaje.getEnvios()) {
				if (envio.getIdEnvio() == envioDto.getIdEnvio()) {
					viajeDelEnvio = viaje;
					break;
				}
			}
		}
		return viajeDelEnvio;
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
		for (int i = 0 ; i < pedidos.size() ; i++) {
			PedidoDTO pedido = pedidos.get(i);
			if (pedido != null) {
				pedidosAux.add(pedido);
			}
		}

		return pedidosAux;
	}
}
