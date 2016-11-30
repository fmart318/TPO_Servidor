package rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class MainServer extends RmiStarter {

	public MainServer() {
		super(MainServer.class);
	}

	@Override
	public void doCustomRmiHandling() {
		try {
			RemoteObject remoteObject = new RemoteObject();
			LocateRegistry.createRegistry(1099);
			Naming.rebind("//localhost/tpo", remoteObject);
			System.out.println("Fijado en //localhost/tpo");
		} catch (Exception e) {
			System.err.println("Remote Object Exception");
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws RemoteException {
		new MainServer();
		RemoteObject remoteObject = new RemoteObject();
	}

}
