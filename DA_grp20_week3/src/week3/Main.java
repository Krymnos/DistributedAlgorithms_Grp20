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
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.setProperty("java.rmi.server.hostname", "145.94.195.202");
		Registry reg1 = null;
//		Registry reg2 = null;
		try {
			reg1 = LocateRegistry.createRegistry(1099);
//			reg2 = LocateRegistry.createRegistry(1098);
			
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		int nrNodes = 50;
		int[] w = new int[nrNodes];
		/*
		 * example 1
		 */
//		int[] w = {1, 5, 3, 7, 2, 6, 4};
		/*
		 * example 2 (from slides)
		 * circle with weights in increasing order
		 */
		for (int i = 0; i < w.length; i++) {
			w[i] = i+1;
		}
//		/*
		
		for (int i = 1; i <= nrNodes; i++) {//create a set of edges for each node
			int[][] edges = new int[2][2];
			if(i == 1){
				edges[0][0] = nrNodes;
				edges[0][1] = nrNodes;
			} else{
				edges[0][0] = i-1;
				edges[0][1] = w[i-2];
			}
			if(i == nrNodes){
				edges[1][0] = 1;
				edges[1][1] = i;
			} else{
				edges[1][0] = i+1;
				edges[1][1] = w[i-1];
			}
//			*/
			try {
				Component p = new Component(edges, i, nrNodes);
				Thread t = new Thread(p);
				String name = "Process" + i;
//				reg1.bind(name, p);
				if(i <= nrNodes/2){
					reg1.bind(name, p);
				} else{
//					reg1.bind(name, p);
				}
				t.start();
				System.out.println(i+" ready");
			} catch (RemoteException | AlreadyBoundException e) {
				e.printStackTrace();
			}
			System.out.println("Created node "+i+" with edges: ");
			System.out.println("["+edges[0][0]+"] ["+edges[0][1]+"]");
			System.out.println("["+edges[1][0]+"] ["+edges[1][1]+"]");
		}
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
