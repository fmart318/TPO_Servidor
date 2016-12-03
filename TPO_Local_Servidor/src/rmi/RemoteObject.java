package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
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
import entities.VehiculoTercero;

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

		ArrayList<PedidoDTO> pedidosPendientes = new ArrayList<PedidoDTO>();
		ArrayList<PedidoDTO> pedidosEsperandoSerBuscadas = new ArrayList<PedidoDTO>();

		for (PedidoDTO pedido : pedidos) {

			System.out.println("-----Controlando Pedido " + pedido.getIdPedido() + "-----");

			VehiculoTerceroDTO vehiculoTerceroDTO = hbtDAO.obtenerVehiculoTerceroConPedido(pedido.getIdPedido());
			EnvioDTO envioDto = hbtDAO.obtenerEnvioActualDePedido(pedido.getIdPedido());

			// Si el pedido ya llego a su destino
			if (pedido.getSucursalActualId() == pedido.getSucursalDestinoId()) {
				System.out.println("-----Pedido LLego a su destino final-----");
				pedidosEsperandoSerBuscadas.add(pedido);
			}

			// Si el pedido fue enviado por un Tercero
			else if (vehiculoTerceroDTO != null) {
				System.out.println("-----Contactando Tercero-----");
				controlarLLegadaDeTercero(vehiculoTerceroDTO, pedido);
			}

			// Si no existe envio para el pedido se crea uno nuevo
			else if (envioDto == null) {
				System.out.println("-----Verificando si se puede generar un envio-----");
				pedidosPendientes.add(pedido);
				if (!controlarPedidoUrgenteNecesario(pedido)) {
					asignarPedidosPendientesATransporteLibre(RemoteObjectHelper
							.obtenerPedidosConMismoSucursalActual(pedidosPendientes, pedido.getSucursalActualId()));
				}
			}

			// Si el envio esta despachado
			else if (envioDto.getEstado().equals("despachado")) {
				System.out.println("-----Controlando Llegada de Envio-----");
				controlarLLegadaDeEnvio(envioDto);
			}

			// Si el envio esta parado
			else if (envioDto.getEstado().equals("parado")) {
				if (!controlarPedidoLLegoADestinoFinal(envioDto, pedido)) {
					System.out.println("-----Verificando si se puede generar un envio-----");
					pedidosPendientes.add(pedido);
					if (!controlarPedidoUrgenteNecesario(pedido)) {
						asignarPedidosPendientesATransporteLibre(RemoteObjectHelper
								.obtenerPedidosConMismoSucursalActual(pedidosPendientes, pedido.getSucursalActualId()));
					}
				}
			}
		}
		
		System.out.println("-----Actualizacion Finalizada-----");
	}

	private boolean controlarPedidoLLegoADestinoFinal(EnvioDTO envioDto, PedidoDTO pedido) throws RemoteException {

		if (envioDto.getSucursalDestino().getIdSucursal() == pedido.getSucursalDestinoId()) {
			System.out.println("-----Pedido llego a Destino Final-----");

			envioDto.setEstado("listo");
			hbtDAO.modificar(EntityManager.EnvioToEntity(envioDto));

			for (PedidoDTO pedidosDeEnvios : envioDto.getPedidos()) {
				pedidosDeEnvios.setEstado("finalizado");
				hbtDAO.modificar(EntityManager.PedidoToEntity(pedidosDeEnvios));
			}
			return true;

		} else {
			System.out.println("-----Pedido llego a Destino Intermediario-----");
			return false;
		}
	}

	private void controlarLLegadaDeTercero(VehiculoTerceroDTO vehiculoTerceroDto, PedidoDTO pedido)
			throws RemoteException {

		Timestamp fechaActual = new Timestamp(System.currentTimeMillis());
		
		// Se asume que el tercero siempre llega en el horario predeterminado
		if (vehiculoTerceroDto.getFechaLlegada().before(fechaActual)) {
			System.out.println("-----Pedido LLego a su destino final-----");
			
			vehiculoTerceroDto.setEstado("Libre");
			vehiculoTerceroDto.setPedidos(new ArrayList<PedidoDTO>());
			vehiculoTerceroDto.setFechaLlegada(null);
			hbtDAO.modificar(EntityManager.VehiculoTerceroToEntity(vehiculoTerceroDto));
			
			SucursalDTO sucursalDestino = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalDestinoId());
			
			pedido.setSucursalActualId(sucursalDestino.getIdSucursal());
			hbtDAO.modificar(EntityManager.PedidoToEntity(pedido));

			List<PedidoDTO> pedidos = sucursalDestino.getPedidos();
			pedidos.add(pedido);
			sucursalDestino.setPedidos(pedidos);
			hbtDAO.modificar(EntityManager.SucursalToEntity(sucursalDestino));

		} else {
			System.out.println("-----Pedido Sigue en camino-----");
		}
	}

	private void controlarLLegadaDeEnvio(EnvioDTO envioDto) throws RemoteException {

		Timestamp fechaActual = new Timestamp(System.currentTimeMillis());

		// Vamos a asumir que el envio siempre llega a tiempo a su destino.
		if (fechaActual.after(envioDto.getFechaLlegada())) {

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
		} else {
			System.out.println("-----Envio Sigue en transito-----");
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
			Timestamp fechaInicialEnvio = new Timestamp(System.currentTimeMillis());
			Timestamp fechaLlegadaEnvio = new Timestamp(System.currentTimeMillis() + minutosFechaLlegadaEnvio);

			List<EnvioDTO> envios = obtenerEnvios();
			EnvioDTO envioDto = new EnvioDTO(envios.size() + 1, fechaInicialEnvio, fechaLlegadaEnvio, true,
					"despachado", pedidos, 1, sucursalActual, rutaVehiculo.getNextSucursal(sucursalActual),
					vehiculoDto.getIdVehiculo());
			hbtDAO.guardar(EntityManager.EnvioToEntity(envioDto));

			System.out.println("-----Envio Creado-----");
		}
	}

	public boolean controlarPedidoUrgenteNecesario(PedidoDTO pedido) throws RemoteException {

		SucursalDTO sucursalActual = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalActualId());
		SucursalDTO sucursalDestino = RemoteObjectHelper.obtenerSucursal(pedido.getSucursalDestinoId());
		Date mejorFechaLLegada = RemoteObjectHelper.calcularMejorFechaLlegada(sucursalActual, sucursalDestino);

		if (mejorFechaLLegada != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mejorFechaLLegada);
			cal.add(Calendar.DATE, -1);
			Date mejorFechaLLegadaMenosUnDia = cal.getTime();

			if (pedido.getFechaMaxima().before(mejorFechaLLegada)) {
				return contratarTercero(pedido);
			} else if (pedido.getFechaMaxima().before(mejorFechaLLegadaMenosUnDia)) {
				return controlarEnvioUrgente(pedido);
			}
		}

		return false;
	}

	private boolean controlarEnvioUrgente(PedidoDTO pedido) throws RemoteException {

		System.out.println("-----Pedido requiere un envio urgente-----");

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

					return true;
				}
			}
			if (!vehiculoParaPedidoUrgenteDisponible) {
				return contratarTercero(pedido);
			}
		} else {
			return contratarTercero(pedido);
		}
		return false;
	}

	private boolean contratarTercero(PedidoDTO pedido) throws RemoteException {

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

			hbtDAO.modificar(EntityManager.VehiculoTerceroToEntity(vehiculoTerceroDTO));

			return true;
		} else {
			System.out.println("-----No hay Tercero Disponible-----");
			return false;
		}
	}

	public void asignarPedidosPendientesATransporteLibre(ArrayList<PedidoDTO> pedidosPendientes)
			throws RemoteException {

		List<VehiculoDTO> vehiculosDisponibles = RemoteObjectHelper
				.obtenerVehiculosDisponiblesEnSucursal(pedidosPendientes.get(0).getSucursalActualId());

		if (vehiculosDisponibles.size() > 0) {

			boolean seGeneroEnvio = false;
			for (VehiculoDTO vehiculoDisponible : vehiculosDisponibles) {
				ArrayList<PedidoDTO> pedidosPendientesConvenientes = RemoteObjectHelper
						.obtenerCombinacionPedidosPendientesMasConveniente(pedidosPendientes,
								(vehiculoDisponible.getVolumen() * 70) / 100, vehiculoDisponible.getVolumen());

				if (pedidosPendientesConvenientes.size() > 0) {
					seGeneroEnvio = true;
					System.out.println("-----Creando nuevo envio-----");
					generarEnvio(vehiculoDisponible, pedidosPendientesConvenientes);
					break;
				}
			}
			if (!seGeneroEnvio) {
				System.out.println("-----Falta mas pedidos para enviar-----");
			}
		} else {
			System.out.println("-----No hay vehiculos disponibles-----");
		}
	}

	@Override
	public void crearEnvioDirecto(int idPedido) throws RemoteException {
		PedidoDTO pedidoDto = hbtDAO.buscarPedidoPorId(idPedido);
		controlarEnvioUrgente(pedidoDto);
	}

	// Datos Iniciales

	@Override
	public void cargarDatosIniciales() throws RemoteException {

		System.out.println("-----Cargando Datos Iniciales Para Envios-----");

		// Sucursales y Cliente

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

		Timestamp fechaActual = new Timestamp(System.currentTimeMillis());
		Timestamp fecahActualMasUnMinuto = new Timestamp(fechaActual.getTime() + (10 * 6000));
		Timestamp fecahActualMasTresMinutos = new Timestamp(fechaActual.getTime() + (10 * 6000 * 3));
		Timestamp fecahActualMas2Dias = new Timestamp(fechaActual.getTime() + (10 * 6000 * 1440 * 2));

		// Pedido A - Volumen = 5 - Sucursal = 1

		List<Carga> cargasA = new ArrayList<Carga>();
		Carga cargaA = new Carga(3, 20, 2, 1, 1, 5, "Normal", "Normal", 4, false, "Apilable", true, "Electronico");
		cargasA.add(cargaA);
		hbtDAO.guardar(cargaA);

		Pedido pedidoA = new Pedido(2, direccionA, direccionC, fechaActual, 12, 13, fecahActualMas2Dias, cargasA, 1500,
				sucursal1.getIdSucursal(), sucursal3.getIdSucursal(), sucursal1.getIdSucursal(), false, false, cliente,
				"pendiente");
		hbtDAO.guardar(pedidoA);

		List<Pedido> pedidosA = new ArrayList<Pedido>();
		pedidosA.add(pedidoA);
		sucursal1.setPedidos(pedidosA);
		hbtDAO.modificar(sucursal1);

		// Pedido B - Volumen = 1 - Sucursal = 2

		List<Carga> cargasB = new ArrayList<Carga>();
		Carga cargaB = new Carga(4, 20, 2, 1, 1, 1, "Normal", "Normal", 4, false, "Apilable", true, "Electronico");
		cargasB.add(cargaB);
		hbtDAO.guardar(cargaB);

		Pedido pedidoB = new Pedido(3, direccionB, direccionC, fechaActual, 12, 13, fecahActualMas2Dias, cargasB, 1500,
				sucursal2.getIdSucursal(), sucursal3.getIdSucursal(), sucursal2.getIdSucursal(), false, false, cliente,
				"pendiente");
		hbtDAO.guardar(pedidoB);

		List<Pedido> pedidosB = new ArrayList<Pedido>();
		pedidosB.add(pedidoB);
		sucursal2.setPedidos(pedidosB);
		hbtDAO.modificar(sucursal2);

		// Pedido C - Volumen = 1 - Sucursal = 1

		List<Carga> cargasC = new ArrayList<Carga>();
		Carga cargaC = new Carga(4, 20, 2, 1, 1, 1, "Normal", "Normal", 4, false, "Apilable", true, "Electronico");
		cargasC.add(cargaC);
		hbtDAO.guardar(cargaC);

		Pedido pedidoC = new Pedido(5, direccionA, direccionC, fechaActual, 12, 13, fecahActualMasTresMinutos, cargasC, 1500,
				sucursal1.getIdSucursal(), sucursal3.getIdSucursal(), sucursal1.getIdSucursal(), false, false, cliente,
				"pendiente");
		hbtDAO.guardar(pedidoC);

		pedidosA.add(pedidoC);
		sucursal1.setPedidos(pedidosA);
		hbtDAO.modificar(sucursal1);

		// Rutas y Trayectos

		List<Trayecto> trayectosA = new ArrayList<Trayecto>();
		List<Trayecto> trayectosB = new ArrayList<Trayecto>();
		List<Trayecto> trayectosC = new ArrayList<Trayecto>();
		List<Trayecto> trayectosD = new ArrayList<Trayecto>();

		Trayecto trayectoA = new Trayecto(1, sucursal1, sucursal2, 1, 400, 200);
		Trayecto trayectoB = new Trayecto(2, sucursal2, sucursal3, 1, 400, 200);
		trayectosA.add(trayectoA);
		trayectosA.add(trayectoB);
		trayectosC.add(trayectoB);
		hbtDAO.guardar(trayectoA);
		hbtDAO.guardar(trayectoB);

		Trayecto trayectoC = new Trayecto(3, sucursal1, sucursal2, 1, 200, 100);
		Trayecto trayectoD = new Trayecto(4, sucursal2, sucursal3, 1, 200, 100);
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

		// Vehiculos - Volumen = 8

		PlanDeMantenimiento planDeMantenimiento = new PlanDeMantenimiento(4, 10, 2, 10000);
		hbtDAO.guardar(planDeMantenimiento);

		Vehiculo vehiculoA = new Vehiculo(3, "Propio", 8, 3500, 2, 2, 3, 1500, 10200, "Libre", false, true, null,
				planDeMantenimiento, sucursal1.getIdSucursal());
		hbtDAO.guardar(vehiculoA);

		Vehiculo vehiculoB = new Vehiculo(4, "Propio", 8, 3500, 2, 2, 3, 1500, 10200, "Libre", false, false, null,
				planDeMantenimiento, sucursal1.getIdSucursal());
		hbtDAO.guardar(vehiculoB);

		VehiculoTercero vehiculoTerceroA = new VehiculoTercero(1, "Semirremolque Con Barandas", 2000, "Libre", null,
				null);
		hbtDAO.guardar(vehiculoTerceroA);

		VehiculoTercero vehiculoTerceroB = new VehiculoTercero(2, "Avioneta", 5000, "Libre", null, null);
		hbtDAO.guardar(vehiculoTerceroB);

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
	
	@Override
	public SucursalDTO obtenerSucursalPorId(int idSucursal) throws RemoteException {
		return hbtDAO.obtenerSucursalPorId(idSucursal);
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
		
		p.setEstado("pendiente");
		
		p.setSucursalActualId(pe.getSucursalActualId());
		p.setSucursalDestinoId(pe.getSucursalDestinoId());
		p.setSucursalOrigenId(pe.getSucursalOrigenId());
		
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
		c.setVolumen(cd.getAlto() * cd.getAncho() * cd.getProfundidad());
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
	
	@Override
	public DireccionDTO obtenerDireccionPorId(int idDireccion) throws RemoteException {
		return hbtDAO.obtenerDireccionPorId(idDireccion);
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
	
	@Override
	public PlanDeMantenimientoDTO obtenerPlanDeMantenimientoPorId(int idPlanDeMantenimiento) throws RemoteException {
		return hbtDAO.obtenerPlanDeMantenimientoPorId(idPlanDeMantenimiento);
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

}
