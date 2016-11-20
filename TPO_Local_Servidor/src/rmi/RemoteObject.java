package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import Strategy.PoliticaEspecificidad;
import Strategy.PoliticaGarantia;
import Strategy.PoliticaGeneral;
import Strategy.PoliticaMantenimiento;
import dao.HibernateDAO;
import dto.CargaDTO;
import dto.ClienteDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.HabilitadoDTO;
import dto.MapaDeRutaDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.ProveedorDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SeguroDTO;
import dto.SucursalDTO;
import dto.TransporteDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;
import dto.ViajeDTO;
import entities.Carga;
import entities.Cliente;
import entities.Direccion;
import entities.Empresa;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.Ruta;
import entities.Sucursal;
import entities.Trayecto;
import entities.Vehiculo;
import entities.Viaje;

public class RemoteObject extends UnicastRemoteObject implements RemoteInterface {

	private static final long serialVersionUID = 1L;
	private static HibernateDAO hbtDAO;
	public static MapaDeRutaDTO mapadeRuta;
	private PoliticaMantenimiento politicaMantenimiento;

	public void InicializarMapaDeRuta() {
		cargarMapaDeRuta();
	}

	public RemoteObject() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
	}

	public void altaPedido(PedidoDTO pedidoDTO) {

		Pedido pedido = new Pedido();
		SucursalDTO o = RemoteObjectHelper.obtenerSucursal(pedidoDTO.getSucursalOrigen());
		SucursalDTO d = RemoteObjectHelper.obtenerSucursal(pedidoDTO.getSucursalDestino());
		pedidoDTO.setPrecio(RemoteObjectHelper.calcularPrecio(o, d));
		pedido = EntityManager.PedidoToEntity(pedidoDTO);

		hbtDAO.guardar(pedido);
	}

	@Override
	public void altaCliente(ClienteDTO clienteDto) throws RemoteException {
		Cliente cliente = new Cliente();
		cliente = EntityManager.ClienteToEntity(clienteDto);
		hbtDAO.guardar(cliente);
	}

	public void altaEnvio(EnvioDTO envioDTO) {
		hbtDAO.guardar(EntityManager.EnvioToEntity(envioDTO));
	}

	public void altaVehiculo(VehiculoDTO vehiculoDTO) {
		hbtDAO.guardar(EntityManager.VehiculoToEntity(vehiculoDTO));
	}

	public void altaPlanMantenimiento(PlanDeMantenimientoDTO planDeMantenimientoDTO) {
		hbtDAO.guardar(EntityManager.PlanDeMantenimientoToEntity(planDeMantenimientoDTO));
	}

	public void altaViaje(ViajeDTO viajeDTO) {
		hbtDAO.guardar(EntityManager.ViajeToEntity(viajeDTO));
	}

	public void altaCarga(CargaDTO cargaDTO) {
		hbtDAO.guardar(EntityManager.CargaToEntity(cargaDTO));
	}

	public void altaRemito(RemitoDTO remitoDTO) {
		hbtDAO.guardar(EntityManager.RemitoToEntity(remitoDTO));
	}

	public void altaFactura(FacturaDTO facturaDTO) {
		hbtDAO.guardar(EntityManager.FacturaToEntity(facturaDTO));
	}

	public void altaTransporte(TransporteDTO transporteDTO) {
		hbtDAO.guardar(EntityManager.TransporteToEntity(transporteDTO));
	}

	public void altaProveedor(ProveedorDTO proveedorDTO) {
		hbtDAO.guardar(EntityManager.ProveedorToEntity(proveedorDTO));
	}

	public List<ViajeDTO> ObtenerViajesDeCliente(int idCliente) {
		return hbtDAO.obtenerViajesDeCliente(idCliente);
	}

	public float SeleccionarViaje(int idViaje) {
		return hbtDAO.seleccionarViaje(idViaje);
	}

	public List<ViajeDTO> obtenerViajes() throws RemoteException {
		return hbtDAO.obtenerViajes();
	}

	public List<SeguroDTO> obtenerSegurosParaCarga(String tipoMercaderia) {
		return hbtDAO.obtenerSegurosParaCarga(tipoMercaderia);
	}

	public List<VehiculoDTO> obtenerVehiculos() {
		return hbtDAO.obtenerVehiculos();
	}
	/*
	 * Aca mi idea era, ves una pantalla de vehiculos, si queres controlar el PM
	 * de uno, apretas en controlar o algo asi y se ejecuta esta funcion,
	 * devuelve true si necesitaba llevarlo a mantenimiento
	 */
	/*
	 * Solo se pueden llevar a mantenimiento los que estan en deposito, los que
	 * estan en Uso o en Mantenimiento no.
	 */

	/*
	 * La pantalla creo que es para cuando se rompe el vehiculo Y despues para
	 * mi con una funcion que se ejecute todos los dias y verifique cadael
	 * vehiculo ya estaria, la agregue como comentarios cualquier cosa la
	 * descomentamos y listo. La clase controlador tambien se usa para eso
	 * Igualmente use esta funcion para controlar
	 */

	public boolean ControlarVehiculo(VehiculoDTO vehiculoDTO) {
		/*
		 * Obtengo la fecha en la que deberian hacerle el mantenimiento al
		 * vehiculo
		 */
		Calendar c = Calendar.getInstance();
		c.setTime(vehiculoDTO.getFechaUltimoControl());
		c.add(Calendar.DATE, vehiculoDTO.getPlanDeMantenimiento().getDiasProxControl());
		Date fecha = c.getTime();

		boolean Boolean = false;
		if (vehiculoDTO.getEstado().equals("En Deposito")) {
			System.out.println(vehiculoDTO.getKilometraje() + vehiculoDTO.getPlanDeMantenimiento().getKmProxControl());
			if (vehiculoDTO.getKilometraje() >= vehiculoDTO.getPlanDeMantenimiento().getKmProxControl()) {
				Boolean = true;
				if (vehiculoDTO.isEnGarantia()) {
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				} else if (vehiculoDTO.isTrabajoEspecifico()) {
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				} else {
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				vehiculoDTO.getPlanDeMantenimiento().setKmProxControl(vehiculoDTO.getKilometraje() + 200);
				vehiculoDTO.getPlanDeMantenimiento().setDiasProxControl(60);
				hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculoDTO));
			}
			/*
			 * Me fijo si la fecha calculada al principio esta antes que la de
			 * hoy, si lo esta, mando mantenimiento
			 */
			else if (fecha.before(new Date())) {
				Boolean = true;
				if (vehiculoDTO.isEnGarantia()) {
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				} else if (vehiculoDTO.isTrabajoEspecifico()) {
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				} else {
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				vehiculoDTO.getPlanDeMantenimiento().setKmProxControl(vehiculoDTO.getKilometraje() + 200);
				vehiculoDTO.getPlanDeMantenimiento().setDiasProxControl(60);
				hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculoDTO));
			}
		}
		return Boolean;
	}

	public VehiculoDTO obtenerVehiculo(VehiculoDTO v) {
		return hbtDAO.obtenerVehiculo(v.getIdVehiculo());
	}

	public ViajeDTO obtenerViajePorVehiculo(VehiculoDTO vehiculo) {
		return hbtDAO.obtenerViajePorVehiculo(vehiculo);
	}

	public void actualiarViaje(ViajeDTO viajeDTO) {
		hbtDAO.updateViaje(EntityManager.ViajeToEntity(viajeDTO));
	}

	private List<PedidoDTO> obtenerPedidos(ClienteDTO c) {
		return hbtDAO.obtenerPedidosDeCliente(c.getIdCliente());
	}

	private List<ViajeDTO> obtenerViajesPorPedidos(List<PedidoDTO> pedidosDTO) {
		return hbtDAO.obtenerViajesDePedidos(pedidosDTO);
	}

	public List<ViajeDTO> controlarPedidosDeCliente(ClienteDTO c) {
		List<ViajeDTO> viajesDTO = new ArrayList<ViajeDTO>();
		List<PedidoDTO> pedidosDTO = obtenerPedidos(c);
		List<SucursalDTO> sucursales = hbtDAO.obtenerSucursales();
		/*
		 * for(SucursalDTO s:sucursales) {
		 * viajesDTo.add(obtenerViajesPorPedidos()); }
		 */
		viajesDTO = obtenerViajesPorPedidos(pedidosDTO);
		return viajesDTO;
	}

	// QUE RECIBA UN int como los minutos a demorar sino creo que es mas
	// complicado
	public void demorarViaje(ViajeDTO viajeDTO, int m) {

		long milisegundos = 60000;
		Date auxiliar = viajeDTO.getFechaLlegada();
		long minutosAux = m * milisegundos;
		Date auxiliar2 = new Date(auxiliar.getTime() + minutosAux);
		viajeDTO.setFechaLlegada(auxiliar2);
		hbtDAO.updateViaje(EntityManager.ViajeToEntity(viajeDTO));
	}

	// En realidad lo unico que tiene es el id que le paso el empleado
	public ViajeDTO obtenerViaje(ViajeDTO viajeDTO) {
		return hbtDAO.obtenerViaje(viajeDTO.getIdViaje());
	}
	// Hay que ver como manejamos el idSucursal porque se supone que el sistema
	// conoce
	// quien le hace el pedido entonces usa los trayectos cuyo origen es la
	// sucursal de origen
	//

	private List<TrayectoDTO> obtenerTodosLosTrayectos(SucursalDTO origen, SucursalDTO destino) {
		List<TrayectoDTO> trayectos = new ArrayList<TrayectoDTO>();

		for (RutaDTO r : mapadeRuta.getRutas()) {
			for (TrayectoDTO t : r.getTrayectos()) {
				if (t.getSucursalOrigen() == origen && t.getSucursalDestino() == destino) {
					trayectos.add(t);
				}
			}
		}
		return trayectos;
	}

	public static void cargarMapaDeRuta() {
		List<RutaDTO> rutas = hbtDAO.obtenerRutas();
		mapadeRuta = new MapaDeRutaDTO();
		mapadeRuta.setRutas(rutas);
	}

	// el Trayecto pasado como parametro tiene los nuevos valores de km ,
	// tiempo, y precio
	// Lo habia pensado como que solo se llama para decir que un trayecto no se
	// puede hacer pero el CU dice que se modifica
	// el precio, km ,etc entonces lo cambie. Y agregue que si el trayecto ahora
	// es mejor que antes entonces no cambio nada solamente
	// actualizo los tiempos

	public void actualizarViajes(TrayectoDTO trayDTO, SucursalDTO sucursalDTO) {
		cargarMapaDeRuta();
		TrayectoDTO aux = new TrayectoDTO();
		float btiempo = 99999999;
		// aux=hbtDAO.obtenerTrayecto(trayDTO);

		actualizarMapaDeRutas(trayDTO);
		List<RutaDTO> rutas = new ArrayList<RutaDTO>();
		rutas = obtenerTodasLasRutasDirectas(trayDTO.getSucursalOrigen());

		aux = new TrayectoDTO();
		for (RutaDTO r : rutas) {
			// Se que tiene un solo trayecto porque eso es lo que devuelve
			// obtenerTodasLasRutasDirectas
			TrayectoDTO t = r.getTrayectos().get(0);
			;
			float tiempo = 0;

			// Veo cual es la sucursal mas cercana en TIEMPO o si me conviene
			// volver a la sucursal de origen

			tiempo = t.getTiempo();
			if (tiempo < btiempo) {
				aux = t;
				btiempo = tiempo;
			}

		}

		rutas = obtenerTodasLasRutasDirectas(trayDTO.getSucursalDestino());
		for (RutaDTO r : rutas) {
			float tiempo = 0;
			TrayectoDTO t = r.getTrayectos().get(0);
			if (t.getSucursalDestino() == trayDTO.getSucursalOrigen()) {
				if (tiempo < btiempo) {
					aux = t;
					btiempo = tiempo;
				}
			}
		}

		// DEBERIA OBTENER LOS VIAJES SOLO DELA SUCURSAL DE ORIGEN PERO DEVUELVE
		// TODOS
		// ASIQ VERIFICO QUE LA SUCURSAL ORIGEN SEA LA INDICADA
		List<ViajeDTO> viajesDTO = hbtDAO.obtenerViajes();
		for (ViajeDTO v : viajesDTO) {
			if (v.getSucursalOrigen().getIdSucursal() == trayDTO.getSucursalOrigen().getIdSucursal()
					&& v.getSucursalDestino().getIdSucursal() == trayDTO.getSucursalDestino().getIdSucursal()) {
				v.setSucursalDestino(aux.getSucursalDestino());
				long m = (long) aux.getTiempo();
				long milisegundos = 60000;
				Date auxiliar = Calendar.getInstance().getTime();
				long minutosAux = m * milisegundos;
				Date auxiliar2 = new Date(auxiliar.getTime() + minutosAux);
				v.setFechaLlegada(auxiliar2);
				for (EnvioDTO e : v.getEnvios()) {
					e.setFechaLlegada(auxiliar2);
					hbtDAO.modificar(EntityManager.EnvioToEntity(e));
				}
				hbtDAO.modificar(EntityManager.ViajeToEntity(v));
			}

		}

	}

	public MapaDeRutaDTO getMapa() {
		return mapadeRuta;
	}

	private List<RutaDTO> obtenerTodasLasRutasDirectas(SucursalDTO sucursalOrigen) {
		MapaDeRutaDTO mp = mapadeRuta;
		List<RutaDTO> rutas = new ArrayList<RutaDTO>();
		for (RutaDTO r : mp.getRutas()) {
			if (r.getOrigen().getIdSucursal() == sucursalOrigen.getIdSucursal() && r.getTrayectos().size() == 1) {
				rutas.add(r);
			}
		}

		return rutas;
	}

	public List<TransporteDTO> obtenerTransportesDeTerceros(CargaDTO c, TransporteDTO tr) {
		return hbtDAO.obtenerTransportesDeTerceros(c, tr);
	}

	public void actualizarMapaDeRutas(TrayectoDTO t) {

		t.setSucursalOrigen(hbtDAO.obtenerSucursal(t.getSucursalOrigen()));
		t.setSucursalDestino(hbtDAO.obtenerSucursal(t.getSucursalDestino()));
		hbtDAO.modificar(EntityManager.TrayectoToEntity(t));
		cargarMapaDeRuta();
	}

	public void actualizarPedido(PedidoDTO pedido) {
		hbtDAO.modificar(EntityManager.PedidoToEntity(pedido));
	}

	public List<PedidoDTO> obtenerPedidos() {
		return hbtDAO.obtenerPedidos();
	}

	public List<ClienteDTO> obtenerClientes() {
		return hbtDAO.obtenerClientes();
	}

	public List<EmpresaDTO> obtenerClientesEmpresa() {
		return hbtDAO.obtenerClientesEmpresa();
	}

	public List<ParticularDTO> obtenerClientesParticular() {
		return hbtDAO.obtenerClientesParticular();
	}

	public List<CargaDTO> obtenerCargasDeUnPedido(PedidoDTO pedido) {
		return hbtDAO.obtenerCargasDeUnPedido(pedido);
	}

	public String validarCredenciales(String username, String password) {
		// TODO Auto-generated method stub

		return hbtDAO.validarCredenciales(username, password);
	}

	public void controlarEstadoDeEnvios() throws RemoteException {

		List<PedidoDTO> pedidos = hbtDAO.obtenerPedidos();
		List<EnvioDTO> envios = new ArrayList<EnvioDTO>();

		pedidos = ordenarPedidosPorPrioridad(pedidos);
		envios = obtenerEnvios();

		ArrayList<EnvioDTO> enviosPendientes = new ArrayList<EnvioDTO>();

		for (PedidoDTO pedido : pedidos) {

			EnvioDTO envioDto = hbtDAO.obtenerEnvioDePedido(pedido.getIdPedido());

			// Si no existe envio para el pedido se crea uno nuevo
			if (envioDto == null) {
				envioDto = crearNuevoEnvio(pedido, envios.size());
				envios.add(envioDto);
			}

			// Si el envio esta pendiente
			else if (envioDto.getEstado().equals("pendiente")) {
				enviosPendientes.add(envioDto);
				asignarEnviosPendientesATransporteLibre(enviosPendientes);
			}

			// Si el envio esta despachado
			else if (envioDto.getEstado().equals("despachado")) {
				controlarLLegadaDeEnvio(envioDto);
			}

			// El envio ya esta listo
			else if (envioDto.getEstado().equals("listo")) {

				// TODO: Que lo pasen a buscar. Ni idea

			}

		}

	}

	private void controlarLLegadaDeEnvio(EnvioDTO envioDto) {

		Date fechaActual = new Date();

		// Vamos a asumir que el envio siempre llega a tiempo a su destino.
		if (fechaActual.after(envioDto.getFechaLlegada())) {

			// Habria que guardar la sucursal destino en envio para que esto funcione
			// Si llego al sucursal destino del pedido.
			if (RemoteObjectHelper.obtenerSucursal(envioDto.getSucursalOrigen()).getIdSucursal()
					== RemoteObjectHelper.obtenerSucursal(envioDto.getPedido().getSucursalDestino()).getIdSucursal()) {
				
				envioDto.setEstado("listo");
				
				ViajeDTO viaje = hbtDAO.obtenerViajeDeEnvio(envioDto.getIdEnvio());
				viaje.setFinalizado(true);
				hbtDAO.modificar(EntityManager.ViajeToEntity(viaje));
				
				VehiculoDTO vehiculo = viaje.getVehiculo();
				vehiculo.setEstado("En Deposito");
				hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculo));
			}
			// Si llego al sucursal intermediario del pedido.
			else {
				envioDto.setEstado("pendiente");
				envioDto.setSucursalOrigen(envioDto.getSucursalOrigen());
			}
			hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));
		}

	}

	private EnvioDTO crearNuevoEnvio(PedidoDTO pedido, int envioId) throws RemoteException {

		EnvioDTO envioDto = new EnvioDTO(envioId, Calendar.getInstance().getTime(), null, true, "pendiente", pedido, 1);
		envioDto.setSucursalOrigen(pedido.getSucursalOrigen());
		hbtDAO.guardar(EntityManager.EnvioToEntity(envioDto));

		float cargaTotalPedido = 0;
		for (CargaDTO carga : pedido.getCargas()) {
			cargaTotalPedido = carga.getVolumen() + cargaTotalPedido;
		}

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper.obtenerVehiculosDisponibles();

		if (vehiculosDisponibles.size() > 0) {

			VehiculoDTO vehiculoDto = vehiculosDisponibles.get(0);

			if (cargaTotalPedido > (vehiculoDto.getVolumen() * 70) / 100
					&& cargaTotalPedido < vehiculoDto.getVolumen()) {
				List<EnvioDTO> envio = new ArrayList<EnvioDTO>();
				envio.add(envioDto);
				generarViaje(vehiculoDto, envio, true);
			}
		}

		return envioDto;
	}

	private void generarViaje(VehiculoDTO vehiculoDto, List<EnvioDTO> envios, boolean esNuevoEnvio) {

		vehiculoDto.setEstado("En uso");
		RutaDTO rutaVehiculo = RemoteObjectHelper.obtenerMejorRuta(
				RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalOrigen()),
				RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalDestino()));

		hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculoDto));

		ViajeDTO viajeDto = new ViajeDTO();
		viajeDto.setEnvios(envios);
		viajeDto.setSucursalOrigen(RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalOrigen()));
		viajeDto.setSucursalDestino(RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalDestino()));
		viajeDto.setVehiculo(vehiculoDto);
		viajeDto.setFinalizado(false);

		float tiempoRuta = 0;
		for (TrayectoDTO trayecto : rutaVehiculo.getTrayectos()) {
			tiempoRuta = trayecto.getTiempo() + tiempoRuta;
		}

		long minutosFechaLlegadaViaje = (long) tiempoRuta * 60000;
		Date fechaLlegadaViaje = new Date(Calendar.getInstance().getTime().getTime() + minutosFechaLlegadaViaje);
		viajeDto.setFechaLlegada(fechaLlegadaViaje);

		hbtDAO.guardar(EntityManager.ViajeToEntity(viajeDto));

		long minutosFechaLlegadaEnvio = (long) rutaVehiculo.getTrayectos().get(0).getTiempo() * 60000;
		Date fechaLlegadaEnvio = new Date(Calendar.getInstance().getTime().getTime() + minutosFechaLlegadaEnvio);

		for (EnvioDTO envioDto : envios) {
			envioDto.setEstado("despachado");
			envioDto.setFechaLlegada(fechaLlegadaEnvio);

			if (esNuevoEnvio) {
				hbtDAO.guardar(EntityManager.EnvioToEntity(envioDto));
			} else {
				hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));
			}
		}
	}

	private List<PedidoDTO> ordenarPedidosPorPrioridad(List<PedidoDTO> pedidos) {
		
		List<PedidoDTO> pedidosAux = new ArrayList<PedidoDTO>();

		// Ordeno pedidos por fecha de carga
		Collections.sort(pedidos, new Comparator<PedidoDTO>() {
			public int compare(PedidoDTO pedido1, PedidoDTO pedido2) {
				Date fechaCargaPedido1 = pedido1.getFechaCarga();
				Date fechaCargaPedido2 = pedido2.getFechaCarga();
				return fechaCargaPedido1.before(fechaCargaPedido2) ? -1 : 1;
			}
		});

		// Controlo si hay que enviar urgentemente un pedido
		for (int i = 0 ; i < pedidos.size() ; i++) {
			PedidoDTO pedido = pedidos.get(i);
			if (pedido != null) {
				pedidosAux.add(pedido);
				SucursalDTO sucursalOrigen = obtenerSucursal(pedido.getSucursalOrigen());
				SucursalDTO sucursalDestino = obtenerSucursal(pedido.getSucursalDestino());
				Date mejorFechaLLegada = calcularMejorFechaLlegada(sucursalOrigen, sucursalDestino);

				if (!pedido.getFechaMaxima().before(mejorFechaLLegada)) {
					enviarUrgente(pedido);
				}
			}
		}

		return pedidosAux;
	}
	
	// Esta mal. Deberia ser con ID
	public SucursalDTO obtenerSucursal(String nombre) {
		List<SucursalDTO> sucursales = hbtDAO.obtenerSucursales();
		for (SucursalDTO sucursal : sucursales) {
			if (sucursal.getNombre().equals(nombre)) {
				return sucursal;
			}
		}
		return null;
	}
	
	public Date calcularMejorFechaLlegada(SucursalDTO sucursalOrigen, SucursalDTO sucursalDestino) {

		RutaDTO mejorRuta = obtenerMejorRuta(sucursalOrigen, sucursalDestino);
		float tiempo = 0;

		for (TrayectoDTO trayecto : mejorRuta.getTrayectos()) {
			tiempo = trayecto.getTiempo() + tiempo;
		}

		Date fechaActual = Calendar.getInstance().getTime();
		long minutosRuta = (long) tiempo * 60000;
		return new Date(fechaActual.getTime() + minutosRuta);
	}
	
	public RutaDTO obtenerMejorRuta(SucursalDTO origen, SucursalDTO destino) {

		RutaDTO mejorRuta = null;
		float precioMin = -1;
		int kmMin = -1;
		
		List<RutaDTO> rutas = hbtDAO.obtenerRutas();
		for (RutaDTO ruta : rutas) {
			// Deberia ser con ID
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

	// TODO
	private void enviarUrgente(PedidoDTO pedido) {
		// TODO Auto-generated method stub

	}

	public void asignarEnviosPendientesATransporteLibre(ArrayList<EnvioDTO> enviosPendientes) {

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper.obtenerVehiculosDisponibles();

		if (vehiculosDisponibles.size() > 0) {

			VehiculoDTO vehiculoDisponible = vehiculosDisponibles.get(0);

			ArrayList<EnvioDTO> enviosPendientesConvenientes = RemoteObjectHelper
					.obtenerCombinacionEnviosPendientesMasConveniente(enviosPendientes,
							(vehiculoDisponible.getVolumen() * 70) / 100, vehiculoDisponible.getVolumen());

			if (enviosPendientesConvenientes.size() > 0) {
				generarViaje(vehiculoDisponible, enviosPendientesConvenientes, false);
			}
		}

	}

	public PedidoDTO obtenerPedido(int idPedido) throws RemoteException {
		return hbtDAO.obtenerPedido(idPedido);
	}

	public List<HabilitadoDTO> obtenerHabilitados() throws RemoteException {
		return hbtDAO.obtenerHabilitados();
	}

	public List<EnvioDTO> obtenerEnvios() throws RemoteException {
		return hbtDAO.obtenerEnvios();
	}

	public List<EnvioDTO> obtenerEnvios(String nombre) throws RemoteException {
		return hbtDAO.obtenerEnvios(nombre);
	}

	public void recibir(ViajeDTO v) {
		v = hbtDAO.obtenerViaje(v.getIdViaje());
		for (EnvioDTO e : v.getEnvios()) {
			if (Integer.parseInt(e.getPedido().getSucursalDestino()) == v.getSucursalDestino().getIdSucursal()) {
				for (CargaDTO c : e.getPedido().getCargas()) {
					c.setDespachado(true);
					hbtDAO.guardar(EntityManager.CargaToEntity(c));
				}

			} else
				e.setEstado("listo");
			v.setFinalizado(true);
		}
		VehiculoDTO veh = hbtDAO.obtenerVehiculo(v.getVehiculo().getIdVehiculo());
		veh.setEstado("En uso");

		hbtDAO.modificar(EntityManager.ViajeToEntity(v));
		hbtDAO.modificar(EntityManager.VehiculoToEntity(veh));

	}

	public List<TransporteDTO> obtenerTransportes() throws RemoteException {
		return hbtDAO.obtenerTransportes();
	}

	public ClienteDTO obtenerClientePorID(int id) {
		return hbtDAO.obtenerClientePorID(id);

	}

	@Override
	public void cargarDatosIniciales() throws RemoteException {

			entities.Empresa e=new Empresa();
			e.setNombre("Empresa 1");
			e.setCUIT(234234);
			e.setDetallePoliticas("Detalle Pol�tica 1");
			e.setTipo("Tipo 1");
			e.setSaldoCuentaCorriente(15000);
			hbtDAO.guardar(e);
			
			entities.Particular p=new Particular();
			p.setNombre("Elio");
			p.setApellido("Mollo");
			p.setDNI(9418723);
			hbtDAO.guardar(p);
			
			entities.Direccion dO=new entities.Direccion();
			dO.setCalle("Av. Rigolleau");
			dO.setCP("1884");
			dO.setDepartamento("F");
			dO.setNumero(1405);
			dO.setPiso(2);
			hbtDAO.guardar(dO);
			
			entities.Direccion dD=new entities.Direccion();
			dD.setCalle("Av. Mitre");
			dD.setCP("1883");
			dD.setDepartamento("A");
			dD.setNumero(9230);
			dD.setPiso(2);
			hbtDAO.guardar(dD);
			
			entities.Pedido pedido=new entities.Pedido();
			pedido.setCliente(p);
			pedido.setDireccionCarga(dO);
			pedido.setDireccionDestino(dD);
			Date hoy=new Date();
			pedido.setFechaCarga(hoy);
			Date fmax=new Date(hoy.getTime() + (1000 * 60 * 60 * 24));
			pedido.setFechaMaxima(fmax);
			pedido.setHoraInicio(12);
			pedido.setHoraFin(13);
			pedido.setPrecio(1500);
			pedido.setSucursalOrigen("Berazategui");
			pedido.setSucursalDestino("Quilmes");
			pedido.setSolicitaAvionetaParticular(false);
			pedido.setSolicitaTransporteDirecto(false);
			ArrayList<entities.Carga> cargas=new ArrayList<entities.Carga>();
			entities.Carga carga=new entities.Carga();
			carga.setAlto(1);
			carga.setAncho(2);
			carga.setApilable(4);
			carga.setCondiciones("Apilable");
			carga.setDespachado(true);
			carga.setFragilidad("Normal");
			carga.setMercaderia("Electr�nico");
			carga.setPeso(20);
			carga.setProfundidad(1);
			carga.setRefrigerable(false);
			carga.setTratamiento("Normal");
			carga.setVolumen(carga.getAlto()*carga.getAncho()*carga.getProfundidad());
			hbtDAO.guardar(carga);
			cargas.add(carga);
			entities.Carga carga2=new entities.Carga();
			carga2.setAlto(1);
			carga2.setAncho(2);
			carga2.setApilable(2);
			carga2.setCondiciones("No apilable");
			carga2.setDespachado(true);
			carga2.setFragilidad("Normal");
			carga2.setMercaderia("Electr�nico");
			carga2.setPeso(30);
			carga2.setProfundidad(1);
			carga2.setRefrigerable(false);
			carga2.setTratamiento("Normal");
			carga2.setVolumen(carga2.getAlto()*carga2.getAncho()*carga2.getProfundidad());
			cargas.add(carga2);
			hbtDAO.guardar(carga2);
			pedido.setCargas(cargas);
			hbtDAO.guardar(pedido);
			
			Carga carga3=new Carga();
			carga3.setAlto(2);
			carga3.setAncho(6);
			carga3.setApilable(0);
			carga3.setCondiciones("A granel");
			carga3.setDespachado(false);
			carga3.setFragilidad("Normal");
			carga3.setMercaderia("A Granel");
			carga3.setPeso(3000);
			carga3.setProfundidad(6);
			carga3.setRefrigerable(false);
			carga3.setTratamiento("Normal");
			carga3.setVolumen(carga3.getAlto()*carga3.getAncho()*carga3.getProfundidad());
			hbtDAO.guardar(carga3);
			Carga carga4=new Carga();
			carga4.setAlto(2);
			carga4.setAncho(4);
			carga4.setApilable(0);
			carga4.setCondiciones("A granel");
			carga4.setDespachado(false);
			carga4.setFragilidad("Normal");
			carga4.setMercaderia("A Granel");
			carga4.setPeso(100);
			carga4.setProfundidad(6);
			carga4.setRefrigerable(false);
			carga4.setTratamiento("Normal");
			carga4.setVolumen(carga4.getAlto()*carga4.getAncho()*carga4.getProfundidad());
			hbtDAO.guardar(carga4);
			
			PlanDeMantenimiento pm=new PlanDeMantenimiento();
			pm.setDiasDemora(0);
			pm.setDiasProxControl(0);
			pm.setKmProxControl(0);
			hbtDAO.guardar(pm);
			
			PlanDeMantenimiento pm1=new PlanDeMantenimiento();
			pm1.setDiasDemora(1);
			pm1.setDiasProxControl(28);
			pm1.setKmProxControl(20000);
			hbtDAO.guardar(pm1);
			
			PlanDeMantenimiento pm2=new PlanDeMantenimiento();
			pm2.setDiasDemora(2);
			pm2.setDiasProxControl(30);
			pm2.setKmProxControl(100000);
			hbtDAO.guardar(pm2);
			
			Vehiculo v=new Vehiculo();
			v.setAlto(2);
			v.setAncho(2);
			v.setProfundidad(3);
			v.setVolumen(v.getAlto()*v.getAncho()*v.getProfundidad());
			v.setEnGarantia(true);
			v.setEstado("Libre");
			v.setFechaUltimoControl(null);
			v.setKilometraje(10200);
			v.setPeso(3500);
			v.setTara(1500);
			v.setTipo("Propio");
			v.setTrabajoEspecifico(false);
			v.setPlanDeMantenimiento(pm);
			hbtDAO.guardar(v);
			
			Vehiculo v2=new Vehiculo();
			v2.setAlto(4);
			v2.setAncho(2);
			v2.setProfundidad(8);
			v2.setVolumen(v.getAlto()*v.getAncho()*v.getProfundidad());
			v2.setEnGarantia(true);
			v2.setEstado("Contrado");
			v2.setFechaUltimoControl(null);
			v2.setKilometraje(90000);
			v2.setPeso(10000);
			v2.setTara(3000);
			v2.setTipo("Tercero");
			v2.setTrabajoEspecifico(true);
			v2.setPlanDeMantenimiento(pm);
			hbtDAO.guardar(v2);
			
			datosInicialesParaEnvios();
			
			
		}

	private void datosInicialesParaEnvios() {
		
		System.out.println("-----Cargando Datos Iniciales Para Envios-----");
		
		Direccion direccionA = new Direccion(1, "Calle 16", 5402, 1, "F", "1500");
		hbtDAO.guardar(direccionA);
		Sucursal sucursal1 = new Sucursal(1, "Berazategui", direccionA, null);
		hbtDAO.guardar(sucursal1);

		Direccion direccionB = new Direccion(2, "Calle 18", 1000, 1, "H", "1800");
		hbtDAO.guardar(direccionB);
		Sucursal sucursal2 = new Sucursal(2, "San Isidro", direccionB, null);
		hbtDAO.guardar(sucursal2);

		Direccion direccionC = new Direccion(3, "Calle 20", 1000, 1, "H", "2000");
		hbtDAO.guardar(direccionC);
		Sucursal sucursal3 = new Sucursal(3, "Quilmes", direccionC, null);
		hbtDAO.guardar(sucursal3);
		
		List<Trayecto> trayectosA = new ArrayList<Trayecto>();
		List<Trayecto> trayectosB = new ArrayList<Trayecto>();
		
		Trayecto trayectoA = new Trayecto(1, sucursal1, sucursal2, 400, 400, 200);
		Trayecto trayectoB = new Trayecto(2, sucursal2, sucursal3, 400, 400, 200);
		trayectosA.add(trayectoA);
		trayectosB.add(trayectoB);
		hbtDAO.guardar(trayectoA);
		hbtDAO.guardar(trayectoB);
		
		Trayecto trayectoC = new Trayecto(3, sucursal1, sucursal2, 200, 200, 100);
		Trayecto trayectoD = new Trayecto(4, sucursal2, sucursal3, 200, 200, 100);
		trayectosB.add(trayectoC);
		trayectosB.add(trayectoD);
		hbtDAO.guardar(trayectoC);
		hbtDAO.guardar(trayectoD);
		
		Ruta rutaA = new Ruta(1, trayectosA, 300, sucursal1, sucursal3);
		hbtDAO.guardar(rutaA);
		Ruta rutaB = new Ruta(2, trayectosB, 200, sucursal1, sucursal3);
		hbtDAO.guardar(rutaB);
		
		Vehiculo vehiculoA = new Vehiculo(1, "camion", 500, 200, 200, 200, 200, 200, 200, "En Deposito", true, false, new Date(), null);
		hbtDAO.guardar(vehiculoA);
		
	}

	@Override
	public void altaClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		// TODO Auto-generated method stub
		Empresa e = new Empresa();
		// e=EmpresaToEntity(empresaDto);
		e.setCUIT(empresaDto.getCUIT());
		e.setDetallePoliticas(empresaDto.getDetallePoliticas());
		e.setNombre(empresaDto.getNombre());
		// e.setProductos(productos);
		e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		e.setTipo(empresaDto.getTipo());
		hbtDAO.guardar(e);
	}

	@Override
	public void altaClienteParticular(ParticularDTO particularDto) throws RemoteException {
		// TODO Auto-generated method stub
		Particular c = new Particular();
		c.setApellido(particularDto.getApellido());
		c.setDNI(particularDto.getDNI());
		c.setNombre(particularDto.getNombre());
		hbtDAO.guardar(c);
	}

	@Override
	public void updateClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		// TODO Auto-generated method stub
		Empresa e = new Empresa();
		e.setIdCliente(empresaDto.getIdCliente());
		e.setCUIT(empresaDto.getCUIT());
		e.setDetallePoliticas(empresaDto.getDetallePoliticas());
		e.setNombre(empresaDto.getNombre());
		e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		e.setTipo(empresaDto.getTipo());
		hbtDAO.modificar(e);
	}

	public void updateClienteParticular(ParticularDTO particularDto) throws RemoteException {
		// TODO Auto-generated method stub
		Particular p = new Particular();
		p.setIdCliente(particularDto.getIdCliente());
		p.setApellido(particularDto.getApellido());
		p.setDNI(particularDto.getDNI());
		p.setNombre(particularDto.getNombre());
		hbtDAO.modificar(p);
	}

	public void deleteClienteEmpresa(int idCliente) throws RemoteException {
		Empresa e = new Empresa();
		e.setIdCliente(idCliente);
		hbtDAO.borrar(e);
	}

	public void deleteClienteParticular(int idCliente) throws RemoteException {
		Particular p = new Particular();
		p.setIdCliente(idCliente);
		hbtDAO.borrar(p);

	}

	@Override
	public List<CargaDTO> listarCargas() throws RemoteException {
		return hbtDAO.listarCargas();
	}

	// Sucursal

	public List<SucursalDTO> obtenerSucursales() throws RemoteException {
		return hbtDAO.obtenerSucursales();
	}

	@Override
	public void altaSucursal(SucursalDTO sucursalDto) throws RemoteException {
		Sucursal sucursal = new Sucursal();
		sucursal = EntityManager.SucursalToEntity(sucursalDto);
		hbtDAO.guardar(sucursal);
	}

	@Override
	public void deleteSucursal(int idSucursal) throws RemoteException {
		Sucursal sucursal = new Sucursal();
		sucursal.setIdSucursal(idSucursal);
		hbtDAO.borrar(sucursal);
	}

	@Override
	public void updateSucursal(SucursalDTO sucursalDto) throws RemoteException {
		Sucursal sucursal = new Sucursal();
		sucursal.setIdSucursal(sucursalDto.getIdSucursal());
		sucursal.setNombre(sucursalDto.getNombre());
		sucursal.setUbicacion(EntityManager.DireccionToEntity(sucursalDto.getUbicacion()));

		List<Viaje> viajes = new ArrayList<Viaje>();
		for (ViajeDTO viaje : sucursalDto.getViajes()) {
			viajes.add(EntityManager.ViajeToEntity(viaje));
		}
		sucursal.setViajes(viajes);

		hbtDAO.modificar(sucursal);
	}

	// Trayecto

	@Override
	public List<TrayectoDTO> obtenerTrayectos() throws RemoteException {
		// Mirar obtenerTodosLosTrayectos(). Puede ser que sirva hacerlo de esta
		// manera en el futuro
		return hbtDAO.obtenerTrayectos();
	}

	@Override
	public void altaTrayecto(TrayectoDTO trayectDto) throws RemoteException {
		Trayecto trayecto = new Trayecto();
		trayecto = EntityManager.TrayectoToEntity(trayectDto);
		hbtDAO.guardar(trayecto);
	}

	@Override
	public void updateTrayecto(TrayectoDTO trayectDto) throws RemoteException {
		Trayecto trayecto = new Trayecto();
		trayecto.setIdTrayecto(trayectDto.getIdTrayecto());
		trayecto.setKm(trayectDto.getKm());
		trayecto.setPrecio(trayectDto.getPrecio());
		trayecto.setSucursalDestino(EntityManager.SucursalToEntity(trayectDto.getSucursalDestino()));
		trayecto.setSucursalOrigen(EntityManager.SucursalToEntity(trayectDto.getSucursalOrigen()));
		trayecto.setTiempo(trayectDto.getTiempo());
		hbtDAO.modificar(trayecto);
	}

	@Override
	public void deleteTrayecto(int idTrayecto) throws RemoteException {
		Trayecto trayecto = new Trayecto();
		trayecto.setIdTrayecto(idTrayecto);
		hbtDAO.borrar(trayecto);
	}

	// Ruta

	@Override
	public List<RutaDTO> obtenerRutas() throws RemoteException {
		return hbtDAO.obtenerRutas();
	}

	@Override
	public void altaRuta(RutaDTO rutaDto) throws RemoteException {
		Ruta ruta = new Ruta();
		ruta = EntityManager.RutaToEntity(rutaDto);
		hbtDAO.guardar(ruta);
	}

	@Override
	public void updateRuta(RutaDTO rutaDto) throws RemoteException {
		Ruta ruta = new Ruta();
		ruta.setIdRuta(rutaDto.getIdRuta());
		ruta.setPrecio(rutaDto.getPrecio());

		List<Trayecto> trayectos = new ArrayList<Trayecto>();
		for (TrayectoDTO trayecto : rutaDto.getTrayectos()) {
			trayectos.add(EntityManager.TrayectoToEntity(trayecto));
		}
		ruta.setTrayectos(trayectos);

		hbtDAO.modificar(ruta);
	}

		@Override
		public void deleteRuta(int idRuta) throws RemoteException {
			Ruta ruta = new Ruta();	
			ruta.setIdRuta(idRuta);
			hbtDAO.borrar(ruta);
		}
		
		@Override
		public void createCarga(CargaDTO cd) throws RemoteException {
			Carga c=new Carga();
			c.setAlto(cd.getAlto());
			c.setAncho(cd.getAncho());
			c.setApilable(cd.getApilable());
			c.setCondiciones(cd.getCondiciones());
			c.setDespachado(cd.isDespachado());
			c.setFragilidad(cd.getFragilidad());
			c.setMercaderia(cd.getTipoMercaderia());
			c.setPeso(cd.getPeso());
			c.setProfundidad(cd.getProfundidad());
			c.setRefrigerable(cd.isRefrigerable());
			c.setTratamiento(cd.getTratamiento());
			c.setVolumen(cd.getVolumen());
			hbtDAO.guardar(c);
		}


		@Override
		public void updateCarga(CargaDTO cd) throws RemoteException {
			// TODO Auto-generated method stub
			Carga c=new Carga();
			c.setIdCarga(cd.getIdCarga());
			c.setAlto(cd.getAlto());
			c.setAncho(cd.getAncho());
			c.setApilable(cd.getApilable());
			c.setCondiciones(cd.getCondiciones());
			c.setDespachado(cd.isDespachado());
			c.setFragilidad(cd.getFragilidad());
			c.setMercaderia(cd.getTipoMercaderia());
			c.setPeso(cd.getPeso());
			c.setProfundidad(cd.getProfundidad());
			c.setRefrigerable(cd.isRefrigerable());
			c.setTratamiento(cd.getTratamiento());
			c.setVolumen(cd.getVolumen());
			hbtDAO.modificar(c);
		}


		@Override
		public void deleteCarga(int idCarga) throws RemoteException {
			// TODO Auto-generated method stub
			Carga c=new Carga();
			c.setIdCarga(idCarga);
			hbtDAO.borrar(c);			
		}


		@Override
		public void crearPedido(PedidoDTO pe) throws RemoteException {
			// TODO Auto-generated method stub
			Pedido p = new Pedido();
			Direccion dC = new Direccion();
			Direccion dD = new Direccion();
			dC.setCalle(pe.getDireccionCarga().getCalle());
			dC.setCP(pe.getDireccionCarga().getCP());
			dC.setDepartamento(pe.getDireccionCarga().getDepartamento());
			dC.setNumero(pe.getDireccionCarga().getNumero());
			dC.setPiso(pe.getDireccionCarga().getPiso());

			dD.setCalle(pe.getDireccionDestino().getCalle());
			dD.setCP(pe.getDireccionDestino().getCP());
			dD.setDepartamento(pe.getDireccionDestino().getDepartamento());
			dD.setNumero(pe.getDireccionDestino().getNumero());
			dD.setPiso(pe.getDireccionDestino().getPiso());

			hbtDAO.guardar(dD);
			hbtDAO.guardar(dC);
			ArrayList<Carga> cargas=new ArrayList<Carga>();
			for (CargaDTO c: pe.getCargas()){
				Carga carg=EntityManager.CargaToEntity(c);
				carg.setDespachado(true);
				cargas.add(carg);
			}

			p.setCargas(cargas);
			List<EmpresaDTO> empresas = hbtDAO.getInstancia().obtenerClientesEmpresa();
			for (EmpresaDTO e : empresas) {
				if (e.getIdCliente() == pe.getCliente().getIdCliente()) {
					Empresa empresa = new Empresa();
					empresa.setIdCliente(e.getIdCliente());
					p.setCliente(empresa);
				}
			}
			List<ParticularDTO> particulares = hbtDAO.getInstancia().obtenerClientesParticular();
			for (ParticularDTO pa : particulares) {
				if (pa.getIdCliente() == pe.getCliente().getIdCliente()) {
					Particular particular = new Particular();
					particular.setIdCliente(pe.getCliente().getIdCliente());
					p.setCliente(particular);
				}
			}
			p.setFechaCarga(pe.getFechaCarga());
			p.setFechaMaxima(pe.getFechaMaxima());
			p.setHoraInicio(pe.getHoraInicio());
			p.setHoraFin(pe.getHoraFin());
			
			p.setDireccionCarga(dC);
			p.setDireccionDestino(dD);
			p.setPrecio(pe.getPrecio());
			p.setSucursalOrigen(pe.getSucursalOrigen());
			p.setSucursalDestino(pe.getSucursalDestino());
			p.setSolicitaAvionetaParticular(pe.isSolicitaAvionetaParticular());
			p.setSolicitaTransporteDirecto(pe.isSolicitaTransporteDirecto());
			hbtDAO.guardar(p);

		}

		@Override
		public CargaDTO buscarCargaPorId(int idCarga) throws RemoteException {
			// TODO Auto-generated method stub
			
			return hbtDAO.buscarCargaPorId(idCarga);
		}


		@Override
		public void crearVehiculo(VehiculoDTO v) throws RemoteException {
			// TODO Auto-generated method stub
			hbtDAO.guardar(EntityManager.VehiculoToEntity(v));
		}


		@Override
		public void modificarVehiculo(VehiculoDTO v) throws RemoteException {
			// TODO Auto-generated method stub
			hbtDAO.modificar(EntityManager.VehiculoToEntity(v));
		}


		@Override
		public void eliminarVehiculo(VehiculoDTO v) throws RemoteException {
			// TODO Auto-generated method stub
			hbtDAO.borrar(EntityManager.VehiculoToEntity(v));
		}


		@Override
		public List<CargaDTO> listarCargasSinDespachar() throws RemoteException {
			// TODO Auto-generated method stub
			return hbtDAO.listarCargasSinDespachar();
		}


		@Override
		public List<DireccionDTO> listarDirecciones() throws RemoteException {
			// TODO Auto-generated method stub
			return hbtDAO.obtenerDirecciones();
		}


		@Override
		public void crearDireccion(DireccionDTO d) throws RemoteException {
			// TODO Auto-generated method stub
			hbtDAO.guardar(EntityManager.DireccionToEntity(d));
		}


		@Override
		public void modificarDireccion(DireccionDTO d) throws RemoteException {
			// TODO Auto-generated method stub
			hbtDAO.borrar(EntityManager.DireccionToEntity(d));
		}


		@Override
		public void eliminarDireccion(DireccionDTO d) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

}
