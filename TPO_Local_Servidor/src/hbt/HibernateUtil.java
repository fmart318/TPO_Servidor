package hbt;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import entities.*;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;
	
	static {
		
		try {
			AnnotationConfiguration config = new AnnotationConfiguration();
			config.addAnnotatedClass(Carga.class);
			config.addAnnotatedClass(Cliente.class);
			config.addAnnotatedClass(Direccion.class);
			config.addAnnotatedClass(Empresa.class);
			config.addAnnotatedClass(Envio.class);
			config.addAnnotatedClass(Factura.class);
			config.addAnnotatedClass(Particular.class);
			config.addAnnotatedClass(Pedido.class);
			config.addAnnotatedClass(PlanDeMantenimiento.class);
			config.addAnnotatedClass(VehiculoTercero.class);
			config.addAnnotatedClass(Remito.class);
			config.addAnnotatedClass(Ruta.class);
			config.addAnnotatedClass(Sucursal.class);
			config.addAnnotatedClass(Trayecto.class);
			config.addAnnotatedClass(Vehiculo.class);
			sessionFactory = config.buildSessionFactory();
		}
		
		catch(Throwable e) {
			System.err.println("Initial SessionFactory creation failed." + e);
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static Session getSession() throws HibernateException {
		return sessionFactory.openSession();
	}
	
	public static SessionFactory getSessionfactory() {
		return sessionFactory;
	}
}
