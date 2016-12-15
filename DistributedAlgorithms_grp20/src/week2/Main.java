package week2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;

import week1.DAGrp20;

/**
 * @author Ron
 *
 */
public class Main {
	private static Registry registry = null;
	private static int n;
	
	public static void main(String argv[]) {
		// add when using multiple machines
		// System.setSecurityManager(new RMISecurityManager());

		
		try {
			Component process = new Component(Integer.parseInt(argv[1]), Integer.parseInt(argv[0]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//System.exit(0);
	}

}
