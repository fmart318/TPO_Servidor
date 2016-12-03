package dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entities.Carga;
import entities.Cliente;
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

		} catch (Exception e) {
			t.rollback();
			System.out.println(e);
			System.out.println("ErrorDAO: " + entidad.getClass().getName() + ".mergear");
		}
	}
	
	/**-------------------------------------------------------------------------------------------**/

	//Sucursales
	/**
	 * Devuelve una lista de entities sucursales
	 */
	@SuppressWarnings("unchecked")
	public List<Sucursal> obtenerSucursales() {
		List<Sucursal> sucursales = new ArrayList<Sucursal>();
		Session s = this.getSession();
		try {
			sucursales = s.createQuery("FROM Sucursal").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursales;
	}
	
	/**
	 * Devuelve una entity sucursal buscada por id
	 */
	public Sucursal obtenerSucursalPorId(int idSucursal) {
		Sucursal sucursal = new Sucursal();
		Session s = this.getSession();
		try {
			sucursal = (Sucursal) s.createQuery("FROM Sucursal c where c.id=:id")
					.setParameter("id", idSucursal).uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return sucursal;
	}
	
	//Cargas
	/**
	 * Devuelve una lista de entities cargas
	 */
	@SuppressWarnings("unchecked")
	public List<Carga> listarCargas() {
		List<Carga> cargas = new ArrayList<Carga>();
		Session s = this.getSession();
		try {
			cargas = s.createQuery("FROM Carga").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}
	
	/**
	 * Devuelve una entity carga buscada por id
	 */
	public Carga buscarCargaPorId(int idCarga) {
		Carga carga = new Carga();
		Session s = this.getSession();
		try {
			carga = (Carga) s.createQuery("FROM Carga c where c.id=:id").setParameter("id", idCarga).uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return carga;
	}

	/**
	 * Devuelve una lista de entities cargas que no fueron despachadas
	 */
	@SuppressWarnings("unchecked")
	public List<Carga> listarCargasSinDespachar() {
		List<Carga> cargas = new ArrayList<Carga>();
		Session s = this.getSession();
		try {
			cargas = s.createQuery("FROM Carga as c where c.despachado=:desp")
					.setParameter("desp", false).list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return cargas;
	}

	//Direcciones
	/**
	 * Devuelve una lista de entities direcciones
	 */
	@SuppressWarnings("unchecked")
	public List<Direccion> obtenerDirecciones() {
		List<Direccion> direcciones = new ArrayList<Direccion>();
		Session s = this.getSession();
		try {
			direcciones = s.createQuery("FROM Direccion").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return direcciones;
	}
	
	/**
	 * Devuelve una entity direecion buscada por id
	 */
	public Direccion obtenerDireccionPorId(int idDireccion) {
		Direccion direccion = new Direccion();
		Session s = this.getSession();
		try {
			direccion = (Direccion) s.createQuery("FROM Direccion c where c.id=:id")
					.setParameter("id", idDireccion).uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return direccion;
	}

	//Vehiculos
	/**
	 * Devuelve una lista de entity vehiculos
	 */
	@SuppressWarnings("unchecked")
	public List<Vehiculo> obtenerVehiculos() {
		List<Vehiculo> vehiculos = new ArrayList<Vehiculo>();
		Session s = this.getSession();
		try {
			vehiculos = s.createQuery("FROM Vehiculo").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculos;
	}

	/**
	 * Devuelve un entity vehiculo buscado por id
	 */
	public Vehiculo obtenerVehiculo(int id) {
		Vehiculo vehiculo = new Vehiculo();
		Session s = this.getSession();
		try {
			vehiculo = (Vehiculo) s.createQuery("FROM Vehiculo v where v.id = :vehiculo")
					.setParameter("vehiculo", id).uniqueResult();

		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculo;
	}

	//Vehiculo de tercero
	/**
	 * Devuelve una lista de entities vehiculos de tercero
	 */
	@SuppressWarnings("unchecked")
	public List<VehiculoTercero> listarVTerceros() {
		List<VehiculoTercero> vehiculos = new ArrayList<VehiculoTercero>();
		Session s = this.getSession();
		try {
			vehiculos = s.createQuery("FROM VehiculoTercero").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculos;
	}
	
	/**
	 * Devuelve una entity vehiculo de tercero buscado por id
	 */
	public VehiculoTercero buscarVehiculoTerceroDTO(int idVehiculoTercero) {
		VehiculoTercero vehiculoTercero = new VehiculoTercero();
		Session s = this.getSession();
		try {
			vehiculoTercero = (VehiculoTercero) s.createQuery("FROM VehiculoTercero c where c.id=:id")
					.setParameter("id", idVehiculoTercero).uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return vehiculoTercero;
	}

	// Plan de Mantenimiento
	/**
	 * Devuelve una lista de entities planes de mantenimiento
	 */
	@SuppressWarnings("unchecked")
	public List<PlanDeMantenimiento> listarPlanesDeMantenimiento() {
		List<PlanDeMantenimiento> planes = new ArrayList<PlanDeMantenimiento>();
		Session s = this.getSession();
		try {
			planes = s.createQuery("FROM PlanDeMantenimiento").list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return planes;
	}

	/**
	 * Devuelve una entity plan de mantenimiento buscada por id
	 */
	public PlanDeMantenimiento obtenerPlanDeMantenimientoPorId(int idPlanDeMantenimiento) {
		PlanDeMantenimiento plan = new PlanDeMantenimiento();
		Session s = this.getSession();
		try {
			plan = (PlanDeMantenimiento) s.createQuery("FROM PlanDeMantenimiento c where c.id=:id")
					.setParameter("id", idPlanDeMantenimiento).uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return plan;
	}
	
	//Rutas
	/**
	 * Devuelve una lista de entities rutas
	 */
	@SuppressWarnings("unchecked")
	public List<Ruta> obtenerRutas() {
		List<Ruta> rutas = new ArrayList<Ruta>();
		Session s = this.getSession();
		try {
			rutas = s.createQuery("FROM Ruta").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return rutas;
	}
	
	//Trayectos
	/**
	 * Devuelve una lista de entities trayectos
	 */
	@SuppressWarnings("unchecked")
	public List<Trayecto> obtenerTrayectos() {
		List<Trayecto> trayectos = new ArrayList<Trayecto>();
		Session s = this.getSession();
		try {
			trayectos = s.createQuery("FROM Trayecto").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return trayectos;
	}

	//Pedidos
	/**
	 * Devuelve una lista de entities pedidos
	 */
	@SuppressWarnings("unchecked")
	public List<Pedido> obtenerPedidos() {
		List<Pedido> pedidos = new ArrayList<Pedido>();
		Session s = this.getSession();
		try {
			pedidos = s.createQuery("FROM Pedido").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return pedidos;
	}
	
	/**
	 * Devuelve un entity pedido buscado por id
	 */
	public Pedido buscarPedidoPorId(int idPedido) {
		Pedido pedido = new Pedido();
		Session s = this.getSession();
		try {
			pedido = (Pedido) s.createQuery("FROM Pedido c where c.id=:id").setParameter("id", idPedido)
					.uniqueResult();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return pedido;
	}

	//Clientes
	/**
	 * Devuelve una lista de entities clientes
	 */
	@SuppressWarnings("unchecked")
	public List<Cliente> obtenerClientes() {
		List<Cliente> clientes = new ArrayList<Cliente>();
		Session s = this.getSession();
		try {
			clientes = s.createQuery("FROM Cliente").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientes;
	}

	//Cliente Empresa
	/**
	 * Devuelve una lista de entities clientes empresa
	 */
	@SuppressWarnings("unchecked")
	public List<Empresa> obtenerClientesEmpresa() {
		List<Empresa> clientes = new ArrayList<Empresa>();
		Session s = this.getSession();
		try {
			clientes = s.createQuery("FROM Empresa").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientes;
	}

	//Cliente Particular
	/**
	 * Devuelve una list de entities clientes particulares
	 */
	@SuppressWarnings("unchecked")
	public List<Particular> obtenerClientesParticular() {
		List<Particular> clientes = new ArrayList<Particular>();
		Session s = this.getSession();
		try {
			clientes = s.createQuery("FROM Particular").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return clientes;
	}

	//Envios
	/**
	 * Devuelve una lista de entities envios
	 */
	@SuppressWarnings("unchecked")
	public List<Envio> obtenerEnvios() {
		List<Envio> envios = new ArrayList<Envio>();
		Session session = this.getSession();
		try {
			envios = session.createQuery("FROM Envio").list();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return envios;
	}
	
	// Facturas
	/**
	 * Devuelve una lista de entities facturas
	 */
	@SuppressWarnings("unchecked")
	public List<Factura> listarFacturas() {
		List<Factura> facturas = new ArrayList<Factura>();
		Session s = this.getSession();
		try {
			facturas = s.createQuery("FROM Factura").list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return facturas;
	}

	// Remitos
	/**
	 * Devuelve una lista de entities remito
	 */
	@SuppressWarnings("unchecked")
	public List<Remito> listarRemitos() {
		List<Remito> remitos = new ArrayList<Remito>();
		Session s = this.getSession();
		try {
			remitos = s.createQuery("FROM Remito").list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return remitos;
	}

	/**
	 * Valida Credenciales
	 */
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
}
