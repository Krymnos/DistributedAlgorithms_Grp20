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
	private static int n = 4;
	
	public static void main(String argv[]) {
		// add when using multiple machines
		// System.setSecurityManager(new RMISecurityManager());

		
		for (int i = 0; i < n; i++) { // create processes
			try {
				Thread process = new Thread(new Component(n, i));
				process.start();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//System.exit(0);
	}
	public Registry getRegistry(){
		return registry;
	}

}
