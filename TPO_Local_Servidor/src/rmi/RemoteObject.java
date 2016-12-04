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
import entities.Envio;
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

	// Pedidos
	/**
	 * Devuelve una lista de entities pedidos
	 */
	private List<Pedido> obtenerPedidosEntity() {
		return hbtDAO.obtenerPedidos();
	}

	/**
	 * Devuelve una lista de dto pedidos
	 */
	public List<PedidoDTO> obtenerPedidos() {
		List<Pedido> pedidos = obtenerPedidosEntity();
		List<PedidoDTO> pedidosDto = new ArrayList<PedidoDTO>();
		for (Pedido pedido : pedidos) {
			pedidosDto.add(pedido.toDTO());
		}
		return pedidosDto;
	}

	/**
	 * Chequea si el pedido debe ser enviado como urgente
	 */
	public boolean controlarPedidoUrgenteNecesario(Pedido pedido) throws RemoteException {
		Date mejorFechaLLegada = RemoteObjectHelper.calcularMejorFechaLlegada(pedido.getSucursalActualId(),
				pedido.getSucursalDestinoId());

		if (mejorFechaLLegada != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mejorFechaLLegada);
			cal.add(Calendar.DATE, -1);
			Date mejorFechaLLegadaMenosUnDia = cal.getTime();

			if (pedido.hayQueMandarUrgente(mejorFechaLLegada)) {
				return contratarTercero(pedido);
			} else if (pedido.hayQueMandarUrgente(mejorFechaLLegadaMenosUnDia)) {
				return controlarEnvioUrgente(pedido);
			}
		}

		return false;
	}

	/**
	 * Controla que el pedido haya llegado a la sucursal de destino
	 */
	private boolean controlarPedidoLLegoADestinoFinal(Envio envio, Pedido pedido) throws RemoteException {
		if (envio.getSucursalDestinoId() == pedido.getSucursalDestinoId()) {
			System.out.println("-----Pedido llego a Destino Final-----");

			envio.setListo();
			hbtDAO.modificar(envio);

			for (Pedido pedidoDeEnvio : envio.getPedidos()) {
				pedido.setEstadoFinalizado();
				hbtDAO.modificar(pedidoDeEnvio);
			}
			return true;

		} else {
			System.out.println("-----Pedido llego a Destino Intermediario-----");
			return false;
		}
	}

	/**
	 * Crea un nuevo pedido
	 */
	@Override
	public void crearPedido(PedidoDTO pedidoDto) throws RemoteException {
		Pedido pedido = new Pedido();
		Direccion direccionCarga = new Direccion(pedidoDto.getDireccionCarga().getIdDireccion(), pedidoDto.getDireccionCarga().getCalle(),
				pedidoDto.getDireccionCarga().getNumero(), pedidoDto.getDireccionCarga().getPiso(),
				pedidoDto.getDireccionCarga().getDepartamento(), pedidoDto.getDireccionCarga().getCP());
		hbtDAO.guardar(direccionCarga);
		Direccion direccionDestino = new Direccion(pedidoDto.getDireccionDestino().getIdDireccion(), pedidoDto.getDireccionDestino().getCalle(),
				pedidoDto.getDireccionDestino().getNumero(), pedidoDto.getDireccionDestino().getPiso(),
				pedidoDto.getDireccionDestino().getDepartamento(), pedidoDto.getDireccionDestino().getCP());
		hbtDAO.guardar(direccionDestino);
		
		ArrayList<Carga> cargas = new ArrayList<Carga>();
		for (CargaDTO cargaDto : pedidoDto.getCargas()) {
			Carga carga = EntityManager.CargaToEntity(cargaDto);
			carga.setDespachado(true);
			cargas.add(carga);
		}
		pedido.setCargas(cargas);

		List<EmpresaDTO> empresas = obtenerClientesEmpresa();
		for (EmpresaDTO empresaDto : empresas) {
			if (empresaDto.getIdCliente() == pedidoDto.getCliente().getIdCliente()) {
				pedido.setCliente(EntityManager.EmpresaToEntity(empresaDto));
			}
		}

		List<ParticularDTO> particulares = obtenerClientesParticular();
		for (ParticularDTO particularDto : particulares) {
			if (particularDto.getIdCliente() == pedidoDto.getCliente().getIdCliente()) {
				pedido.setCliente(EntityManager.ParticularToEntity(particularDto));
			}
		}

		pedido.setFechaCarga(pedidoDto.getFechaCarga());
		pedido.setFechaMaxima(pedidoDto.getFechaMaxima());
		pedido.setHoraInicio(pedidoDto.getHoraInicio());
		pedido.setHoraFin(pedidoDto.getHoraFin());

		pedido.setDireccionCarga(direccionCarga);
		pedido.setDireccionDestino(direccionDestino);
		pedido.setPrecio(pedidoDto.getPrecio());
		pedido.setSucursalOrigenId(pedidoDto.getSucursalOrigenId());
		pedido.setSucursalDestinoId(pedidoDto.getSucursalDestinoId());
		pedido.setSolicitaAvionetaParticular(pedidoDto.isSolicitaAvionetaParticular());
		pedido.setSolicitaTransporteDirecto(pedidoDto.isSolicitaTransporteDirecto());

		pedido.setEstadoPendiente();

		pedido.setSucursalActualId(pedidoDto.getSucursalActualId());
		pedido.setSucursalDestinoId(pedidoDto.getSucursalDestinoId());
		pedido.setSucursalOrigenId(pedidoDto.getSucursalOrigenId());

		hbtDAO.guardar(pedido);

		Factura factura = new Factura();
		factura.setPedido(pedido);
		factura.setPrecio(pedido.getPrecio());
		hbtDAO.guardar(factura);

		Remito remito = new Remito();
		remito.setPedido(pedido);
		hbtDAO.guardar(remito);
	}

	// Envios
	/**
	 * Chequea si un envio debe ser enviad de forma urgente
	 */
	private boolean controlarEnvioUrgente(Pedido pedido) throws RemoteException {

		System.out.println("-----Pedido requiere un envio urgente-----");

		float cargaTotalPedido = pedido.getVolumenTotalDeCargas();

		List<Vehiculo> vehiculosDisponibles = RemoteObjectHelper
				.obtenerVehiculosDisponiblesEnSucursalEntity(pedido.getSucursalActualId());

		if (vehiculosDisponibles.size() > 0) {

			boolean vehiculoParaPedidoUrgenteDisponible = false;
			for (Vehiculo vehiculo : vehiculosDisponibles) {
				if (vehiculo.alcanzaLugar(cargaTotalPedido)) {

					vehiculoParaPedidoUrgenteDisponible = true;

					List<Pedido> pedidoList = new ArrayList<Pedido>();
					pedidoList.add(pedido);
					generarEnvio(vehiculo, pedidoList);

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

	/**
	 * Chequea el estado de los envios
	 */
	public void controlarEstadoDeEnvios() throws RemoteException {
		List<Pedido> pedidos = obtenerPedidosEntity();
		pedidos = RemoteObjectHelper.ordenarPedidosPorPrioridad(pedidos);

		ArrayList<Pedido> pedidosPendientes = new ArrayList<Pedido>();
		ArrayList<Pedido> pedidosEsperandoSerBuscadas = new ArrayList<Pedido>();

		for (Pedido pedido : pedidos) {
			System.out.println("-----Controlando Pedido " + pedido.getIdPedido() + "-----");

			VehiculoTercero vehiculoTercero = obtenerVehiculoTerceroConPedido(pedido.getIdPedido());
			Envio envio = obtenerEnvioActualDePedido(pedido.getIdPedido());

			// Si el pedido ya llego a su destino
			if (pedido.llegoDestinoFinal()) {
				System.out.println("-----Pedido LLego a su destino final-----");
				pedidosEsperandoSerBuscadas.add(pedido);
			}

			// Si el pedido fue enviado por un Tercero
			else if (vehiculoTercero != null) {
				System.out.println("-----Contactando Tercero-----");
				controlarLLegadaDeTercero(vehiculoTercero, pedido);
			}

			// Si no existe envio para el pedido se crea uno nuevo
			else if (envio == null) {
				System.out.println("-----Verificando si se puede generar un envio-----");
				pedidosPendientes.add(pedido);
				if (!controlarPedidoUrgenteNecesario(pedido)) {
					asignarPedidosPendientesATransporteLibre(RemoteObjectHelper
							.obtenerPedidosConMismoSucursalActual(pedidosPendientes, pedido.getSucursalActualId()));
				}
			}

			// Si el envio esta despachado
			else if (envio.isDespachado()) {
				System.out.println("-----Controlando Llegada de Envio-----");
				controlarLLegadaDeEnvio(envio);
			}

			// Si el envio esta parado
			else if (envio.isParado()) {
				if (!controlarPedidoLLegoADestinoFinal(envio, pedido)) {
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

	/**
	 * Chequea la llegada el envio
	 */
	private void controlarLLegadaDeEnvio(Envio envio) throws RemoteException {

		// Vamos a asumir que el envio siempre llega a tiempo a su destino.
		if (envio.llegoADestino()) {
			Sucursal sucursalDestino = envio.getSucursalDestino();
			Vehiculo vehiculo = hbtDAO.obtenerVehiculo(envio.getIdVehiculo());

			vehiculo.setLlegoADestino(sucursalDestino.getIdSucursal());
			hbtDAO.modificar(vehiculo);

			envio.setllegoADestino();
			hbtDAO.modificar(envio);

			for (Pedido pedido : envio.getPedidos()) {
				pedido.setSucursalActualId(envio.getSucursalDestino().getIdSucursal());
				hbtDAO.modificar(pedido);
			}

			sucursalDestino.addNuevoPedidos(envio.getPedidos());
			hbtDAO.modificar(sucursalDestino);

			System.out.println("-----Envio llego a su destino-----");
		} else {
			System.out.println("-----Envio Sigue en transito-----");
		}
	}

	/**
	 * Genera un nuevo envio
	 */
	private void generarEnvio(Vehiculo vehiculo, List<Pedido> pedidos) throws RemoteException {

		Sucursal sucursalActual = RemoteObjectHelper.getSucursalPorIdEntity(pedidos.get(0).getSucursalActualId());
		Sucursal sucursalDestino = RemoteObjectHelper.getSucursalPorIdEntity(pedidos.get(0).getSucursalDestinoId());
		Ruta rutaVehiculo = RemoteObjectHelper.obtenerMejorRuta(sucursalActual, sucursalDestino);

		if (rutaVehiculo != null) {
			vehiculo.setEnUso();
			hbtDAO.modificar(vehiculo);

			long minutosFechaLlegadaEnvio = rutaVehiculo.getTiempoDelPrimerTrayectoEnMinutos();
			Timestamp fechaInicialEnvio = new Timestamp(System.currentTimeMillis());
			Timestamp fechaLlegadaEnvio = new Timestamp(System.currentTimeMillis() + minutosFechaLlegadaEnvio);

			List<Envio> envios = getEnvios();
			Envio envio = new Envio(envios.size() + 1, fechaInicialEnvio, fechaLlegadaEnvio, true, "despachado",
					pedidos, 1, sucursalActual, rutaVehiculo.getNextSucursal(sucursalActual), vehiculo.getIdVehiculo());
			hbtDAO.guardar(envio);

			System.out.println("-----Envio Creado-----");
		}
	}

	/**
	 * Crea un nuevo envio directo
	 */
	@Override
	public void crearEnvioDirecto(int idPedido) throws RemoteException {
		Pedido pedido = hbtDAO.buscarPedidoPorId(idPedido);
		controlarEnvioUrgente(pedido);
	}

	/**
	 * Devuelve una lista de dto envios
	 */
	public List<EnvioDTO> obtenerEnvios() throws RemoteException {
		List<Envio> envios = getEnvios();
		List<EnvioDTO> enviosDto = new ArrayList<EnvioDTO>();
		for (Envio envio : envios) {
			enviosDto.add(envio.toDTO());
		}
		return enviosDto;
	}

	/**
	 * Devuelve una lista entity de envios
	 */
	private List<Envio> getEnvios() {
		return hbtDAO.obtenerEnvios();
	}

	/**
	 * Devuelve un entity envio el cual es el envio actual de una pedido dado
	 */
	public Envio obtenerEnvioActualDePedido(int idPedido) {
		Envio envioEntity = null;
		List<Envio> envios = hbtDAO.obtenerEnvios();
		if (envios != null) {
			for (Envio envio : envios) {
				for (Pedido pedido : envio.getPedidos()) {
					if (pedido.getIdPedido() == idPedido && !envio.getEstado().equals("listo")) {
						envioEntity = envio;
					}
				}
			}
		}
		return envioEntity;
	}

	// Vehiculos
	// No cambiar el param a vehiculo
	/**
	 * Chequea si hay que mandar un vehiculo a mantenimiento
	 */
	public boolean controlarVehiculo(VehiculoDTO vehiculoDto) {
		Vehiculo vehiculo = EntityManager.VehiculoToEntity(vehiculoDto);

		Calendar c = Calendar.getInstance();
		c.setTime(vehiculo.getFechaUltimoControl());
		c.add(Calendar.DATE, vehiculo.getPlanDeMantenimiento().getDiasProxControl());
		Date fecha = c.getTime();

		boolean Boolean = false;
		if (vehiculo.isEnDeposito()) {
			System.out.println(vehiculo.getKilometraje() + vehiculo.getPlanDeMantenimiento().getKmProxControl());
			if (vehiculo.necesitaMantenimiento()) {
				Boolean = true;
				if (vehiculo.isEnGarantia()) {
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				} else if (vehiculo.isTrabajoEspecifico()) {
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				} else {
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				}
				vehiculo.getPlanDeMantenimiento().setKmProxControl(vehiculo.getKilometraje() + 200);
				vehiculo.getPlanDeMantenimiento().setDiasProxControl(60);
				hbtDAO.modificar(vehiculo);

			} else if (fecha.before(new Date())) {
				Boolean = true;
				if (vehiculo.isEnGarantia()) {
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				} else if (vehiculo.isTrabajoEspecifico()) {
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				} else {
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculo);
				}
				vehiculo.setKmProximoControl(vehiculo.getKilometraje() + 200);
				vehiculo.setDiasProximoControl(60);
				hbtDAO.modificar(vehiculo);
			}
		}
		return Boolean;
	}

	/**
	 * Obtiene una lista de dto vehiculos
	 */
	public List<VehiculoDTO> obtenerVehiculos() {
		List<VehiculoDTO> vehiculosDto = new ArrayList<VehiculoDTO>();
		List<Vehiculo> vehiculosEntity = hbtDAO.obtenerVehiculos();
		for (Vehiculo vehiculo : vehiculosEntity) {
			vehiculosDto.add(vehiculo.toDTO());
		}
		return vehiculosDto;
	}

	/**
	 * Crea un nuevo vehiculo
	 */
	@Override
	public void crearVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.guardar(EntityManager.VehiculoToEntity(v));
	}

	/**
	 * Modifica un vehiculo
	 */
	@Override
	public void modificarVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.modificar(EntityManager.VehiculoToEntity(v));
	}

	/**
	 * Elimina un vehiculo
	 */
	@Override
	public void eliminarVehiculo(VehiculoDTO v) throws RemoteException {
		hbtDAO.borrar(EntityManager.VehiculoToEntity(v));
	}

	/**
	 * Devuelve una lista de Vehiculos a mantener
	 */
	public List<VehiculoAMantenerDTO> getVehiculosAMantener() {
		List<Vehiculo> vehiculos = new ArrayList<Vehiculo>();
		List<VehiculoAMantenerDTO> mantener = new ArrayList<VehiculoAMantenerDTO>();
		VehiculoAMantenerDTO aMantener;
		try {
			vehiculos = hbtDAO.obtenerVehiculos();
			for (Vehiculo vehiculo : vehiculos) {
				if (vehiculo.hayQueMantener()) {
					aMantener = new VehiculoAMantenerDTO(vehiculo.getIdVehiculo(), true, vehiculo.getTipoTrabajo(),
							vehiculo.getPlanDeMantenimiento().getPuntoAControlar(),
							vehiculo.getPlanDeMantenimiento().getTareas(), vehiculo.getEstado(), vehiculo.toDTO());
					mantener.add(aMantener);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mantener;
	}

	// Vehiculos de Terceros
	/**
	 * Controla la llegada de los vehiculos de terceros
	 */
	private void controlarLLegadaDeTercero(VehiculoTercero vehiculoTercero, Pedido pedido) throws RemoteException {

		// Se asume que el tercero siempre llega en el horario predeterminado
		if (vehiculoTercero.llegoADestino()) {
			System.out.println("-----Pedido LLego a su destino final-----");

			vehiculoTercero.liberarVehiculoTercero();
			hbtDAO.modificar(vehiculoTercero);

			Sucursal sucursalDestino = RemoteObjectHelper.getSucursalPorIdEntity(pedido.getSucursalDestinoId());

			pedido.setSucursalActualId(sucursalDestino.getIdSucursal());
			hbtDAO.modificar(pedido);

			sucursalDestino.addNuevoPedido(pedido);
			hbtDAO.modificar(sucursalDestino);

		} else {
			System.out.println("-----Pedido Sigue en camino-----");
		}
	}

	/**
	 * Realiza la contratacion de un vehiculo de tercero
	 */
	private boolean contratarTercero(Pedido pedido) throws RemoteException {

		System.out.println("-----Contratando un Tercero-----");

		List<VehiculoTercero> vehiculosTercerosDisponibles = RemoteObjectHelper
				.obtenerVehiculosTerceroDisponiblesEntity();

		if (vehiculosTercerosDisponibles.size() > 0) {
			VehiculoTercero vehiculoTercero = vehiculosTercerosDisponibles.get(0);
			vehiculoTercero.cargarPerdido(pedido);
			hbtDAO.modificar(vehiculoTercero);
			return true;
		} else {
			System.out.println("-----No hay Tercero Disponible-----");
			return false;
		}
	}

	/**
	 * Devuelve una lista de dto vehiculos de tercero
	 */
	@Override
	public List<VehiculoTerceroDTO> listarVTerceros() throws RemoteException {
		List<VehiculoTercero> vehiculos = hbtDAO.listarVTerceros();
		List<VehiculoTerceroDTO> vehiculosDto = new ArrayList<VehiculoTerceroDTO>();
		for (VehiculoTercero vehiculo : vehiculos) {
			vehiculosDto.add(vehiculo.toDTO());
		}
		return vehiculosDto;
	}

	/**
	 * Crea un nuevo vehiculo de tercero
	 */
	@Override
	public void crearVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.guardar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	/**
	 * Modifica un vehiculo de tercero
	 */
	@Override
	public void modificarVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.modificar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	/**
	 * Elimina un vehiculo de tercero
	 */
	@Override
	public void eliminarVTerceros(VehiculoTerceroDTO vehiculoTercero) throws RemoteException {
		hbtDAO.borrar(EntityManager.VehiculoTerceroToEntity(vehiculoTercero));
	}

	/**
	 * Devuelve un dto vehiculo de tercero buscado por el id
	 */
	@Override
	public VehiculoTerceroDTO buscarVehiculoTerceroDTO(int idVehiculoTerceroDTO) throws RemoteException {
		return hbtDAO.buscarVehiculoTercero(idVehiculoTerceroDTO).toDTO();
	}

	/**
	 * Devuelve una entity vehiculo de tercero que pertenece a un pedido
	 */
	public VehiculoTercero obtenerVehiculoTerceroConPedido(int idPedido) {
		List<VehiculoTercero> vehiculosTercero = new ArrayList<VehiculoTercero>();
		VehiculoTercero vehiculoTercero = null;
		vehiculosTercero = hbtDAO.listarVTerceros();
		for (VehiculoTercero vehiculo : vehiculosTercero) {
			for (Pedido pedido : vehiculo.getPedidos()) {
				if (pedido.getIdPedido() == idPedido) {
					vehiculoTercero = vehiculo;
				}
			}
		}
		return vehiculoTercero;
	}

	/**
	 * Asigna los pedidos pendientes
	 */
	public void asignarPedidosPendientesATransporteLibre(ArrayList<Pedido> pedidosPendientes) throws RemoteException {

		List<Vehiculo> vehiculosDisponibles = RemoteObjectHelper
				.obtenerVehiculosDisponiblesEnSucursalEntity(pedidosPendientes.get(0).getSucursalActualId());

		if (vehiculosDisponibles.size() > 0) {

			boolean seGeneroEnvio = false;
			for (Vehiculo vehiculoDisponible : vehiculosDisponibles) {
				ArrayList<Pedido> pedidosPendientesConvenientes = RemoteObjectHelper
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

	// Cliente Empresa
	/**
	 * Devuelve una lista de dto empresas
	 */
	public List<EmpresaDTO> obtenerClientesEmpresa() {
		List<Empresa> empresas = hbtDAO.obtenerClientesEmpresa();
		List<EmpresaDTO> empresasDto = new ArrayList<EmpresaDTO>();
		for (Empresa empresa : empresas) {
			empresasDto.add(empresa.toDTO());
		}
		return empresasDto;
	}

	/**
	 * Crea un nuevo cliente empresa
	 */
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

	/**
	 * Actualiza un cliente empresa
	 */
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

	/**
	 * Elimina un cliente empresa
	 */
	public void deleteClienteEmpresa(int idCliente) throws RemoteException {
		Empresa clienteEmpresa = new Empresa();
		clienteEmpresa.setIdCliente(idCliente);
		hbtDAO.borrar(clienteEmpresa);
	}

	// Cliente Particular
	/**
	 * Devuelve una lista de dto particulares
	 */
	public List<ParticularDTO> obtenerClientesParticular() {
		List<Particular> particulares = hbtDAO.obtenerClientesParticular();
		List<ParticularDTO> particularesDto = new ArrayList<ParticularDTO>();
		for (Particular particular : particulares) {
			particularesDto.add(particular.toDTO());
		}
		return particularesDto;
	}

	/**
	 * Crea un nuevo cliente particular
	 */
	@Override
	public void altaClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular clienteParticular = new Particular(particularDto.getIdCliente(), particularDto.getNombre(),
				particularDto.getDNI(), particularDto.getApellido());
		hbtDAO.guardar(clienteParticular);
	}

	/**
	 * Actualiza un cliente particular
	 */
	public void updateClienteParticular(ParticularDTO particularDto) throws RemoteException {
		Particular clienteParticular = new Particular(particularDto.getIdCliente(), particularDto.getNombre(),
				particularDto.getDNI(), particularDto.getApellido());
		hbtDAO.modificar(clienteParticular);
	}

	/**
	 * Elimina un cliente particular
	 */
	public void deleteClienteParticular(int idCliente) throws RemoteException {
		Particular clienteParticular = new Particular();
		clienteParticular.setIdCliente(idCliente);
		hbtDAO.borrar(clienteParticular);
	}

	// Sucursal
	/**
	 * Devuelve una lista de dto sucursales
	 */
	public List<SucursalDTO> obtenerSucursales() throws RemoteException {
		List<Sucursal> sucursalesEntitiy = hbtDAO.obtenerSucursales();
		List<SucursalDTO> sucursalesDto = new ArrayList<SucursalDTO>();
		for (Sucursal sucursal : sucursalesEntitiy) {
			sucursalesDto.add(sucursal.toDTO());
		}
		return sucursalesDto;
	}

	/**
	 * Crea una nueva sucursal
	 */
	@Override
	public void altaSucursal(SucursalDTO sucursalDto) throws RemoteException {
		Sucursal sucursal = EntityManager.SucursalToEntity(sucursalDto);
		hbtDAO.guardar(sucursal);
	}

	/**
	 * Elimina una sucursal
	 */
	@Override
	public void deleteSucursal(int idSucursal) throws RemoteException {
		Sucursal sucursal = new Sucursal();
		sucursal.setIdSucursal(idSucursal);
		hbtDAO.borrar(sucursal);
	}

	/**
	 * Actualiza una sucursal existente
	 */
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

	/**
	 * Busca una sucursal por el id
	 */
	@Override
	public SucursalDTO obtenerSucursalPorId(int idSucursal) throws RemoteException {
		Sucursal sucursal = RemoteObjectHelper.getSucursalPorIdEntity(idSucursal);
		if (sucursal != null) {
			return sucursal.toDTO();
		}
		return null;
	}

	// Trayecto
	/**
	 * Devuelve una lista de dto trayectos
	 */
	@Override
	public List<TrayectoDTO> obtenerTrayectos() throws RemoteException {
		List<Trayecto> trayectos = hbtDAO.obtenerTrayectos();
		List<TrayectoDTO> trayectosDto = new ArrayList<TrayectoDTO>();
		for (Trayecto trayecto : trayectos) {
			trayectosDto.add(trayecto.toDTO());
		}
		return trayectosDto;
	}

	/**
	 * Crea un nuevo trayecto
	 */
	@Override
	public void altaTrayecto(TrayectoDTO trayectDto) throws RemoteException {
		hbtDAO.guardar(EntityManager.TrayectoToEntity(trayectDto));
	}

	/**
	 * Actualiza un trayecto
	 */
	@Override
	public void updateTrayecto(TrayectoDTO trayectDto) throws RemoteException {
		hbtDAO.modificar(EntityManager.TrayectoToEntity(trayectDto));
	}

	/**
	 * Elimina un trayecto
	 */
	@Override
	public void deleteTrayecto(int idTrayecto) throws RemoteException {
		Trayecto trayecto = new Trayecto();
		trayecto.setIdTrayecto(idTrayecto);
		hbtDAO.borrar(trayecto);
	}

	// Ruta
	/**
	 * Devuelve una lista de dto rutas
	 */
	@Override
	public List<RutaDTO> obtenerRutas() throws RemoteException {
		List<Ruta> rutas = hbtDAO.obtenerRutas();
		List<RutaDTO> rutasDto = new ArrayList<RutaDTO>();
		for (Ruta ruta : rutas) {
			rutasDto.add(ruta.toDTO());
		}
		return rutasDto;
	}

	/**
	 * Crea una nueva ruta
	 */
	@Override
	public void altaRuta(RutaDTO rutaDto) throws RemoteException {
		Ruta ruta = EntityManager.RutaToEntity(rutaDto);
		hbtDAO.guardar(ruta);
	}

	/**
	 * Actualiza una ruta
	 */
	@Override
	public void updateRuta(RutaDTO rutaDto) throws RemoteException {
		hbtDAO.modificar(EntityManager.RutaToEntity(rutaDto));
	}

	/**
	 * Elimina una ruta
	 */
	@Override
	public void deleteRuta(int idRuta) throws RemoteException {
		Ruta ruta = new Ruta();
		ruta.setIdRuta(idRuta);
		hbtDAO.borrar(ruta);
	}

	// Cargas
	/**
	 * Devuelve una lista de dto cargas
	 */
	@Override
	public List<CargaDTO> listarCargas() throws RemoteException {
		List<CargaDTO> cargasDto = new ArrayList<CargaDTO>();
		List<Carga> cargasEntity = hbtDAO.listarCargas();
		for (Carga carga : cargasEntity) {
			cargasDto.add(carga.toDTO());
		}
		return cargasDto;
	}

	/**
	 * Crea una nueva carga
	 */
	@Override
	public void createCarga(CargaDTO cargaDto) throws RemoteException {
		float volumenCarga = cargaDto.getAlto() * cargaDto.getAncho() * cargaDto.getProfundidad();
		Carga carga = new Carga(cargaDto.getIdCarga(), cargaDto.getPeso(), cargaDto.getAncho(), cargaDto.getAlto(),
				cargaDto.getProfundidad(), volumenCarga, cargaDto.getFragilidad(), cargaDto.getTratamiento(),
				cargaDto.getApilable(), cargaDto.isRefrigerable(), cargaDto.getCondiciones(), cargaDto.isDespachado(),
				cargaDto.getTipoMercaderia());
		hbtDAO.guardar(carga);
	}

	/**
	 * Actualiza una carga
	 */
	@Override
	public void updateCarga(CargaDTO cargaDto) throws RemoteException {
		float volumenCarga = cargaDto.getAlto() * cargaDto.getAncho() * cargaDto.getProfundidad();
		Carga carga = new Carga(cargaDto.getIdCarga(), cargaDto.getPeso(), cargaDto.getAncho(), cargaDto.getAlto(),
				cargaDto.getProfundidad(), volumenCarga, cargaDto.getFragilidad(), cargaDto.getTratamiento(),
				cargaDto.getApilable(), cargaDto.isRefrigerable(), cargaDto.getCondiciones(), cargaDto.isDespachado(),
				cargaDto.getTipoMercaderia());
		hbtDAO.modificar(carga);
	}

	/**
	 * Elimina una carga
	 */
	@Override
	public void deleteCarga(int idCarga) throws RemoteException {
		Carga carga = new Carga();
		carga.setIdCarga(idCarga);
		hbtDAO.borrar(carga);
	}

	/**
	 * Devuelve un dto carga buscado por id
	 */
	@Override
	public CargaDTO buscarCargaPorId(int idCarga) throws RemoteException {
		return hbtDAO.buscarCargaPorId(idCarga).toDTO();
	}

	/**
	 * Devuelve una lista de dto cargas que no fueron despachadas
	 */
	@Override
	public List<CargaDTO> listarCargasSinDespachar() throws RemoteException {
		List<Carga> cargasEntity = hbtDAO.listarCargasSinDespachar();
		List<CargaDTO> cargasDto = new ArrayList<CargaDTO>();
		for (Carga carga : cargasEntity) {
			cargasDto.add(carga.toDTO());
		}
		return cargasDto;
	}

	// Direcciones
	/**
	 * Devuelve una lista de dto direcciones
	 */
	@Override
	public List<DireccionDTO> listarDirecciones() throws RemoteException {
		List<Direccion> direccionesEntity = hbtDAO.obtenerDirecciones();
		List<DireccionDTO> direccionesDto = new ArrayList<DireccionDTO>();
		for (Direccion direccion : direccionesEntity) {
			direccionesDto.add(direccion.toDTO());
		}
		return direccionesDto;
	}

	/**
	 * Crea una nueva direccion
	 */
	@Override
	public void crearDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.guardar(EntityManager.DireccionToEntity(d));
	}

	/**
	 * Modifica una direccion
	 */
	@Override
	public void modificarDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.modificar(EntityManager.DireccionToEntity(d));
	}

	/**
	 * Elimina una direccion
	 */
	@Override
	public void eliminarDireccion(DireccionDTO d) throws RemoteException {
		hbtDAO.borrar(EntityManager.DireccionToEntity(d));
	}

	/**
	 * Devuelve un dto direccion buscado por el id
	 */
	@Override
	public DireccionDTO obtenerDireccionPorId(int idDireccion) throws RemoteException {
		Direccion direccion = hbtDAO.obtenerDireccionPorId(idDireccion);
		return direccion.toDTO();
	}

	// Plan de Mantenimiento
	/**
	 * Devuelve una lista de dto plan de mantenimiento
	 */
	@Override
	public List<PlanDeMantenimientoDTO> listarPlanesDeMantenimiento() throws RemoteException {
		List<PlanDeMantenimiento> planes = hbtDAO.listarPlanesDeMantenimiento();
		List<PlanDeMantenimientoDTO> planesDto = new ArrayList<PlanDeMantenimientoDTO>();
		for (PlanDeMantenimiento plan : planes) {
			planesDto.add(plan.toDTO());
		}
		return planesDto;
	}

	/**
	 * Crea un nuevo plan de mantenimiento
	 */
	public void altaPlanMantenimiento(PlanDeMantenimientoDTO planDeMantenimientoDTO) {
		hbtDAO.guardar(EntityManager.PlanDeMantenimientoToEntity(planDeMantenimientoDTO));
	}

	/**
	 * Elimina un plan de mantenimiento
	 */
	@Override
	public void deletePlanDeMantenimiento(int idPlan) throws RemoteException {
		PlanDeMantenimiento plan = new PlanDeMantenimiento();
		plan.setIdPlanDeMantenimiento(idPlan);
		hbtDAO.borrar(plan);
	}

	/**
	 * Actualiza un plan de mantenimiento
	 */
	@Override
	public void updatePlanDeMantenimiento(PlanDeMantenimientoDTO plan) throws RemoteException {
		hbtDAO.modificar(EntityManager.PlanDeMantenimientoToEntity(plan));
	}

	/**
	 * Devuelve un dto plan de mantenimiento buscado por id
	 */
	@Override
	public PlanDeMantenimientoDTO obtenerPlanDeMantenimientoPorId(int idPlanDeMantenimiento) throws RemoteException {
		return hbtDAO.obtenerPlanDeMantenimientoPorId(idPlanDeMantenimiento).toDTO();
	}

	// Facturas
	/**
	 * Devuvle una lista de dto facturas
	 */
	@Override
	public List<FacturaDTO> listarFacturas() throws RemoteException {
		List<Factura> facturas = hbtDAO.listarFacturas();
		List<FacturaDTO> facturasDto = new ArrayList<FacturaDTO>();
		for (Factura factura : facturas) {
			facturasDto.add(factura.toDTO());
		}
		return facturasDto;
	}

	/**
	 * Crea una nueva factura
	 */
	public void altaFactura(FacturaDTO facturaDTO) {
		hbtDAO.guardar(EntityManager.FacturaToEntity(facturaDTO));
	}

	/**
	 * Elimina una factura
	 */
	@Override
	public void deleteFactura(int idFactura) throws RemoteException {
		Factura factura = new Factura();
		factura.setIdFactura(idFactura);
		hbtDAO.borrar(factura);
	}

	// Remitos
	/**
	 * Devuelve una lista de dto remitos
	 */
	@Override
	public List<RemitoDTO> listarRemitos() throws RemoteException {
		List<Remito> remitos = hbtDAO.listarRemitos();
		List<RemitoDTO> remitosDto = new ArrayList<RemitoDTO>();
		for (Remito remito : remitos) {
			remitosDto.add(remito.toDTO());
		}
		return remitosDto;
	}

	/**
	 * Crea un nuevo remito
	 */
	public void altaRemito(RemitoDTO remitoDTO) {
		hbtDAO.guardar(EntityManager.RemitoToEntity(remitoDTO));
	}

	/**
	 * Elimina un remito
	 */
	@Override
	public void deleteRemito(int idRemito) throws RemoteException {
		Remito remito = new Remito();
		remito.setIdRemito(idRemito);
		hbtDAO.borrar(remito);
	}

	// Datos Iniciales
	/**
	 * Crea los datos iniciales para el programa
	 */
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

		Pedido pedidoC = new Pedido(5, direccionA, direccionC, fechaActual, 12, 13, fecahActualMasTresMinutos, cargasC,
				1500, sucursal1.getIdSucursal(), sucursal3.getIdSucursal(), sucursal1.getIdSucursal(), false, false,
				cliente, "pendiente");
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
}
