package rmi;
import dao.HibernateDAO;
 
 

public class Controlador {

	 private static Controlador instancia;
	 private static HibernateDAO hbtDAO;
	 
	 
	public static Controlador getInstancia() {
		if (instancia == null) {
		 
			instancia = new Controlador();
		}
		return instancia;
	}
	private Controlador()
	{
		super();
	}
}
