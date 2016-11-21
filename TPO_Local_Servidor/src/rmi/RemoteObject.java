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
import dto.PrecioVehiculoDTO;
import dto.ProductoDTO;
import dto.ProveedorDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SeguroDTO;
import dto.SucursalDTO;
import dto.TransporteDTO;
import dto.TrayectoDTO;
import dto.VehiculoAMantenerDTO;
import dto.VehiculoDTO;
import dto.ViajeDTO;
import entities.Carga;
import entities.Cliente;
import entities.Direccion;
import entities.Empresa;
import entities.Envio;
import entities.Habilitado;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.PrecioVehiculo;
import entities.Producto;
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
	private RemoteObjectHelper remoteObjectHelper;

	public void InicializarMapaDeRuta() {
		cargarMapaDeRuta();
	}

	public RemoteObject() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
		remoteObjectHelper = new RemoteObjectHelper();
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

	public boolean ControlarVehiculo(VehiculoDTO vehiculoDTO) {

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

	// En realidad lo unico que tiene es el id que le paso el empleado
	public ViajeDTO obtenerViaje(ViajeDTO viajeDTO) {
		return hbtDAO.obtenerViaje(viajeDTO.getIdViaje());
	}

	public static void cargarMapaDeRuta() {
		List<RutaDTO> rutas = hbtDAO.obtenerRutas();
		mapadeRuta = new MapaDeRutaDTO();
		mapadeRuta.setRutas(rutas);
	}

	public List<TransporteDTO> obtenerTransportesDeTerceros(CargaDTO c, TransporteDTO tr) {
		return hbtDAO.obtenerTransportesDeTerceros(c, tr);
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
		return hbtDAO.validarCredenciales(username, password);
	}

	public void controlarEstadoDeEnvios() throws RemoteException {

		List<PedidoDTO> pedidos = obtenerPedidos();
		pedidos = RemoteObjectHelper.ordenarPedidosPorPrioridad(pedidos);
		controlarPedidosUrgentes(pedidos);

		List<EnvioDTO> envios = new ArrayList<EnvioDTO>();
		envios = obtenerEnvios();

		ArrayList<EnvioDTO> enviosPendientes = new ArrayList<EnvioDTO>();

		for (PedidoDTO pedido : pedidos) {

			EnvioDTO envioDto = hbtDAO.obtenerEnvioDePedido(pedido.getIdPedido());

			// Si no existe envio para el pedido se crea uno nuevo
			if (envioDto == null) {
				System.out.println("-----Creando Nuevo Envio-----");
				envioDto = crearNuevoEnvio(pedido, envios.size());
				envios.add(envioDto);
			}

			// Si el envio esta despachado
			else if (envioDto.getEstado().equals("despachado")) {
				System.out.println("-----Controlando Llegada de Envio-----");
				controlarLLegadaDeEnvio(envioDto);
			}

			// Si el envio esta pendiente
			else if (envioDto.getEstado().equals("pendiente")) {
				System.out.println("-----Controlando Envios Pendientes-----");
				enviosPendientes.add(envioDto);
				asignarEnviosPendientesATransporteLibre(enviosPendientes);
			}

			// Si el envio esta pendiente
			else if (envioDto.getEstado().equals("listo")) {
				System.out.println("-----Envio Esta Listo-----");
			}
		}
	}

	private void controlarLLegadaDeEnvio(EnvioDTO envioDto) throws RemoteException {

		Date fechaActual = new Date();

		// Vamos a asumir que el envio siempre llega a tiempo a su destino.
		if (true) {
			// if (fechaActual.after(envioDto.getFechaLlegada())) {

			ViajeDTO viajeDelEnvio = RemoteObjectHelper.obtenerViajeDelEnvio(envioDto);
			if (viajeDelEnvio != null) {
				// Si llego al sucursal destino del pedido.
				if (RemoteObjectHelper.obtenerSucursal(envioDto.getSucursalDestino())
						.getIdSucursal() == RemoteObjectHelper
								.obtenerSucursal(envioDto.getPedido().getSucursalDestino()).getIdSucursal()) {

					VehiculoDTO vehiculo = viajeDelEnvio.getVehiculo();
					vehiculo.setEstado("Libre");
					hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculo));

					envioDto.setEstado("listo");
					envioDto.setFechaLlegada(fechaActual);
					hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));

					System.out.println("-----Envio llego a destino Final-----");
				}
				// Si llego al sucursal intermediario del pedido.
				else {
					RutaDTO rutaViaje = RemoteObjectHelper.obtenerMejorRuta(viajeDelEnvio.getSucursalOrigen(),
							viajeDelEnvio.getSucursalDestino());
					envioDto.setSucursalOrigen(envioDto.getSucursalDestino());
					envioDto.setSucursalDestino(
							rutaViaje.getNextSucursal(RemoteObjectHelper.obtenerSucursal(envioDto.getSucursalDestino()))
									.getNombre());
					hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));
					System.out.println("-----Envio llego a sucursal intermediaria-----");
				}
			}
		}

	}

	private EnvioDTO crearNuevoEnvio(PedidoDTO pedido, int envioId) throws RemoteException {

		EnvioDTO envioDto = new EnvioDTO(envioId, Calendar.getInstance().getTime(), null, true, "pendiente", pedido, 1);
		envioDto.setSucursalOrigen(pedido.getSucursalOrigen());

		float cargaTotalPedido = 0;
		for (CargaDTO carga : pedido.getCargas()) {
			cargaTotalPedido = carga.getVolumen() + cargaTotalPedido;
		}

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper.obtenerVehiculosDisponibles();

		if (vehiculosDisponibles.size() > 0) {

			for (VehiculoDTO vehiculoDto : vehiculosDisponibles) {
				if (cargaTotalPedido > (vehiculoDto.getVolumen() * 70) / 100
						&& cargaTotalPedido < vehiculoDto.getVolumen()) {
					List<EnvioDTO> envio = new ArrayList<EnvioDTO>();
					envio.add(envioDto);
					generarViaje(vehiculoDto, envio, true);
					break;
				}
			}

		}

		return envioDto;
	}

	private void generarViaje(VehiculoDTO vehiculoDto, List<EnvioDTO> envios, boolean esNuevoEnvio) {

		vehiculoDto.setEstado("En viaje");
		RutaDTO rutaVehiculo = RemoteObjectHelper.obtenerMejorRuta(
				RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalOrigen()),
				RemoteObjectHelper.obtenerSucursal(envios.get(0).getPedido().getSucursalDestino()));

		hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculoDto));

		ViajeDTO viajeDto = new ViajeDTO();
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

		long minutosFechaLlegadaEnvio = (long) rutaVehiculo.getTrayectos().get(0).getTiempo() * 60000;
		Date fechaLlegadaEnvio = new Date(Calendar.getInstance().getTime().getTime() + minutosFechaLlegadaEnvio);

		for (EnvioDTO envioDto : envios) {
			envioDto.setEstado("despachado");
			envioDto.setFechaLlegada(fechaLlegadaEnvio);
			envioDto.setSucursalDestino(rutaVehiculo.getTrayectos().get(0).getSucursalDestino().getNombre());
			if (esNuevoEnvio) {
				System.out.println("-----Envio Creado-----");
			} else {
				System.out.println("-----Envio pendiente asignado a un Vehiculo-----");
				hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));
			}
		}
		viajeDto.setEnvios(envios);

		hbtDAO.guardar(EntityManager.ViajeToEntity(viajeDto));
	}

	public void controlarPedidosUrgentes(List<PedidoDTO> pedidos) {
		for (int i = 0; i < pedidos.size(); i++) {
			PedidoDTO pedido = pedidos.get(i);
			if (pedido != null) {
				SucursalDTO sucursalOrigen = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalOrigen());
				SucursalDTO sucursalDestino = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalDestino());
				Date mejorFechaLLegada = RemoteObjectHelper.calcularMejorFechaLlegada(sucursalOrigen, sucursalDestino);

				if (!pedido.getFechaMaxima().before(mejorFechaLLegada)) {
					enviarUrgente(pedido);
				}
			}
		}
	}

	// TODO
	private void enviarUrgente(PedidoDTO pedido) {

	}

	public void asignarEnviosPendientesATransporteLibre(ArrayList<EnvioDTO> enviosPendientes) {

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper.obtenerVehiculosDisponibles();

		if (vehiculosDisponibles.size() > 0) {

			for (VehiculoDTO vehiculoDisponible : vehiculosDisponibles) {
				ArrayList<EnvioDTO> enviosPendientesConvenientes = RemoteObjectHelper
						.obtenerCombinacionEnviosPendientesMasConveniente(enviosPendientes,
								(vehiculoDisponible.getVolumen() * 70) / 100, vehiculoDisponible.getVolumen());

				if (enviosPendientesConvenientes.size() > 0) {
					generarViaje(vehiculoDisponible, enviosPendientesConvenientes, false);
					break;
				}
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

	public List<TransporteDTO> obtenerTransportes() throws RemoteException {
		return hbtDAO.obtenerTransportes();
	}

	public ClienteDTO obtenerClientePorID(int id) {
		return hbtDAO.obtenerClientePorID(id);
	}
	
	// Datos Iniciales

	@Override
	public void cargarDatosIniciales() throws RemoteException {

		/*Producto prod=new Producto();
		prod.setNombre("Electrodomésticos");
		prod.setTipo("Electrónico");
		hbtDAO.guardar(prod);
		Producto prod2=new Producto();
		prod2.setNombre("Cereal");
		prod2.setTipo("A Granel");
		hbtDAO.guardar(prod2);
		
		ArrayList<Producto> prods=new ArrayList<Producto>();
		prods.add(prod2);*/
		
		entities.Empresa e = new Empresa();
		e.setNombre("Distribución BS AS SA");
		e.setCUIT(2342342);
		e.setDetallePoliticas("Detalle Polítca");
		e.setTipo("SA");
		e.setSaldoCuentaCorriente(15000);
		//e.setProductos(prods);
		hbtDAO.guardar(e);
		
		entities.Empresa e2 = new Empresa();
		e2.setNombre("Fabella SRL");
		e2.setCUIT(234234);
		e2.setDetallePoliticas("Detalle Polítca");
		e2.setTipo("SRL");
		e2.setSaldoCuentaCorriente(15000);
		hbtDAO.guardar(e2);
		
	
		
		
		Habilitado h=new Habilitado();
		h.setDniHabilitado(String.valueOf("9418723"));
		h.setNombre("REBA");
		hbtDAO.guardar(h);
		ArrayList<Habilitado> hs=new ArrayList<Habilitado>();
		hs.add(h);
		entities.Particular p = new Particular();
		p.setNombre("Elio");
		p.setApellido("Mollo");
		p.setDNI(9418723);
		p.setHabilitados(hs);
		hbtDAO.guardar(p);
		entities.Particular p2 = new Particular();
		p2.setNombre("Felipe");
		p2.setApellido("Mart");
		p2.setDNI(2303040);
		hbtDAO.guardar(p2);
	
		

		entities.Direccion dO = new entities.Direccion();
		dO.setCalle("Av. Rigolleau");
		dO.setCP("1884");
		dO.setDepartamento("F");
		dO.setNumero(1405);
		dO.setPiso(2);
		hbtDAO.guardar(dO);

		Sucursal so = new Sucursal();
		so.setNombre("Sucursal Berazategui");
		so.setUbicacion(dO);
		hbtDAO.guardar(so);

		entities.Direccion dD = new entities.Direccion();
		dD.setCalle("Av. Mitre");
		dD.setCP("1883");
		dD.setDepartamento("A");
		dD.setNumero(9230);
		dD.setPiso(2);
		hbtDAO.guardar(dD);

		Sucursal s = new Sucursal();
		s.setNombre("Sucursal Quilmes");
		s.setUbicacion(dD);
		hbtDAO.guardar(s);

		entities.Pedido pedido = new entities.Pedido();
		pedido.setCliente(p);
		pedido.setDireccionCarga(dO);
		pedido.setDireccionDestino(dD);
		Date hoy = new Date();
		pedido.setFechaCarga(hoy);
		Date fmax = new Date(hoy.getTime() + (1000 * 60 * 60 * 24));
		pedido.setFechaMaxima(fmax);
		pedido.setHoraInicio(12);
		pedido.setHoraFin(13);
		pedido.setPrecio(1500);
		pedido.setSucursalOrigen("Berazategui");
		pedido.setSucursalDestino("Quilmes");
		pedido.setSolicitaAvionetaParticular(false);
		pedido.setSolicitaTransporteDirecto(false);
		ArrayList<entities.Carga> cargas = new ArrayList<entities.Carga>();
		entities.Carga carga = new entities.Carga();
		carga.setAlto(1);
		carga.setAncho(2);
		carga.setApilable(4);
		carga.setCondiciones("Apilable");
		carga.setDespachado(true);
		carga.setFragilidad("Normal");
		carga.setMercaderia("Electrónico");
		carga.setPeso(20);
		carga.setProfundidad(1);
		carga.setRefrigerable(false);
		carga.setTratamiento("Normal");
		carga.setVolumen(carga.getAlto() * carga.getAncho() * carga.getProfundidad());
		hbtDAO.guardar(carga);
		cargas.add(carga);
		entities.Carga carga2 = new entities.Carga();
		carga2.setAlto(1);
		carga2.setAncho(2);
		carga2.setApilable(2);
		carga2.setCondiciones("No apilable");
		carga2.setDespachado(true);
		carga2.setFragilidad("Normal");
		carga2.setMercaderia("Electrónico");
		carga2.setPeso(30);
		carga2.setProfundidad(1);
		carga2.setRefrigerable(false);
		carga2.setTratamiento("Normal");
		carga2.setVolumen(carga2.getAlto() * carga2.getAncho() * carga2.getProfundidad());
		cargas.add(carga2);
		hbtDAO.guardar(carga2);
		pedido.setCargas(cargas);
		hbtDAO.guardar(pedido);

		Carga carga3 = new Carga();
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
		carga3.setVolumen(carga3.getAlto() * carga3.getAncho() * carga3.getProfundidad());
		hbtDAO.guardar(carga3);
		Carga carga4 = new Carga();
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
		carga4.setVolumen(carga4.getAlto() * carga4.getAncho() * carga4.getProfundidad());
		hbtDAO.guardar(carga4);

		PlanDeMantenimiento pm = new PlanDeMantenimiento();
		pm.setDiasDemora(2);
		pm.setDiasProxControl(4);
		pm.setKmProxControl(1);
		hbtDAO.guardar(pm);

		PlanDeMantenimiento pm1 = new PlanDeMantenimiento();
		pm1.setDiasDemora(1);
		pm1.setDiasProxControl(28);
		pm1.setKmProxControl(20000);
		hbtDAO.guardar(pm1);

		PlanDeMantenimiento pm2 = new PlanDeMantenimiento();
		pm2.setDiasDemora(2);
		pm2.setDiasProxControl(30);
		pm2.setKmProxControl(100000);
		hbtDAO.guardar(pm2);

		Vehiculo v = new Vehiculo();
		v.setAlto(2);
		v.setAncho(2);
		v.setProfundidad(3);
		v.setVolumen(v.getAlto() * v.getAncho() * v.getProfundidad());
		v.setEnGarantia(true);
		v.setEstado("Libre");
		v.setFechaUltimoControl(new Date(2016, 05, 23));
		v.setKilometraje(10200);
		v.setPeso(3500);
		v.setTara(1500);
		v.setTipo("Camioneta");
		v.setTrabajoEspecifico(false);
		v.setPlanDeMantenimiento(pm);
		hbtDAO.guardar(v);

		Vehiculo v2 = new Vehiculo();
		v2.setAlto(4);
		v2.setAncho(2);
		v2.setProfundidad(8);
		v2.setVolumen(v.getAlto() * v.getAncho() * v.getProfundidad());
		v2.setEnGarantia(true);
		v2.setEstado("Libre");
		v2.setFechaUltimoControl(new Date(2016, 11, 15));
		v2.setKilometraje(90000);
		v2.setPeso(10000);
		v2.setTara(3000);
		v2.setTipo("Camioneta");
		v2.setTrabajoEspecifico(true);
		v2.setPlanDeMantenimiento(pm);
		hbtDAO.guardar(v2);
		
		PrecioVehiculo pv=new PrecioVehiculo();
		pv.setPrecio(2000);
		pv.setTipoVehiculo("Semirremolque Con Barandas");
		hbtDAO.guardar(pv);
		PrecioVehiculo pv2=new PrecioVehiculo();
		pv2.setPrecio(5000);
		pv2.setTipoVehiculo("Avioneta");
		hbtDAO.guardar(pv2);

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
		trayectosA.add(trayectoB);
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

		PlanDeMantenimiento planDeMantenimiento = new PlanDeMantenimiento(4, 10, 2, 10000);
		hbtDAO.guardar(planDeMantenimiento);

		Vehiculo vehiculoA = new Vehiculo(3, "Propio", 5, 3500, 2, 2, 3, 1500, 10200, "Libre", false, true, null,
				planDeMantenimiento);
		hbtDAO.guardar(vehiculoA);
		Vehiculo vehiculoB = new Vehiculo(4, "Propio", 5, 3500, 2, 2, 3, 1500, 10200, "Libre", false, false, null,
				planDeMantenimiento);
		hbtDAO.guardar(vehiculoB);

		System.out.println("-----Fin Cargo de Datos Iniciales Para Envios-----");
	}
	
	// Cliente Empresa

	@Override
	public void altaClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		Empresa e = new Empresa();
		e.setCUIT(empresaDto.getCUIT());
		e.setDetallePoliticas(empresaDto.getDetallePoliticas());
		e.setNombre(empresaDto.getNombre());
		e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		e.setTipo(empresaDto.getTipo());
		hbtDAO.guardar(e);
	}
	
	@Override
	public void updateClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		Empresa e = new Empresa();
		e.setIdCliente(empresaDto.getIdCliente());
		e.setCUIT(empresaDto.getCUIT());
		e.setDetallePoliticas(empresaDto.getDetallePoliticas());
		e.setNombre(empresaDto.getNombre());
		e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		e.setTipo(empresaDto.getTipo());
		hbtDAO.modificar(e);
	}
	
	public void deleteClienteEmpresa(int idCliente) throws RemoteException {
		Empresa e = new Empresa();
		e.setIdCliente(idCliente);
		hbtDAO.borrar(e);
	}
	
	// Cliente Particular

	@Override
	public void altaClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular c = new Particular();
		c.setApellido(particularDto.getApellido());
		c.setDNI(particularDto.getDNI());
		c.setNombre(particularDto.getNombre());
		hbtDAO.guardar(c);
	}

	public void updateClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular p = new Particular();
		p.setIdCliente(particularDto.getIdCliente());
		p.setApellido(particularDto.getApellido());
		p.setDNI(particularDto.getDNI());
		p.setNombre(particularDto.getNombre());
		hbtDAO.modificar(p);
	}

	public void deleteClienteParticular(int idCliente) throws RemoteException {
		Particular p = new Particular();
		p.setIdCliente(idCliente);
		hbtDAO.borrar(p);
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
	
	// Pedido

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
		ArrayList<Carga> cargas = new ArrayList<Carga>();
		for (CargaDTO c : pe.getCargas()) {
			Carga carg = EntityManager.CargaToEntity(c);
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
	
	// Vehiculo

	@Override
	public void crearVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.guardar(EntityManager.VehiculoToEntity(v));
	}

	@Override
	public void modificarVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.modificar(EntityManager.VehiculoToEntity(v));
	}

	@Override
	public void eliminarVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.borrar(EntityManager.VehiculoToEntity(v));
	}
	
	// Cargas
	
	@Override
	public List<CargaDTO> listarCargas() throws RemoteException {
		return hbtDAO.listarCargas();
	}
	
	@Override
	public void createCarga(CargaDTO cd) throws RemoteException {
		Carga c = new Carga();
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
		Carga c = new Carga();
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
		Carga c = new Carga();
		c.setIdCarga(idCarga);
		hbtDAO.borrar(c);
	}
	
	@Override
	public CargaDTO buscarCargaPorId(int idCarga) throws RemoteException {
		return hbtDAO.buscarCargaPorId(idCarga);
	}

	@Override
	public List<CargaDTO> listarCargasSinDespachar() throws RemoteException {
		return hbtDAO.listarCargasSinDespachar();
	}
	
	// Direcciones

	@Override
	public List<DireccionDTO> listarDirecciones() throws RemoteException {
		return hbtDAO.obtenerDirecciones();
	}

	@Override
	public void crearDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.guardar(EntityManager.DireccionToEntity(d));
	}

	@Override
	public void modificarDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.modificar(EntityManager.DireccionToEntity(d));
	}

	@Override
	public void eliminarDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.borrar(EntityManager.DireccionToEntity(d));
	}
	
	// Envios

	@Override
	public List<EnvioDTO> listarEnvios() throws RemoteException {
		return hbtDAO.listarEnvios();
	}
	
	// Viajes

	@Override
	public List<ViajeDTO> listarViajes() throws RemoteException {
		return hbtDAO.obtenerViajes();
	}

	// Planes de Mantenimiento
	@Override
	public List<PlanDeMantenimientoDTO> listarPlanesDeMantenimiento() throws RemoteException {
		return hbtDAO.listarPlanesDeMantenimiento();
	}

	@Override
	public void deletePlanDeMantenimiento(int idPlan) throws RemoteException {
		PlanDeMantenimiento plan = new PlanDeMantenimiento();
		plan.setIdPlanDeMantenimiento(idPlan);
		hbtDAO.borrar(plan);
	}

	@Override
	public void updatePlanDeMantenimiento(PlanDeMantenimientoDTO plan) throws RemoteException {
		hbtDAO.modificar(EntityManager.PlanDeMantenimientoToEntity(plan));
	}

	@Override
	public List<VehiculoAMantenerDTO> getVehiculosAMantener() throws RemoteException {
		return hbtDAO.getVehiculosAMantener();
	}

	@Override
	public List<PrecioVehiculoDTO> listarVTerceros() throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.listarVTerceros();
	}

	@Override
	public void crearVTerceros(PrecioVehiculoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.guardar(EntityManager.PrecioVehiculoToEntity(v));
	}

	@Override
	public void modificarVTerceros(PrecioVehiculoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.modificar(EntityManager.PrecioVehiculoToEntity(v));
	}

	@Override
	public void eliminarVTerceros(PrecioVehiculoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.borrar(EntityManager.PrecioVehiculoToEntity(v));
	}

	@Override
	public void crearEnvioDirecto(int idPedido, int idPrecioVehiculo) throws RemoteException {
		// TODO Auto-generated method stub
		entities.Viaje v=new entities.Viaje();
		PedidoDTO p=hbtDAO.buscarPedidoPorId(idPedido);
		PrecioVehiculoDTO pv=hbtDAO.buscarPrecioVehiculoDTO(idPrecioVehiculo);
		for (SucursalDTO suc: hbtDAO.obtenerSucursales()){
			if(suc.getNombre().equals(p.getSucursalOrigen())){
				v.setSucursalOrigen(EntityManager.SucursalToEntity(suc));
			}
			if(suc.getNombre().equals(p.getSucursalDestino())){
				v.setSucursalDestino(EntityManager.SucursalToEntity(suc));				
			}
		}
		entities.Envio e=new entities.Envio();
		e.setCumpleCondicionesCarga(true);
		e.setEstado("Pendiente");
		e.setFechaSalida(p.getFechaCarga());
		e.setFechaLlegada(p.getFechaMaxima());
		e.setPedido(EntityManager.PedidoToEntity(p));
		e.setPrioridad(1);
		e.setSucursalOrigen(p.getSucursalOrigen());		
		e.setSucursalDestino(p.getSucursalDestino());
		hbtDAO.guardar(e);
		
		EnvioDTO env=hbtDAO.getInstancia().buscarEnvioPorId(e.getIdEnvio());
		ArrayList<Envio> envios=new ArrayList<Envio>();
		envios.add(EntityManager.EnvioToEntity(env));
		v.setEnvios(envios);
		v.setFechaLlegada(p.getFechaMaxima());
		v.setFinalizado(false);
		
		Vehiculo vehiculo=new Vehiculo();
		vehiculo.setAlto(2);
		vehiculo.setAncho(10);
		vehiculo.setProfundidad(2);
		vehiculo.setVolumen(vehiculo.getAlto() * vehiculo.getAncho() * vehiculo.getProfundidad());
		vehiculo.setEnGarantia(true);
		vehiculo.setEstado("En Uso");
		vehiculo.setFechaUltimoControl(new Date());
		vehiculo.setKilometraje(10200);
		vehiculo.setPeso(1500);
		vehiculo.setTara(600);
		vehiculo.setTipo(pv.getTipoVehiculo());
		vehiculo.setTrabajoEspecifico(true);
		PlanDeMantenimiento pm=new PlanDeMantenimiento();
		pm.setDiasDemora(0);
		pm.setDiasProxControl(0);
		pm.setKmProxControl(0);
		hbtDAO.guardar(pm);
		vehiculo.setPlanDeMantenimiento(pm);
		
		hbtDAO.guardar(vehiculo);
		VehiculoDTO ve=new VehiculoDTO();
		ve=hbtDAO.buscarVehiculoPorId(vehiculo.getIdVehiculo());
		v.setVehiculo(EntityManager.VehiculoToEntity(ve));
		hbtDAO.guardar(v);
	}

	@Override
	public PedidoDTO buscarPedidoPorId(int idPedido) throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.buscarPedidoPorId(idPedido);
	}

	@Override
	public PrecioVehiculoDTO buscarPrecioVehiculoDTO(int idPrecioVehiculoDTO) throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.buscarPrecioVehiculoDTO(idPrecioVehiculoDTO);
	}

	@Override
	public List<HabilitadoDTO> listarHabilitados() throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.listarHabilitados();
	}

	@Override
	public List<ProductoDTO> listarProductos() throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.listarProducto();
	}

	@Override
	public void crearHabilitacion(HabilitadoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.guardar(EntityManager.HabilitadoToEntity(v));
	}

	@Override
	public void modificarHabilitacion(HabilitadoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.modificar(EntityManager.HabilitadoToEntity(v));
	}

	@Override
	public void eliminarHabilitacion(HabilitadoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.borrar(EntityManager.HabilitadoToEntity(v));
	}

	@Override
	public void crearProducto(ProductoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.guardar(EntityManager.ProductoToEntity(v));
	}

	@Override
	public void modificarProducto(ProductoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.modificar(EntityManager.ProductoToEntity(v));
	}

	@Override
	public void eliminarProduct(ProductoDTO v) throws RemoteException {
		// TODO Auto-generated method stub
		hbtDAO.borrar(EntityManager.ProductoToEntity(v));
	}

}
