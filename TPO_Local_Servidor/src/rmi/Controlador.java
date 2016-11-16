package rmi;
import java.rmi.RemoteException;
import java.util.List;

import dao.HibernateDAO;
import dto.*;
 
 

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
	
	public void controlarPlanMantenimiento() throws RemoteException {
		List<VehiculoDTO> vehiculosDTO= hbtDAO.obtenerVehiculos();
		RemoteObject rmO=new RemoteObject();
		for(VehiculoDTO v: vehiculosDTO)
		{
			if(v.getEstado().equals("En Deposito"))
			{
				rmO.ControlarVehiculo(v);
			}
		}
		
	}
}
