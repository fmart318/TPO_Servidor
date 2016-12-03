package dao;

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
		Session s = this.getSession();
		try {
			List<Sucursal> sucursales = s.createQuery("FROM Sucursal").list();
			return sucursales;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	/**
	 * Devuelve una entity sucursal buscada por id
	 */
	public Sucursal obtenerSucursalPorId(int idSucursal) {
		
		Session s = this.getSession();
		try {
			Sucursal sucursal = (Sucursal) s.createQuery("FROM Sucursal c where c.id=:id")
					.setParameter("id", idSucursal).uniqueResult();
			return sucursal;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	//Cargas
	/**
	 * Devuelve una lista de entities cargas
	 */
	@SuppressWarnings("unchecked")
	public List<Carga> listarCargas() {
		
		Session s = this.getSession();
		try {
			List<Carga> cargas = s.createQuery("FROM Carga").list();
			return cargas;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	/**
	 * Devuelve una entity carga buscada por id
	 */
	public Carga buscarCargaPorId(int idCarga) {
		
		Session s = this.getSession();
		try {
			Carga carga = (Carga) s.createQuery("FROM Carga c where c.id=:id").setParameter("id", idCarga).uniqueResult();
			return carga;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	/**
	 * Devuelve una lista de entities cargas que no fueron despachadas
	 */
	@SuppressWarnings("unchecked")
	public List<Carga> listarCargasSinDespachar() {
		
		Session s = this.getSession();
		try {
			List<Carga> cargas = s.createQuery("FROM Carga as c where c.despachado=:desp")
					.setParameter("desp", false).list();
			return cargas;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Direcciones
	/**
	 * Devuelve una lista de entities direcciones
	 */
	@SuppressWarnings("unchecked")
	public List<Direccion> obtenerDirecciones() {
		
		Session s = this.getSession();
		try {
			List<Direccion> direcciones = s.createQuery("FROM Direccion").list();
			return direcciones;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	/**
	 * Devuelve una entity direecion buscada por id
	 */
	public Direccion obtenerDireccionPorId(int idDireccion) {
		
		Session s = this.getSession();
		try {
			Direccion direccion = (Direccion) s.createQuery("FROM Direccion c where c.id=:id")
					.setParameter("id", idDireccion).uniqueResult();
			return direccion;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Vehiculos
	/**
	 * Devuelve una lista de entity vehiculos
	 */
	@SuppressWarnings("unchecked")
	public List<Vehiculo> obtenerVehiculos() {
		
		Session s = this.getSession();
		try {
			List<Vehiculo> vehiculos = s.createQuery("FROM Vehiculo").list();
			return vehiculos;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	/**
	 * Devuelve un entity vehiculo buscado por id
	 */
	public Vehiculo obtenerVehiculo(int id) {
		
		Session s = this.getSession();
		try {
			Vehiculo vehiculo = (Vehiculo) s.createQuery("FROM Vehiculo v where v.id = :vehiculo")
					.setParameter("vehiculo", id).uniqueResult();
			return vehiculo;

		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Vehiculo de tercero
	/**
	 * Devuelve una lista de entities vehiculos de tercero
	 */
	@SuppressWarnings("unchecked")
	public List<VehiculoTercero> listarVTerceros() {
		
		Session s = this.getSession();
		try {
			List<VehiculoTercero> vehiculos = s.createQuery("FROM VehiculoTercero").list();
			return vehiculos;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	/**
	 * Devuelve una entity vehiculo de tercero buscado por id
	 */
	public VehiculoTercero buscarVehiculoTerceroDTO(int idVehiculoTercero) {
		
		Session s = this.getSession();
		try {
			VehiculoTercero vehiculoTercero = (VehiculoTercero) s.createQuery("FROM VehiculoTercero c where c.id=:id")
					.setParameter("id", idVehiculoTercero).uniqueResult();
			return vehiculoTercero;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	// Plan de Mantenimiento
	/**
	 * Devuelve una lista de entities planes de mantenimiento
	 */
	@SuppressWarnings("unchecked")
	public List<PlanDeMantenimiento> listarPlanesDeMantenimiento() {
		
		Session s = this.getSession();
		try {
			List<PlanDeMantenimiento> planes = s.createQuery("FROM PlanDeMantenimiento").list();
			return planes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Devuelve una entity plan de mantenimiento buscada por id
	 */
	public PlanDeMantenimiento obtenerPlanDeMantenimientoPorId(int idPlanDeMantenimiento) {
		
		Session s = this.getSession();
		try {
			PlanDeMantenimiento plan = (PlanDeMantenimiento) s.createQuery("FROM PlanDeMantenimiento c where c.id=:id")
					.setParameter("id", idPlanDeMantenimiento).uniqueResult();
			return plan;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	//Rutas
	/**
	 * Devuelve una lista de entities rutas
	 */
	@SuppressWarnings("unchecked")
	public List<Ruta> obtenerRutas() {
		
		Session s = this.getSession();
		try {
			List<Ruta> rutas = s.createQuery("FROM Ruta").list();
			return rutas;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	//Trayectos
	/**
	 * Devuelve una lista de entities trayectos
	 */
	@SuppressWarnings("unchecked")
	public List<Trayecto> obtenerTrayectos() {
		
		Session s = this.getSession();
		try {
			List<Trayecto> trayectos = s.createQuery("FROM Trayecto").list();
			return trayectos;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Pedidos
	/**
	 * Devuelve una lista de entities pedidos
	 */
	@SuppressWarnings("unchecked")
	public List<Pedido> obtenerPedidos() {
		
		Session s = this.getSession();
		try {
			List<Pedido> pedidos = s.createQuery("FROM Pedido").list();
			return pedidos;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	/**
	 * Devuelve un entity pedido buscado por id
	 */
	public Pedido buscarPedidoPorId(int idPedido) {
		
		Session s = this.getSession();
		try {
			Pedido pedido = (Pedido) s.createQuery("FROM Pedido c where c.id=:id").setParameter("id", idPedido)
					.uniqueResult();
			return pedido;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Clientes
	/**
	 * Devuelve una lista de entities clientes
	 */
	@SuppressWarnings("unchecked")
	public List<Cliente> obtenerClientes() {
		
		Session s = this.getSession();
		try {
			List<Cliente> clientes = s.createQuery("FROM Cliente").list();
			return clientes;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Cliente Empresa
	/**
	 * Devuelve una lista de entities clientes empresa
	 */
	@SuppressWarnings("unchecked")
	public List<Empresa> obtenerClientesEmpresa() {
		
		Session s = this.getSession();
		try {
			List<Empresa> clientes = s.createQuery("FROM Empresa").list();
			return clientes;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Cliente Particular
	/**
	 * Devuelve una list de entities clientes particulares
	 */
	@SuppressWarnings("unchecked")
	public List<Particular> obtenerClientesParticular() {
		
		Session s = this.getSession();
		try {
			List<Particular> clientes = s.createQuery("FROM Particular").list();
			return clientes;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}

	//Envios
	/**
	 * Devuelve una lista de entities envios
	 */
	@SuppressWarnings("unchecked")
	public List<Envio> obtenerEnvios() {
		
		Session session = this.getSession();
		try {
			List<Envio> envios = session.createQuery("FROM Envio").list();
			return envios;
		} catch (Exception e) {
			System.out.println(e);
		}
		this.closeSession();
		return null;
	}
	
	// Facturas
	/**
	 * Devuelve una lista de entities facturas
	 */
	@SuppressWarnings("unchecked")
	public List<Factura> listarFacturas() {
		
		Session s = this.getSession();
		try {
			List<Factura> facturas = s.createQuery("FROM Factura").list();
			return facturas;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Remitos
	/**
	 * Devuelve una lista de entities remito
	 */
	@SuppressWarnings("unchecked")
	public List<Remito> listarRemitos() {
		
		Session s = this.getSession();
		try {
			List<Remito> remitos = s.createQuery("FROM Remito").list();
			return remitos;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
