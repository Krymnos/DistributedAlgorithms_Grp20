/**
 * 
 */
package week3;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Ron
 *
 */
public class Test3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
		Registry reg1 = null;
//		Registry reg2 = null;
		try {
			reg1 = LocateRegistry.createRegistry(1099);
//			reg2 = LocateRegistry.createRegistry(1098);
			
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		//TODO fill out all edges
		int[][] edges1 = new int[3][2];
		int[][] edges2 = new int[3][2];
		int[][] edges3 = new int[3][2];
		int[][] edges4 = new int[3][2];
		
		//edges[][0] is the name of the other node on that edge 
		//edges[][1] is the weight of that edge
		edges1[0][0] =  ;
		edges1[0][1] =  ;
		
		edges1[1][0] =  ;
		edges1[1][1] =  ;
		
		edges1[2][0] =  ;
		edges1[2][1] =  ;
		
		
		
			System.out.println("Created node "+i+" with edges: ");
			System.out.println("["+edges[0][0]+"] ["+edges[0][1]+"]");
			System.out.println("["+edges[1][0]+"] ["+edges[1][1]+"]");
		
		try {	// delay
			Thread.sleep((long)(5000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (int i = 1; i <= nrNodes; i+=100) {
			try {
				System.out.println("Main: WakeUp "+i);
				Component p = (Component) reg1.lookup("Process" + i);
				p.wakeUp();
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}

	}

}
