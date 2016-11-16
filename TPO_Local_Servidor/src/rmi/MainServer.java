package rmi;

import hbt.HibernateUtil;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dao.HibernateDAO;
import dto.*;
public class MainServer extends RmiStarter {

	public MainServer() {
		super(MainServer.class);
	}
	
	@Override
    public void doCustomRmiHandling() {
		try {
			RemoteObject remoteObject = new RemoteObject();
			/*Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind("//localhost/tpo", remoteObject);
			System.out.println("Fijado en //localhost/tpo");
			*/
			LocateRegistry.createRegistry(1099);
            Naming.rebind ("//localhost/tpo", remoteObject);
            System.out.println("Fijado en //localhost/tpo");

		}
		catch (Exception e) {
			
			System.err.println("Remote Object Exception");
			e.printStackTrace();
		}

    }

    public static void main(String[] args) throws RemoteException {
        new MainServer();
        RemoteObject remoteObject = new RemoteObject();
      //remoteObject.enviar();
//       remoteObject.enviar();
//       ViajeDTO v=new ViajeDTO();
//       v.setIdViaje(2);
//        remoteObject.recibir(v);
        
    }
    
}
