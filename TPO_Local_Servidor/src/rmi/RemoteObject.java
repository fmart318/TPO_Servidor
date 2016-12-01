package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Strategy.PoliticaEspecificidad;
import Strategy.PoliticaGarantia;
import Strategy.PoliticaGeneral;
import Strategy.PoliticaMantenimiento;
import dao.HibernateDAO;
import dto.CargaDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SucursalDTO;
import dto.TrayectoDTO;
import dto.VehiculoAMantenerDTO;
import dto.VehiculoDTO;
import dto.VehiculoTerceroDTO;
import entities.Carga;
import entities.Direccion;
import entities.Empresa;
import entities.Factura;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.Remito;
import entities.Ruta;
import entities.Sucursal;
import entities.Trayecto;
import entities.Vehiculo;

public class RemoteObject extends UnicastRemoteObject implements RemoteInterface {

	private static final long serialVersionUID = 1L;
	private static HibernateDAO hbtDAO;
	private PoliticaMantenimiento politicaMantenimiento;

	@SuppressWarnings("unused")
	private RemoteObjectHelper remoteObjectHelper;

	public RemoteObject() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
		remoteObjectHelper = new RemoteObjectHelper();
	}

	// Funciones del Negocio

	public boolean controlarVehiculo(VehiculoDTO vehiculoDTO) {

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
			} else if (fecha.before(new Date())) {
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

	public void controlarEstadoDeEnvios() throws RemoteException {

		List<PedidoDTO> pedidos = obtenerPedidos();
		pedidos = RemoteObjectHelper.ordenarPedidosPorPrioridad(pedidos);
		// controlarPedidosUrgentes(pedidos);

		ArrayList<PedidoDTO> pedidosPendientes = new ArrayList<PedidoDTO>();
		ArrayList<PedidoDTO> pedidosEsperandoSerBuscadas = new ArrayList<PedidoDTO>();

		for (PedidoDTO pedido : pedidos) {

			EnvioDTO envioDto = hbtDAO.obtenerEnvioActualDePedido(pedido.getIdPedido());

			// Si el pedido ya llego a su destino
			if (pedido.getSucursalActualId() == pedido.getSucursalDestinoId()) {
				System.out.println("-----Pedido LLego a su destino final-----");
				pedidosEsperandoSerBuscadas.add(pedido);
			}

			// Si no existe envio para el pedido se crea uno nuevo
			else if (envioDto == null) {
				System.out.println("-----Creando Nuevo Envio-----");
				pedidosPendientes.add(pedido);
				asignarPedidosPendientesATransporteLibre(RemoteObjectHelper
						.obtenerPedidosConMismoSucursalActual(pedidosPendientes, pedido.getSucursalActualId()));
			}

			// Si el envio esta despachado
			else if (envioDto.getEstado().equals("despachado")) {
				System.out.println("-----Controlando Llegada de Envio-----");
				controlarLLegadaDeEnvio(envioDto);
			}

			// Si el envio esta parado
			else if (envioDto.getEstado().equals("parado")) {
				if (envioDto.getSucursalDestino().getIdSucursal() == pedido.getSucursalDestinoId()) {
					System.out.println("-----Pedido llego a Destino Final-----");

					envioDto.setEstado("listo");
					hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));

					for (PedidoDTO pedidosDeEnvios : envioDto.getPedidos()) {
						pedidosDeEnvios.setEstado("finalizado");
						hbtDAO.modificar(EntityManager.PedidoToEntity(pedidosDeEnvios));
					}

				} else {
					System.out.println("-----Pedido llego a Destino Intermediario-----");
					System.out.println("-----Verificando si se puede enviar el pedido-----");
					pedidosPendientes.add(pedido);
					asignarPedidosPendientesATransporteLibre(RemoteObjectHelper
							.obtenerPedidosConMismoSucursalActual(pedidosPendientes, pedido.getSucursalActualId()));
				}
			}
		}
	}

	private void controlarLLegadaDeEnvio(EnvioDTO envioDto) throws RemoteException {

		Date fechaActual = new Date();

		// Vamos a asumir que el envio siempre llega a tiempo a su destino.
		if (true) {
			// if (fechaActual.after(envioDto.getFechaLlegada())) {

			SucursalDTO sucursalDestino = envioDto.getSucursalDestino();

			VehiculoDTO vehiculo = hbtDAO.obtenerVehiculo(envioDto.getVehiculoId());
			vehiculo.setSucursalIdActual(sucursalDestino.getIdSucursal());
			vehiculo.setEstado("Libre");
			hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculo));

			envioDto.setEstado("parado");
			envioDto.setFechaLlegada(fechaActual);
			hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));

			for (PedidoDTO pedido : envioDto.getPedidos()) {
				pedido.setSucursalActualId(envioDto.getSucursalDestino().getIdSucursal());
				hbtDAO.modificar(EntityManager.PedidoToEntity(pedido));
			}

			List<PedidoDTO> pedidos = sucursalDestino.getPedidos();
			pedidos.addAll(envioDto.getPedidos());
			sucursalDestino.setPedidos(pedidos);
			hbtDAO.modificar(EntityManager.SucursalToEntity(sucursalDestino));

			System.out.println("-----Envio llego a su destino-----");
		}
	}

	private void generarEnvio(VehiculoDTO vehiculoDto, List<PedidoDTO> pedidos) throws RemoteException {

		SucursalDTO sucursalActual = RemoteObjectHelper.obtenerSucursal(pedidos.get(0).getSucursalActualId());
		SucursalDTO sucursalDestino = RemoteObjectHelper.obtenerSucursal(pedidos.get(0).getSucursalDestinoId());
		RutaDTO rutaVehiculo = RemoteObjectHelper.obtenerMejorRuta(sucursalActual, sucursalDestino);

		if (rutaVehiculo != null) {
			vehiculoDto.setEstado("En viaje");
			vehiculoDto.setSucursalIdActual(-1);
			hbtDAO.modificar(EntityManager.VehiculoToEntity(vehiculoDto));

			float tiempoRuta = 0;
			for (TrayectoDTO trayecto : rutaVehiculo.getTrayectos()) {
				tiempoRuta = trayecto.getTiempo() + tiempoRuta;
			}

			long minutosFechaLlegadaEnvio = (long) rutaVehiculo.getTrayectos().get(0).getTiempo() * 60000;
			Date fechaLlegadaEnvio = new Date(Calendar.getInstance().getTime().getTime() + minutosFechaLlegadaEnvio);

			List<EnvioDTO> envios = obtenerEnvios();
			EnvioDTO envioDto = new EnvioDTO(envios.size() + 1, Calendar.getInstance().getTime(), fechaLlegadaEnvio,
					true, "despachado", pedidos, 1, sucursalActual, rutaVehiculo.getNextSucursal(sucursalActual),
					vehiculoDto.getIdVehiculo());
			hbtDAO.guardar(EntityManager.EnvioToEntity(envioDto));

			System.out.println("-----Envio Creado-----");
		}
	}

	public void controlarPedidosUrgentes(List<PedidoDTO> pedidos) throws RemoteException {
		for (int i = 0; i < pedidos.size(); i++) {
			PedidoDTO pedido = pedidos.get(i);
			if (pedido != null) {
				SucursalDTO sucursalActual = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalActualId());
				SucursalDTO sucursalDestino = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalDestinoId());
				Date mejorFechaLLegada = RemoteObjectHelper.calcularMejorFechaLlegada(sucursalActual, sucursalDestino);

				if (mejorFechaLLegada != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(mejorFechaLLegada);
					cal.add(Calendar.DATE, -30);
					Date mejorFechaLLegadaMenosUnDia = cal.getTime();

					if (!pedido.getFechaMaxima().before(mejorFechaLLegada)) {
						contratarTercero(pedido);
					} else if (!pedido.getFechaMaxima().before(mejorFechaLLegadaMenosUnDia)) {
						enviarUrgente(pedido);
					}
				} else {
					contratarTercero(pedido);
				}
			}
		}
	}

	private void enviarUrgente(PedidoDTO pedido) throws RemoteException {

		float cargaTotalPedido = 0;
		for (CargaDTO carga : pedido.getCargas()) {
			cargaTotalPedido = carga.getVolumen() + cargaTotalPedido;
		}

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper
				.obtenerVehiculosDisponiblesEnSucursal(pedido.getSucursalActualId());

		if (vehiculosDisponibles.size() > 0) {

			boolean vehiculoParaPedidoUrgenteDisponible = false;
			for (VehiculoDTO vehiculoDto : vehiculosDisponibles) {
				if (cargaTotalPedido < vehiculoDto.getVolumen()) {

					vehiculoParaPedidoUrgenteDisponible = true;

					List<PedidoDTO> pedidoList = new ArrayList<PedidoDTO>();
					pedidoList.add(pedido);
					generarEnvio(vehiculoDto, pedidoList);

					break;
				}
			}
			if (!vehiculoParaPedidoUrgenteDisponible) {
				contratarTercero(pedido);
			}
		} else {
			contratarTercero(pedido);
		}
	}

	private void contratarTercero(PedidoDTO pedido) throws RemoteException {

		System.out.println("-----Contratando un Tercero-----");

		float cargaTotalPedido = 0;
		for (CargaDTO carga : pedido.getCargas()) {
			cargaTotalPedido = carga.getVolumen() + cargaTotalPedido;
		}

		List<VehiculoTerceroDTO> vehiculosTercerosDisponibles = RemoteObjectHelper
				.obtenerVehiculosTercerosDisponibles();

		if (vehiculosTercerosDisponibles.size() > 0) {
			VehiculoTerceroDTO vehiculoTerceroDTO = vehiculosTercerosDisponibles.get(0);
			vehiculoTerceroDTO.setEstado("En Uso");
			vehiculoTerceroDTO.setFechaLlegada(pedido.getFechaMaxima());

			List<PedidoDTO> pedidoList = new ArrayList<PedidoDTO>();
			pedidoList.add(pedido);
			vehiculoTerceroDTO.setPedidos(pedidoList);

			hbtDAO.guardar(EntityManager.VehiculoTerceroToEntity(vehiculoTerceroDTO));
		} else {
			System.out.println("-----No hay Tercero Disponible-----");
		}
	}

	public void asignarPedidosPendientesATransporteLibre(ArrayList<PedidoDTO> pedidosPendientes)
			throws RemoteException {

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper
				.obtenerVehiculosDisponiblesEnSucursal(pedidosPendientes.get(0).getSucursalActualId());

		if (vehiculosDisponibles.size() > 0) {

			for (VehiculoDTO vehiculoDisponible : vehiculosDisponibles) {
				ArrayList<PedidoDTO> pedidosPendientesConvenientes = RemoteObjectHelper
						.obtenerCombinacionPedidosPendientesMasConveniente(pedidosPendientes,
								(vehiculoDisponible.getVolumen() * 70) / 100, vehiculoDisponible.getVolumen());

				if (pedidosPendientesConvenientes.size() > 0) {
					generarEnvio(vehiculoDisponible, pedidosPendientesConvenientes);
					break;
				}
			}
		}
	}

	@Override
	public void crearEnvioDirecto(int idPedido) throws RemoteException {
		PedidoDTO pedidoDto = hbtDAO.buscarPedidoPorId(idPedido);
		enviarUrgente(pedidoDto);
	}

	// Datos Iniciales

	@Override
	public void cargarDatosIniciales() throws RemoteException {

		/*
		 * Producto prod = new Producto();
		 * prod.setNombre("Electrodom�sticos"); prod.setTipo("Electr�nico");
		 * hbtDAO.guardar(prod); Producto prod2 = new Producto();
		 * prod2.setNombre("Cereal"); prod2.setTipo("A Granel");
		 * hbtDAO.guardar(prod2);
		 * 
		 * ArrayList<Producto> prods = new ArrayList<Producto>();
		 * prods.add(prod2);
		 * 
		 * entities.Empresa e = new Empresa(); e.setNombre(
		 * "Distribuci�n BS AS SA"); e.setCUIT(2342342);
		 * e.setDetallePoliticas("Detalle Pol�tca"); e.setTipo("SA");
		 * e.setSaldoCuentaCorriente(15000); hbtDAO.guardar(e);
		 * 
		 * entities.Empresa e2 = new Empresa(); e2.setNombre("Fabella SRL");
		 * e2.setCUIT(234234); e2.setDetallePoliticas("Detalle Pol�tca");
		 * e2.setTipo("SRL"); e2.setSaldoCuentaCorriente(15000);
		 * hbtDAO.guardar(e2);
		 * 
		 * Habilitado h = new Habilitado();
		 * h.setDniHabilitado(String.valueOf("9418723")); h.setNombre("REBA");
		 * hbtDAO.guardar(h); ArrayList<Habilitado> hs = new
		 * ArrayList<Habilitado>(); hs.add(h); entities.Particular p = new
		 * Particular(); p.setNombre("Elio"); p.setApellido("Mollo");
		 * p.setDNI(9418723); p.setHabilitados(hs); hbtDAO.guardar(p);
		 * entities.Particular p2 = new Particular(); p2.setNombre("Felipe");
		 * p2.setApellido("Mart"); p2.setDNI(2303040); hbtDAO.guardar(p2);
		 * 
		 * entities.Direccion dO = new entities.Direccion(); dO.setCalle(
		 * "Av. Rigolleau"); dO.setCP("1884"); dO.setDepartamento("F");
		 * dO.setNumero(1405); dO.setPiso(2); hbtDAO.guardar(dO);
		 * 
		 * Sucursal so = new Sucursal(); so.setNombre("Sucursal Berazategui");
		 * so.setUbicacion(dO); so.setPedidos(null); hbtDAO.guardar(so);
		 * 
		 * entities.Direccion dD = new entities.Direccion(); dD.setCalle(
		 * "Av. Mitre"); dD.setCP("1883"); dD.setDepartamento("A");
		 * dD.setNumero(9230); dD.setPiso(2); hbtDAO.guardar(dD);
		 * 
		 * Sucursal s = new Sucursal(); s.setNombre("Sucursal Quilmes");
		 * s.setUbicacion(dD); so.setPedidos(null); hbtDAO.guardar(s);
		 * 
		 * entities.Pedido pedido = new entities.Pedido(); pedido.setCliente(p);
		 * pedido.setDireccionCarga(dO); pedido.setDireccionDestino(dD); Date
		 * hoy = new Date(); pedido.setFechaCarga(hoy); Date fmax = new
		 * Date(hoy.getTime() + (1000 * 60 * 60 * 24));
		 * pedido.setFechaMaxima(fmax); pedido.setHoraInicio(12);
		 * pedido.setHoraFin(13); pedido.setPrecio(1500);
		 * pedido.setSucursalOrigenId(so.getIdSucursal());
		 * pedido.setSucursalDestinoId(s.getIdSucursal());
		 * pedido.setSolicitaAvionetaParticular(false);
		 * pedido.setSolicitaTransporteDirecto(false); ArrayList<entities.Carga>
		 * cargas = new ArrayList<entities.Carga>(); entities.Carga carga = new
		 * entities.Carga(); carga.setAlto(1); carga.setAncho(2);
		 * carga.setApilable(4); carga.setCondiciones("Apilable");
		 * carga.setDespachado(true); carga.setFragilidad("Normal");
		 * carga.setMercaderia("Electr�nico"); carga.setPeso(20);
		 * carga.setProfundidad(1); carga.setRefrigerable(false);
		 * carga.setTratamiento("Normal"); carga.setVolumen(carga.getAlto() *
		 * carga.getAncho() * carga.getProfundidad()); hbtDAO.guardar(carga);
		 * cargas.add(carga); entities.Carga carga2 = new entities.Carga();
		 * carga2.setAlto(1); carga2.setAncho(2); carga2.setApilable(2);
		 * carga2.setCondiciones("No apilable"); carga2.setDespachado(true);
		 * carga2.setFragilidad("Normal");
		 * carga2.setMercaderia("Electr�nico"); carga2.setPeso(30);
		 * carga2.setProfundidad(1); carga2.setRefrigerable(false);
		 * carga2.setTratamiento("Normal"); carga2.setVolumen(carga2.getAlto() *
		 * carga2.getAncho() * carga2.getProfundidad()); cargas.add(carga2);
		 * hbtDAO.guardar(carga2); pedido.setCargas(cargas);
		 * hbtDAO.guardar(pedido);
		 * 
		 * Carga carga3 = new Carga(); carga3.setAlto(2); carga3.setAncho(6);
		 * carga3.setApilable(0); carga3.setCondiciones("A granel");
		 * carga3.setDespachado(false); carga3.setFragilidad("Normal");
		 * carga3.setMercaderia("A Granel"); carga3.setPeso(3000);
		 * carga3.setProfundidad(6); carga3.setRefrigerable(false);
		 * carga3.setTratamiento("Normal"); carga3.setVolumen(carga3.getAlto() *
		 * carga3.getAncho() * carga3.getProfundidad()); hbtDAO.guardar(carga3);
		 * Carga carga4 = new Carga(); carga4.setAlto(2); carga4.setAncho(4);
		 * carga4.setApilable(0); carga4.setCondiciones("A granel");
		 * carga4.setDespachado(false); carga4.setFragilidad("Normal");
		 * carga4.setMercaderia("A Granel"); carga4.setPeso(100);
		 * carga4.setProfundidad(6); carga4.setRefrigerable(false);
		 * carga4.setTratamiento("Normal"); carga4.setVolumen(carga4.getAlto() *
		 * carga4.getAncho() * carga4.getProfundidad()); hbtDAO.guardar(carga4);
		 * 
		 * PlanDeMantenimiento pm = new PlanDeMantenimiento();
		 * pm.setDiasDemora(2); pm.setDiasProxControl(4);
		 * pm.setKmProxControl(1); hbtDAO.guardar(pm);
		 * 
		 * PlanDeMantenimiento pm1 = new PlanDeMantenimiento();
		 * pm1.setDiasDemora(1); pm1.setDiasProxControl(28);
		 * pm1.setKmProxControl(20000); hbtDAO.guardar(pm1);
		 * 
		 * PlanDeMantenimiento pm2 = new PlanDeMantenimiento();
		 * pm2.setDiasDemora(2); pm2.setDiasProxControl(30);
		 * pm2.setKmProxControl(100000); hbtDAO.guardar(pm2);
		 * 
		 * Vehiculo v = new Vehiculo(); v.setAlto(2); v.setAncho(2);
		 * v.setProfundidad(3); v.setVolumen(v.getAlto() * v.getAncho() *
		 * v.getProfundidad()); v.setEnGarantia(true); v.setEstado("Libre");
		 * v.setFechaUltimoControl(new Date(2016, 05, 23));
		 * v.setKilometraje(10200); v.setPeso(3500); v.setTara(1500);
		 * v.setTipo("Camioneta"); v.setTrabajoEspecifico(false);
		 * v.setPlanDeMantenimiento(pm); hbtDAO.guardar(v);
		 * 
		 * Vehiculo v2 = new Vehiculo(); v2.setAlto(4); v2.setAncho(2);
		 * v2.setProfundidad(8); v2.setVolumen(v.getAlto() * v.getAncho() *
		 * v.getProfundidad()); v2.setEnGarantia(true); v2.setEstado("Libre");
		 * v2.setFechaUltimoControl(new Date(2016, 11, 15));
		 * v2.setKilometraje(90000); v2.setPeso(10000); v2.setTara(3000);
		 * v2.setTipo("Camioneta"); v2.setTrabajoEspecifico(true);
		 * v2.setPlanDeMantenimiento(pm); hbtDAO.guardar(v2);
		 * 
		 * PrecioVehiculo pv = new PrecioVehiculo(); pv.setPrecio(2000);
		 * pv.setTipoVehiculo("Semirremolque Con Barandas"); hbtDAO.guardar(pv);
		 * PrecioVehiculo pv2 = new PrecioVehiculo(); pv2.setPrecio(5000);
		 * pv2.setTipoVehiculo("Avioneta"); hbtDAO.guardar(pv2);
		 * 
		 * Factura factura = new Factura(); factura.setIdFactura(1);
		 * factura.setPedido(pedido); factura.setPrecio(1000);
		 * hbtDAO.guardar(factura);
		 * 
		 * Remito remito = new Remito(); remito.setIdRemito(1);
		 * remito.setPedido(pedido); hbtDAO.guardar(remito);
		 */
		datosInicialesParaEnvios();
	}

	private void datosInicialesParaEnvios() {

		System.out.println("-----Cargando Datos Iniciales Para Envios-----");

		List<Sucursal> sucursalesA = new ArrayList<Sucursal>();
		List<Sucursal> sucursalesB = new ArrayList<Sucursal>();

		Direccion direccionA = new Direccion(1, "Calle 16", 5402, 1, "F", "1500");
		hbtDAO.guardar(direccionA);
		Sucursal sucursal1 = new Sucursal(1, "Berazategui", direccionA, null);
		hbtDAO.guardar(sucursal1);
		sucursalesA.add(sucursal1);

		Direccion direccionB = new Direccion(2, "Calle 18", 1000, 1, "H", "1800");
		hbtDAO.guardar(direccionB);
		Sucursal sucursal2 = new Sucursal(2, "San Isidro", direccionB, null);
		hbtDAO.guardar(sucursal2);
		sucursalesA.add(sucursal2);
		sucursalesB.add(sucursal2);

		Direccion direccionC = new Direccion(3, "Calle 20", 1000, 1, "H", "2000");
		hbtDAO.guardar(direccionC);
		Sucursal sucursal3 = new Sucursal(3, "Quilmes", direccionC, null);
		hbtDAO.guardar(sucursal3);
		sucursalesA.add(sucursal3);
		sucursalesB.add(sucursal3);

		Particular cliente = new Particular(4, "Felipe", 45017024, "Martinez");
		hbtDAO.guardar(cliente);

		List<Carga> cargas = new ArrayList<Carga>();
		Carga carga = new Carga(3, 20, 2, 1, 1, 4, "Normal", "Normal", 4, false, "Apilable", true, "Electronico");
		cargas.add(carga);
		hbtDAO.guardar(carga);

		Date fecahActual = new Date();
		Pedido pedido = new Pedido(2, direccionA, direccionC, fecahActual, 12, 13,
				new Date(fecahActual.getTime() + (1000 * 60 * 60 * 24)), cargas, 1500, sucursal1.getIdSucursal(),
				sucursal3.getIdSucursal(), sucursal1.getIdSucursal(), false, false, cliente, "pendiente");
		hbtDAO.guardar(pedido);

		List<Pedido> pedidos = new ArrayList<Pedido>();
		pedidos.add(pedido);
		sucursal1.setPedidos(pedidos);
		hbtDAO.modificar(sucursal1);

		List<Trayecto> trayectosA = new ArrayList<Trayecto>();
		List<Trayecto> trayectosB = new ArrayList<Trayecto>();
		List<Trayecto> trayectosC = new ArrayList<Trayecto>();
		List<Trayecto> trayectosD = new ArrayList<Trayecto>();

		Trayecto trayectoA = new Trayecto(1, sucursal1, sucursal2, 400, 400, 200);
		Trayecto trayectoB = new Trayecto(2, sucursal2, sucursal3, 400, 400, 200);
		trayectosA.add(trayectoA);
		trayectosA.add(trayectoB);
		trayectosC.add(trayectoB);
		hbtDAO.guardar(trayectoA);
		hbtDAO.guardar(trayectoB);

		Trayecto trayectoC = new Trayecto(3, sucursal1, sucursal2, 200, 200, 100);
		Trayecto trayectoD = new Trayecto(4, sucursal2, sucursal3, 200, 200, 100);
		trayectosB.add(trayectoC);
		trayectosB.add(trayectoD);
		trayectosD.add(trayectoD);
		hbtDAO.guardar(trayectoC);
		hbtDAO.guardar(trayectoD);

		Ruta rutaA = new Ruta(1, trayectosA, 300, sucursalesA, sucursal1, sucursal3);
		hbtDAO.guardar(rutaA);
		Ruta rutaB = new Ruta(2, trayectosB, 200, sucursalesA, sucursal1, sucursal3);
		hbtDAO.guardar(rutaB);

		Ruta rutaC = new Ruta(3, trayectosC, 200, sucursalesB, sucursal2, sucursal3);
		hbtDAO.guardar(rutaC);
		Ruta rutaD = new Ruta(4, trayectosD, 200, sucursalesB, sucursal2, sucursal3);
		hbtDAO.guardar(rutaD);

		PlanDeMantenimiento planDeMantenimiento = new PlanDeMantenimiento(4, 10, 2, 10000);
		hbtDAO.guardar(planDeMantenimiento);

		Vehiculo vehiculoA = new Vehiculo(3, "Propio", 5, 3500, 2, 2, 3, 1500, 10200, "Libre", false, true, null,
				planDeMantenimiento, sucursal1.getIdSucursal());
		hbtDAO.guardar(vehiculoA);
		Vehiculo vehiculoB = new Vehiculo(4, "Propio", 5, 3500, 2, 2, 3, 1500, 10200, "Libre", false, false, null,
				planDeMantenimiento, sucursal1.getIdSucursal());
		hbtDAO.guardar(vehiculoB);
		/*
		 * VehiculoTercero vehiculoTerceroA = new VehiculoTercero(1,
		 * "Semirremolque Con Barandas", 2000, "Libre", null, null);
		 * hbtDAO.guardar(vehiculoTerceroA);
		 * 
		 * VehiculoTercero vehiculoTerceroB = new VehiculoTercero(2, "Avioneta",
		 * 5000, "Libre", null, null); hbtDAO.guardar(vehiculoTerceroB);
		 */
		System.out.println("-----Fin Cargo de Datos Iniciales Para Envios-----");
	}

	// Cliente Empresa

	public List<EmpresaDTO> obtenerClientesEmpresa() {
		return hbtDAO.obtenerClientesEmpresa();
	}

	@Override
	public void altaClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		Empresa clienteEmpresa = new Empresa();
		clienteEmpresa.setCUIT(empresaDto.getCUIT());
		clienteEmpresa.setDetallePoliticas(empresaDto.getDetallePoliticas());
		clienteEmpresa.setNombre(empresaDto.getNombre());
		clienteEmpresa.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		clienteEmpresa.setTipo(empresaDto.getTipo());
		hbtDAO.guardar(clienteEmpresa);
	}

	@Override
	public void updateClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
		Empresa clienteEmpresa = new Empresa();
		clienteEmpresa.setIdCliente(empresaDto.getIdCliente());
		clienteEmpresa.setCUIT(empresaDto.getCUIT());
		clienteEmpresa.setDetallePoliticas(empresaDto.getDetallePoliticas());
		clienteEmpresa.setNombre(empresaDto.getNombre());
		clienteEmpresa.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
		clienteEmpresa.setTipo(empresaDto.getTipo());
		hbtDAO.modificar(clienteEmpresa);
	}

	public void deleteClienteEmpresa(int idCliente) throws RemoteException {
		Empresa clienteEmpresa = new Empresa();
		clienteEmpresa.setIdCliente(idCliente);
		hbtDAO.borrar(clienteEmpresa);
	}

	// Cliente Particular

	public List<ParticularDTO> obtenerClientesParticular() {
		return hbtDAO.obtenerClientesParticular();
	}

	@Override
	public void altaClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular clienteParticular = new Particular();
		clienteParticular.setApellido(particularDto.getApellido());
		clienteParticular.setDNI(particularDto.getDNI());
		clienteParticular.setNombre(particularDto.getNombre());
		hbtDAO.guardar(clienteParticular);
	}

	public void updateClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular clienteParticular = new Particular();
		clienteParticular.setIdCliente(particularDto.getIdCliente());
		clienteParticular.setApellido(particularDto.getApellido());
		clienteParticular.setDNI(particularDto.getDNI());
		clienteParticular.setNombre(particularDto.getNombre());
		hbtDAO.modificar(clienteParticular);
	}

	public void deleteClienteParticular(int idCliente) throws RemoteException {
		Particular clienteParticular = new Particular();
		clienteParticular.setIdCliente(idCliente);
		hbtDAO.borrar(clienteParticular);
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

		List<Pedido> pedidos = new ArrayList<Pedido>();
		for (PedidoDTO pedido : sucursalDto.getPedidos()) {
			pedidos.add(EntityManager.PedidoToEntity(pedido));
		}
		sucursal.setPedidos(pedidos);

		hbtDAO.modificar(sucursal);
	}

	// Trayecto

	@Override
	public List<TrayectoDTO> obtenerTrayectos() throws RemoteException {
		return hbtDAO.obtenerTrayectos();
	}

	@Override
	public void altaTrayecto(TrayectoDTO trayectDto) throws RemoteException {

		hbtDAO.guardar(EntityManager.TrayectoToEntity(trayectDto));
	}

	@Override
	public void updateTrayecto(TrayectoDTO trayectDto) throws RemoteException {
		hbtDAO.modificar(EntityManager.TrayectoToEntity(trayectDto));
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

		hbtDAO.modificar(EntityManager.RutaToEntity(rutaDto));
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

		List<EmpresaDTO> empresas = hbtDAO.obtenerClientesEmpresa();
		for (EmpresaDTO e : empresas) {
			if (e.getIdCliente() == pe.getCliente().getIdCliente()) {
				Empresa empresa = new Empresa();
				empresa.setIdCliente(e.getIdCliente());
				p.setCliente(empresa);
			}
		}

		List<ParticularDTO> particulares = hbtDAO.obtenerClientesParticular();
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
		p.setSucursalOrigenId(pe.getSucursalOrigenId());
		p.setSucursalDestinoId(pe.getSucursalDestinoId());
		p.setSolicitaAvionetaParticular(pe.isSolicitaAvionetaParticular());
		p.setSolicitaTransporteDirecto(pe.isSolicitaTransporteDirecto());
		hbtDAO.guardar(p);

		FacturaDTO factura = new FacturaDTO();
		factura.setPedido(p.toDTO());
		factura.setPrecio(p.getPrecio());
		altaFactura(factura);

	}

	public List<PedidoDTO> obtenerPedidos() {
		return hbtDAO.obtenerPedidos();
	}

	// Envio

	public List<EnvioDTO> obtenerEnvios() throws RemoteException {
		return hbtDAO.obtenerEnvios();
	}

	// Vehiculo

	public List<VehiculoDTO> obtenerVehiculos() {
		return hbtDAO.obtenerVehiculos();
	}

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

	// Carga

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

	// Direccion

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

	// Plan de Mantenimiento

	@Override
	public List<PlanDeMantenimientoDTO> listarPlanesDeMantenimiento() throws RemoteException {
		return hbtDAO.listarPlanesDeMantenimiento();
	}

	public void altaPlanMantenimiento(PlanDeMantenimientoDTO planDeMantenimientoDTO) {
		hbtDAO.guardar(EntityManager.PlanDeMantenimientoToEntity(planDeMantenimientoDTO));
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

	// Vehiculo Tercero

	@Override
	public List<VehiculoTerceroDTO> listarVTerceros() throws RemoteException {
		return hbtDAO.listarVTerceros();
	}

	@Override
	public void crearVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.guardar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	@Override
	public void modificarVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.modificar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	@Override
	public void eliminarVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.borrar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	@Override
	public VehiculoTerceroDTO buscarVehiculoTerceroDTO(int idVehiculoTerceroDTO) throws RemoteException {
		return hbtDAO.buscarVehiculoTerceroDTO(idVehiculoTerceroDTO);
	}

	// Facturas

	@Override
	public List<FacturaDTO> listarFacturas() throws RemoteException {
		return hbtDAO.listarFacturas();
	}

	public void altaFactura(FacturaDTO facturaDTO) {
		hbtDAO.guardar(EntityManager.FacturaToEntity(facturaDTO));
	}

	@Override
	public void deleteFactura(int idFactura) throws RemoteException {
		Factura factura = new Factura();
		factura.setIdFactura(idFactura);
		hbtDAO.borrar(factura);
	}

	// Remitos

	@Override
	public List<RemitoDTO> listarRemitos() throws RemoteException {
		return hbtDAO.listarRemitos();
	}

	public void altaRemito(RemitoDTO remitoDTO) {
		hbtDAO.guardar(EntityManager.RemitoToEntity(remitoDTO));
	}

	@Override
	public void deleteRemito(int idRemito) throws RemoteException {
		Remito remito = new Remito();
		remito.setIdRemito(idRemito);
		hbtDAO.borrar(remito);
	}

	@Override
	public DireccionDTO obtenerDireccionPorId(int idDireccion) throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.obtenerDireccionPorId(idDireccion);
	}

	@Override
	public SucursalDTO obtenerSucursalPorId(int idSucursal) throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.obtenerSucursalPorId(idSucursal);
	}

	@Override
	public PlanDeMantenimientoDTO obtenerPlanDeMantenimientoPorId(int idPlanDeMantenimiento) throws RemoteException {
		// TODO Auto-generated method stub
		return hbtDAO.obtenerPlanDeMantenimientoPorId(idPlanDeMantenimiento);
	}

}
