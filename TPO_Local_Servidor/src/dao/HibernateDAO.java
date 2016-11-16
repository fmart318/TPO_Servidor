package dao;

import hbt.HibernateUtil;
import hbt.PersistentObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dto.CargaDTO;
import dto.ClienteDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.HabilitadoDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.RutaDTO;
import dto.SeguroDTO;
import dto.SucursalDTO;
import dto.TransporteDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;
import dto.ViajeDTO;
import entities.Carga;
import entities.Cliente;
import entities.Empresa;
import entities.Envio;
import entities.Habilitado;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.Ruta;
import entities.Seguro;
import entities.Sucursal;
import entities.Transporte;
import entities.Trayecto;
import entities.Vehiculo;
import entities.Viaje;

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
			System.out.println("Object Saved");
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName()
					+ ".guardar");
		}
	}

	public void borrar(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.delete(entidad);
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName()
					+ ".borrar");
		}
	}

	public void modificar(PersistentObject entidad) {
		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.update(entidad);
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName()
					+ ".modificar");
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
			System.out.println("ErrorDAO: " + entidad.getClass().getName()
					+ ".mergear");
		}
	}

	/* Tested and Passed */
	public List<ViajeDTO> obtenerViajesDeCliente(int idCliente) {
		List<ViajeDTO> viajesDTO = new ArrayList<ViajeDTO>();
		Session s = this.getSession();
		try {
			List<Viaje> viajes = s
					.createQuery(
							"From Viaje v Join Envio e where e.idViaje=v.idViaje And e.pedido.cliente.idCliente=:id ")
					.setParameter("id", idCliente).list();
			for (Viaje viaje : viajes) {
				viajesDTO.add(viaje.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return viajesDTO;
	}

	public int seleccionarViaje(int idViaje) {
		int dias = 0;
		Session s = this.getSession();
		try {
			Date fechaLlegada = (Date) s
					.createQuery(
							"Select v.fechaLlegada from Viaje v where v.=:id ")
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

	public List<ViajeDTO> obtenerViajes() {
		List<ViajeDTO> viajesDTO = new ArrayList<ViajeDTO>();
		Session s = this.getSession();
		try {
			List<Viaje> viajes = s.createQuery("FROM Viaje").list();
			for (Viaje viaje : viajes) {
				viajesDTO.add(viaje.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return viajesDTO;
	}

	/* Tested and Passed */
	public ParticularDTO obtenerClienteParticular(int DNI) {
		ParticularDTO particularDTO = new ParticularDTO();
		Session s = this.getSession();
		try {
			Particular particular = (Particular) s
					.createQuery("FROM Particular p where p.DNI=:dni")
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
			List<Seguro> seguros = s
					.createQuery(
							"FROM Seguro s where s.tipoMercaderia=:tipoMercaderia")
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
			Vehiculo vehiculo = (Vehiculo) s
					.createQuery("FROM Vehiculo v where v.id = :vehiculo")
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
					.createQuery(
							"Select v.planDeMantenimiento FROM Vehiculo v where v.idVehiculo=:id")
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
			Sucursal suc = (Sucursal) s
					.createQuery(" FROM Sucursal s where s.idSucursal=:id")
					.setParameter("id", sucursalOrigen.getIdSucursal())
					.uniqueResult();
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
			Sucursal suc = (Sucursal) s
					.createQuery(" FROM Sucursal s where s.nombre=:id")
					.setParameter("id", nombre).uniqueResult();
			sucursalDTO = suc.toDTO();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursalDTO;
	}

	public ViajeDTO obtenerViajePorVehiculo(VehiculoDTO vehiculo) {
		ViajeDTO viaje = new ViajeDTO();
		int id = vehiculo.getIdVehiculo();
		Session s = this.getSession();
		try {
			Viaje v = (Viaje) s
					.createQuery(
							" FROM Viaje v where v.vehiculo.id =:idVehiculo")
					.setParameter("idVehiculo", id).uniqueResult();

			viaje = v.toDTO();
			this.closeSession();
			return viaje;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	public void updateViaje(Viaje viaje) {

		Transaction t = null;
		Session s = sessionFactory.getCurrentSession();
		try {

			t = s.beginTransaction();

			s.update(viaje);
			t.commit();

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + viaje.getClass().getName()
					+ ".modificar");
		}
	}

	public List<PedidoDTO> obtenerPedidosDeCliente(int idCliente) {
		List<PedidoDTO> pedidosDTO = new ArrayList<PedidoDTO>();
		Session s = this.getSession();
		try {
			List<Pedido> pedidos = s
					.createQuery(
							"from Pedido p where p.cliente.idCliente =:id ")
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

	public List<ViajeDTO> obtenerViajesDePedidos(List<PedidoDTO> pedidosDTO) {
		List<ViajeDTO> viajesDTO = new ArrayList<ViajeDTO>();
		int x = 0;

		List<Viaje> aux = new ArrayList<Viaje>();
		Session s = this.getSession();
		for (int i = 0; i < pedidosDTO.size(); i++) {
			x = pedidosDTO.get(i).getIdPedido();
			try {
				aux = s.createQuery(
						"Select e.viajes from Envio e where e.pedido.idPedido IN (:id) ")
						.setParameter("id", x).list();

				for (Viaje viaje : aux) {
					viajesDTO.add(viaje.toDTO());
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}

		this.closeSession();
		return viajesDTO;
	}

	public ViajeDTO obtenerViajeDeEnvio(int id) {
		Session s = this.getSession();
		ViajeDTO viajeDTO = new ViajeDTO();
		try {
			Viaje v = (Viaje) s
					.createQuery("FROM Viaje v JOIN v.envios.idEnvio=:idEnvio ")
					.setParameter("idEnvio", id).uniqueResult();

			viajeDTO = v.toDTO();
			System.out.println(viajeDTO.getIdViaje());
			this.closeSession();
			return viajeDTO;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	public ViajeDTO obtenerViaje(int id) {
		ViajeDTO viajeDTO = new ViajeDTO();
		Session s = this.getSession();
		try {
			Viaje v = (Viaje) s.createQuery("FROM Viaje v where v.id = :viaje")
					.setParameter("viaje", id).uniqueResult();

			viajeDTO = v.toDTO();
			this.closeSession();
			return viajeDTO;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	public List<TransporteDTO> obtenerTransportesDeTerceros(CargaDTO c,
			TransporteDTO tr) {
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
					.setParameter("mer", mercaderia)
					.setParameter("trans", trans).list();
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
					.setParameter("idOrigen", idOrigen)
					.setParameter("idDestino", idDestino).uniqueResult();

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
			Cliente c = (Cliente) s
					.createQuery("FROM Cliente c where c.id=:id")
					.setParameter("id", id).uniqueResult();
			cl = c.toDTO();
		} catch (Exception e) {

			System.out.println(e);

		}
		this.closeSession();
		return cl;
	}

	public EnvioDTO obtenerEnvioDePedido(int idPedido) {
		EnvioDTO envioDTO = new EnvioDTO();

		Envio e = new Envio();
		Session s = this.getSession();

		try {
			e = (Envio) s
					.createQuery(" from Envio e where e.pedido.idPedido=:id  ")
					.setParameter("id", idPedido).uniqueResult();

			if (e.equals(null)) {
				envioDTO = null;
			}
			envioDTO = e.toDTO();

		}

		catch (Exception ex) {

			System.out.println(ex);

			this.closeSession();
			return null;
		}
		this.closeSession();
		return envioDTO;
	}

	public List<CargaDTO> obtenerCargasDeUnPedido(PedidoDTO pedido) {
		List<CargaDTO> cargasDTO = new ArrayList<CargaDTO>();
		Session s = this.getSession();
		try {
			List<Carga> cargas = s
					.createQuery(
							"SELECT p.cargas FROM Pedido p JOIN p.cargas WHERE p.idPedido=:id")
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
			Pedido pedido = (Pedido) s
					.createQuery("FROM Pedido p WHERE p.idPedido=:id")
					.setParameter("id", idPedido).uniqueResult();
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
			List<Habilitado> habilitados = s.createQuery("FROM Habilitado")
					.list();
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
					.createQuery(
							"Select c.type FROM Credential c WHERE c.username=:username and c.password=:password")
					.setParameter("username", username)
					.setParameter("password", password).uniqueResult();

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
			List<Transporte> transportes = s.createQuery("FROM Transporte")
					.list();
			for (Transporte t : transportes) {
				transportesDTO.add(t.toDTO());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return transportesDTO;
	}

	public List<EnvioDTO> obtenerEnvios(String nombre) {
		List<EnvioDTO> enviosDTO = new ArrayList<EnvioDTO>();
		Session s = this.getSession();
		try {
			List<Envio> envios = s.createQuery("FROM Envio").list();
			List<Pedido> pedidos = s.createQuery("FROM Pedido").list();
			System.out.println(nombre);
			for (Envio e : envios) {
				for (Pedido p : pedidos) {

					if (p.getCliente().getNombre().equals(nombre)
							&& e.getPedido().getIdPedido() == p.getIdPedido()) {
						enviosDTO.add(e.toDTO());
						System.out.println("IF");
					}
					System.out.println("FOR");
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return enviosDTO;
	}

}
