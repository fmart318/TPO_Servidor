package dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dto.CargaDTO;
import dto.ClienteDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.HabilitadoDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.PrecioVehiculoDTO;
import dto.ProductoDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SeguroDTO;
import dto.SucursalDTO;
import dto.TransporteDTO;
import dto.TrayectoDTO;
import dto.VehiculoAMantenerDTO;
import dto.VehiculoDTO;
import entities.Carga;
import entities.Cliente;
import entities.Direccion;
import entities.Empresa;
import entities.Envio;
import entities.Factura;
import entities.Habilitado;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.PrecioVehiculo;
import entities.Remito;
import entities.Ruta;
import entities.Seguro;
import entities.Sucursal;
import entities.Transporte;
import entities.Trayecto;
import entities.Vehiculo;
import hbt.HibernateUtil;
import hbt.PersistentObject;

public class HibernateDAO {

	private static HibernateDAO instancia;
	private static SessionFactory sessionFactory = null;
	private static Session session = null;

	public static HibernateDAO getInstancia() {
		if (instancia == null) {
			sessionFactory = HibernateUtil.getSessionfactory();
			instancia = new HibernateDAO();
		}
		return instancia;
	}

	public Session getSession() {
		if (session == null || !session.isOpen()) {
			session = sessionFactory.openSession();
		}
		return session;
	}

	public void closeSession() {
		if (session.isOpen()) {
			session.close();
		}
	}

	public void guardar(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {
			t = (Transaction) s.beginTransaction();

			s.save(entidad);
			System.out.println("Object " + entidad.getClass().getName() + " Saved");
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName() + ".guardar");
		}
	}

	public void borrar(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.delete(entidad);
			System.out.println("Object " + entidad.getClass().getName() + " Deleted");
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName() + ".borrar");
		}
	}

	public void modificar(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.update(entidad);
			System.out.println("Object " + entidad.getClass().getName() + " Modified");
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName() + ".modificar");
		}
	}

	public void mergear(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.merge(entidad);
			t.commit();

			// Definir una exception nuestra.
		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName() + ".mergear");
		}
	}

	public int seleccionarViaje(int idViaje) {
		int dias = 0;
		Session s = this.getSession();
		try {
			Date fechaLlegada = (Date) s.createQuery("Select v.fechaLlegada from Viaje v where v.=:id ")
					.setParameter("id", idViaje).uniqueResult();
			Calendar cal = Calendar.getInstance();
			dias = (int) (fechaLlegada.getTime() - cal.getTime().getTime()) / 86400000;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return dias;
	}

	public List<SucursalDTO> obtenerSucursales() {
		List<SucursalDTO> sucursalesDTO = new ArrayList<SucursalDTO>();
		Session s = this.getSession();
		try {
			List<Sucursal> sucursales = s.createQuery("FROM Sucursal").list();
			for (Sucursal sucursal : sucursales) {
				sucursalesDTO.add(sucursal.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursalesDTO;
	}

	public List<CargaDTO> listarCargas() {
		List<CargaDTO> cargas = new ArrayList<CargaDTO>();
		Session s = this.getSession();
		try {
			List<entities.Carga> cs = s.createQuery("FROM Carga").list();
			for (entities.Carga c : cs) {
				cargas.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}

	public List<HabilitadoDTO> listarHabilitados() {
		List<HabilitadoDTO> cargas = new ArrayList<HabilitadoDTO>();
		Session s = this.getSession();
		try {
			List<entities.Habilitado> cs = s.createQuery("FROM Habilitado").list();
			for (entities.Habilitado c : cs) {
				cargas.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}

	public List<ProductoDTO> listarProducto() {
		List<ProductoDTO> cargas = new ArrayList<ProductoDTO>();
		Session s = this.getSession();
		try {
			List<entities.Producto> cs = s.createQuery("FROM Producto").list();
			for (entities.Producto c : cs) {
				cargas.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}

	public List<CargaDTO> listarCargasSinDespachar() {
		List<CargaDTO> cargas = new ArrayList<CargaDTO>();
		Session s = this.getSession();
		try {
			List<entities.Carga> cs = s.createQuery("FROM Carga as c where c.despachado=:desp")
					.setParameter("desp", false).list();
			for (entities.Carga c : cs) {
				cargas.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}

	public List<DireccionDTO> obtenerDirecciones() {
		List<DireccionDTO> direcciones = new ArrayList<DireccionDTO>();
		Session s = this.getSession();
		try {
			List<Direccion> cs = s.createQuery("FROM Direccion").list();
			for (Direccion c : cs) {
				direcciones.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return direcciones;
	}

	/* Tested and Passed */
	public ParticularDTO obtenerClienteParticular(int DNI) {
		ParticularDTO particularDTO = new ParticularDTO();
		Session s = this.getSession();
		try {
			Particular particular = (Particular) s.createQuery("FROM Particular p where p.DNI=:dni")
					.setParameter("dni", DNI).uniqueResult();
			particularDTO = particular.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return particularDTO;
	}

	/* Tested and Passed */
	public List<SeguroDTO> obtenerSegurosParaCarga(String tipoMercaderia) {
		List<SeguroDTO> segurosDTO = new ArrayList<SeguroDTO>();
		Session s = this.getSession();
		try {
			List<Seguro> seguros = s.createQuery("FROM Seguro s where s.tipoMercaderia=:tipoMercaderia")
					.setParameter("tipoMercaderia", tipoMercaderia).list();
			for (Seguro seguro : seguros) {
				segurosDTO.add(seguro.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return segurosDTO;
	}

	public List<VehiculoDTO> obtenerVehiculos() {
		List<VehiculoDTO> vehiculosDTO = new ArrayList<VehiculoDTO>();
		Session s = this.getSession();
		try {
			List<Vehiculo> vehiculos = s.createQuery("FROM Vehiculo").list();
			for (Vehiculo vehiculo : vehiculos) {
				vehiculosDTO.add(vehiculo.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculosDTO;
	}

	public VehiculoDTO obtenerVehiculo(int id) {
		VehiculoDTO vehiculoDTO = new VehiculoDTO();
		Session s = this.getSession();
		try {
			Vehiculo vehiculo = (Vehiculo) s.createQuery("FROM Vehiculo v where v.id = :vehiculo")
					.setParameter("vehiculo", id).uniqueResult();

			vehiculoDTO = vehiculo.toDTO();
			this.closeSession();
			return vehiculoDTO;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	/* Este esta al pedo, porque el de arriba ya los trae a todos con sus PMs */
	public PlanDeMantenimientoDTO obtenerPlanDeMantenimiento(int idVehiculo) {
		PlanDeMantenimientoDTO planDeMantenimientoDTO = new PlanDeMantenimientoDTO();
		Session s = this.getSession();
		try {
			PlanDeMantenimiento planDeMantenimiento = (PlanDeMantenimiento) s
					.createQuery("Select v.planDeMantenimiento FROM Vehiculo v where v.idVehiculo=:id")
					.setParameter("id", idVehiculo).list();
			planDeMantenimientoDTO = planDeMantenimiento.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return planDeMantenimientoDTO;
	}

	public SucursalDTO obtenerSucursal(SucursalDTO sucursalOrigen) {
		SucursalDTO sucursalDTO = new SucursalDTO();
		Session s = this.getSession();
		try {
			Sucursal suc = (Sucursal) s.createQuery(" FROM Sucursal s where s.idSucursal=:id")
					.setParameter("id", sucursalOrigen.getIdSucursal()).uniqueResult();
			sucursalDTO = suc.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursalDTO;
	}

	public SucursalDTO obtenerSucursal(String nombre) {
		SucursalDTO sucursalDTO = new SucursalDTO();
		Session s = this.getSession();
		try {
			Sucursal suc = (Sucursal) s.createQuery(" FROM Sucursal s where s.nombre=:id").setParameter("id", nombre)
					.uniqueResult();
			sucursalDTO = suc.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursalDTO;
	}

	public List<PedidoDTO> obtenerPedidosDeCliente(int idCliente) {
		List<PedidoDTO> pedidosDTO = new ArrayList<PedidoDTO>();
		Session s = this.getSession();
		try {
			List<Pedido> pedidos = s.createQuery("from Pedido p where p.cliente.idCliente =:id ")
					.setParameter("id", idCliente).list();
			for (Pedido pedido : pedidos) {
				pedidosDTO.add(pedido.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return pedidosDTO;
	}

	public List<TransporteDTO> obtenerTransportesDeTerceros(CargaDTO c, TransporteDTO tr) {
		List<TransporteDTO> transportesDTO = new ArrayList<TransporteDTO>();
		String mercaderia = c.getTipoMercaderia();
		String trans = tr.getTipoTransporte();
		// Transporte tiene un tipo MERcaderia, que no esta en el DC asi que no
		// lo tengo en cuenta

		Session s = this.getSession();
		try {
			List<Transporte> transportes = s
					.createQuery(
							"Select p.Transporte from Proveedor p  where p.tipoMercaderia=(:mer) and p.Transporte.tipoTransporte=(:trans) ")
					.setParameter("mer", mercaderia).setParameter("trans", trans).list();
			for (Transporte transporte : transportes) {
				transportesDTO.add(transporte.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return transportesDTO;

	}

	public List<RutaDTO> obtenerRutas() {
		List<RutaDTO> rutasDTO = new ArrayList<RutaDTO>();
		Session s = this.getSession();
		try {
			List<Ruta> rutas = s.createQuery("FROM Ruta").list();
			for (Ruta ruta : rutas) {
				rutasDTO.add(ruta.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return rutasDTO;
	}

	public TrayectoDTO obtenerTrayecto(TrayectoDTO trayDTO) {
		TrayectoDTO t = new TrayectoDTO();
		Session s = this.getSession();
		int idOrigen = trayDTO.getSucursalOrigen().getIdSucursal();
		int idDestino = trayDTO.getSucursalDestino().getIdSucursal();
		try {
			Trayecto tr = (Trayecto) s
					.createQuery(
							"FROM Trayecto t where t.idSucursalOrigen.idSucursal =:idOrigen and t.idSucursalDestino.idSucursal=:idDestino")
					.setParameter("idOrigen", idOrigen).setParameter("idDestino", idDestino).uniqueResult();

			t = tr.toDTO();
			this.closeSession();
			return t;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	public List<PedidoDTO> obtenerPedidos() {
		List<PedidoDTO> pedidosDTO = new ArrayList<PedidoDTO>();
		Session s = this.getSession();
		try {
			List<Pedido> pedidos = s.createQuery("FROM Pedido").list();
			for (Pedido p : pedidos) {
				pedidosDTO.add(p.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return pedidosDTO;
	}

	public List<ClienteDTO> obtenerClientes() {
		List<ClienteDTO> clientesDTO = new ArrayList<ClienteDTO>();
		Session s = this.getSession();
		try {
			List<Cliente> clientes = s.createQuery("FROM Cliente").list();
			for (Cliente c : clientes) {
				clientesDTO.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientesDTO;
	}

	public List<EmpresaDTO> obtenerClientesEmpresa() {
		List<EmpresaDTO> clientesDTO = new ArrayList<EmpresaDTO>();
		Session s = this.getSession();
		try {
			List<Empresa> clientes = s.createQuery("FROM Empresa").list();
			for (Empresa c : clientes) {
				clientesDTO.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientesDTO;
	}

	public List<ParticularDTO> obtenerClientesParticular() {
		List<ParticularDTO> clientesDTO = new ArrayList<ParticularDTO>();
		Session s = this.getSession();
		try {
			List<Particular> clientes = s.createQuery("FROM Particular").list();
			for (Particular c : clientes) {
				clientesDTO.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientesDTO;
	}

	public ClienteDTO obtenerClientePorID(int id) {
		ClienteDTO cl = new ClienteDTO();
		Session s = this.getSession();
		try {
			Cliente c = (Cliente) s.createQuery("FROM Cliente c where c.id=:id").setParameter("id", id).uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;
	}

	public EnvioDTO obtenerEnvioActualDePedido(int idPedido) {

		Session session = this.getSession();
		try {

			List<Envio> envios = (List<Envio>) session.createQuery("from Envio").list();
			if (envios != null) {
				for (Envio envio : envios) {
					for (Pedido pedido : envio.getPedidos()) {
						if (pedido.getIdPedido() == idPedido && !envio.getEstado().equals("listo")) {
							return envio.toDTO();
						}
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
			this.closeSession();
			return null;
		}
		this.closeSession();
		return null;
	}

	public List<CargaDTO> obtenerCargasDeUnPedido(PedidoDTO pedido) {
		List<CargaDTO> cargasDTO = new ArrayList<CargaDTO>();
		Session s = this.getSession();
		try {
			List<Carga> cargas = s.createQuery("SELECT p.cargas FROM Pedido p JOIN p.cargas WHERE p.idPedido=:id")
					.setParameter("id", pedido.getIdPedido()).list();
			for (Carga c : cargas) {
				cargasDTO.add(c.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargasDTO;
	}

	public PedidoDTO obtenerPedido(int idPedido) {
		PedidoDTO pedidoDTO = new PedidoDTO();
		Session s = this.getSession();
		try {
			Pedido pedido = (Pedido) s.createQuery("FROM Pedido p WHERE p.idPedido=:id").setParameter("id", idPedido)
					.uniqueResult();
			pedidoDTO = pedido.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return pedidoDTO;
	}

	public List<HabilitadoDTO> obtenerHabilitados() {
		List<HabilitadoDTO> habilitadosDTO = new ArrayList<HabilitadoDTO>();
		Session s = this.getSession();
		try {
			List<Habilitado> habilitados = s.createQuery("FROM Habilitado").list();
			for (Habilitado h : habilitados) {
				habilitadosDTO.add(h.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return habilitadosDTO;
	}

	public String validarCredenciales(String username, String password) {
		String string = "No Valido";
		Session s = this.getSession();
		try {
			string = (String) s
					.createQuery("Select c.type FROM Credential c WHERE c.username=:username and c.password=:password")
					.setParameter("username", username).setParameter("password", password).uniqueResult();

		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return string;
	}

	public List<TransporteDTO> obtenerTransportes() {
		List<TransporteDTO> transportesDTO = new ArrayList<TransporteDTO>();
		Session s = this.getSession();
		try {
			List<Transporte> transportes = s.createQuery("FROM Transporte").list();
			for (Transporte t : transportes) {
				transportesDTO.add(t.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return transportesDTO;
	}

	public List<EnvioDTO> listarEnvios() {
		List<EnvioDTO> enviosDTO = new ArrayList<EnvioDTO>();
		Session s = this.getSession();
		try {
			List<Envio> envios = s.createQuery("FROM Envio").list();
			for (Envio e : envios)
				enviosDTO.add(e.toDTO());
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return enviosDTO;
	}

	public List<TrayectoDTO> obtenerTrayectos() {
		List<TrayectoDTO> trayectoDtos = new ArrayList<TrayectoDTO>();
		Session s = this.getSession();
		try {
			List<Trayecto> trayectos = s.createQuery("FROM Trayecto").list();
			for (Trayecto trayecto : trayectos) {
				trayectoDtos.add(trayecto.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return trayectoDtos;
	}

	public List<EnvioDTO> obtenerEnvios() {
		List<EnvioDTO> enviosDTO = new ArrayList<EnvioDTO>();
		Session session = this.getSession();
		try {
			List<Envio> envios = session.createQuery("FROM Envio").list();
			for (Envio envio : envios) {
				enviosDTO.add(envio.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return enviosDTO;
	}

	public CargaDTO buscarCargaPorId(int idCarga) {
		CargaDTO cl = new CargaDTO();
		Session s = this.getSession();
		try {
			Carga c = (Carga) s.createQuery("FROM Carga c where c.id=:id").setParameter("id", idCarga).uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	public VehiculoDTO buscarVehiculoPorId(int idVehiculo) {
		VehiculoDTO cl = new VehiculoDTO();
		Session s = this.getSession();
		try {
			Vehiculo v = (Vehiculo) s.createQuery("FROM Vehiculo c where c.id=:id").setParameter("id", idVehiculo)
					.uniqueResult();
			cl = v.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	public SucursalDTO buscarSucursalPorID(int idSucursal) {
		SucursalDTO cl = new SucursalDTO();
		Session s = this.getSession();
		try {
			Sucursal c = (Sucursal) s.createQuery("FROM Sucursal c where c.id=:id").setParameter("id", idSucursal)
					.uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	public PedidoDTO buscarPedidoPorId(int idPedido) {
		PedidoDTO cl = new PedidoDTO();
		Session s = this.getSession();
		try {
			Pedido c = (Pedido) s.createQuery("FROM Pedido c where c.id=:id").setParameter("id", idPedido)
					.uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	public EnvioDTO buscarEnvioPorId(int idEnvio) {
		EnvioDTO cl = new EnvioDTO();
		Session s = this.getSession();
		try {
			Envio c = (Envio) s.createQuery("FROM Envio c where c.id=:id").setParameter("id", idEnvio).uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	public PrecioVehiculoDTO buscarPrecioVehiculoDTO(int idPrecioVehiculo) {
		PrecioVehiculoDTO cl = new PrecioVehiculoDTO();
		Session s = this.getSession();
		try {
			PrecioVehiculo c = (PrecioVehiculo) s.createQuery("FROM PrecioVehiculo c where c.id=:id")
					.setParameter("id", idPrecioVehiculo).uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;

	}

	// Plan de Mantenimiento
	@SuppressWarnings("unchecked")
	public List<PlanDeMantenimientoDTO> listarPlanesDeMantenimiento() {
		List<PlanDeMantenimientoDTO> planesDto = new ArrayList<PlanDeMantenimientoDTO>();
		Session s = this.getSession();
		try {
			List<PlanDeMantenimiento> planes = s.createQuery("FROM PlanDeMantenimiento").list();
			for (PlanDeMantenimiento plan : planes) {
				planesDto.add(plan.toDTO());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return planesDto;
	}

	public void updatePlanDeMantenimiento(PersistentObject plan) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {
			t = s.beginTransaction();
			s.update(plan);
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + plan.getClass().getName() + ".modificar");
		}
	}

	@SuppressWarnings("unchecked")
	public List<VehiculoAMantenerDTO> getVehiculosAMantener() {
		List<Vehiculo> vehiculos = new ArrayList<Vehiculo>();
		List<VehiculoAMantenerDTO> mantener = new ArrayList<VehiculoAMantenerDTO>();
		VehiculoAMantenerDTO aMantener;
		Session s = this.getSession();
		try {
			vehiculos = s.createQuery("FROM Vehiculo").list();
			for (Vehiculo vehiculo : vehiculos) {
				if (hayQueMantener(vehiculo)) {
					aMantener = new VehiculoAMantenerDTO();
					aMantener.setIdVehiculo(vehiculo.getIdVehiculo());
					aMantener.setHayQueMantener("Si");
					aMantener.setTipoDeTrabajo(getTipoTrabajo(vehiculo));
					aMantener.setPuntoAControlar(vehiculo.getPlanDeMantenimiento().getPuntoAControlar());
					aMantener.setTareas(vehiculo.getPlanDeMantenimiento().getTareas());
					aMantener.setEstado(vehiculo.getEstado());
					aMantener.setVehiculo(vehiculo.toDTO());
					mantener.add(aMantener);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mantener;
	}

	private boolean hayQueMantener(Vehiculo vehiculo) {
		PlanDeMantenimiento plan = vehiculo.getPlanDeMantenimiento();
		long time = Calendar.getInstance().getTimeInMillis();
		long planTime;
		if (vehiculo.getFechaUltimoControl() != null) {
			planTime = plan.getDiasProxControl() + vehiculo.getFechaUltimoControl().getTime();
		} else {
			planTime = time + 1;
		}
		if (vehiculo.getKilometraje() % 10000 == 0 || vehiculo.getKilometraje() >= plan.getKmProxControl()
				|| time >= planTime) {
			return true;
		}
		return false;
	}

	private String getTipoTrabajo(Vehiculo vehiculo) {
		String tipo;
		if (vehiculo.isEnGarantia()) {
			tipo = "En Garantia: Llevar a la agencia oficial";
		} else if (vehiculo.isTrabajoEspecifico()) {
			tipo = "Trabajo Especifico: Llevar a taller";
		} else {
			tipo = "Trabajo General: Llevar a lubricentro";
		}
		return tipo;
	}

	public List<PrecioVehiculoDTO> listarVTerceros() {
		List<PrecioVehiculoDTO> vehiculos = new ArrayList<PrecioVehiculoDTO>();
		Session s = this.getSession();
		try {
			List<PrecioVehiculo> vs = s.createQuery("FROM PrecioVehiculo").list();
			for (PrecioVehiculo e : vs)
				vehiculos.add(e.toDTO());
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculos;
	}

	// Facturas
	public List<FacturaDTO> listarFacturas() {
		List<FacturaDTO> facturasDTO = new ArrayList<FacturaDTO>();
		Session s = this.getSession();
		try {
			List<Factura> facturas = s.createQuery("FROM Factura").list();
			for (Factura factura : facturas) {
				facturasDTO.add(factura.toDTO());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return facturasDTO;
	}

	// Remitos
	public List<RemitoDTO> listarRemitos() {
		List<RemitoDTO> remitosDTO = new ArrayList<RemitoDTO>();
		Session s = this.getSession();
		try {
			List<Remito> remitos = s.createQuery("FROM Remito").list();
			for (Remito remito : remitos) {
				remitosDTO.add(remito.toDTO());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return remitosDTO;
	}

}
