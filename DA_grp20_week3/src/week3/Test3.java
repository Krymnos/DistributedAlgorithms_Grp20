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
		int nrNodes = 4;
		//TODO fill out all edges
		int[][] edges1 = new int[3][2];
		int[][] edges2 = new int[3][2];
		int[][] edges3 = new int[3][2];
		int[][] edges4 = new int[3][2];
		
		//edges[][0] is the name of the other node on that edge 
		//edges[][1] is the weight of that edge
		edges1[0][0] =  2;
		edges1[0][1] =  3;
		
		edges1[1][0] =  3;
		edges1[1][1] =  2;
		
		edges1[2][0] =  4;
		edges1[2][1] =  1;
		
		edges2[0][0] =  1;
		edges2[0][1] =  3;
		
		edges2[1][0] =  3;
		edges2[1][1] =  4;
		
		edges2[2][0] =  4;
		edges2[2][1] =  6;
		
		edges3[0][0] =  1;
		edges3[0][1] =  2;
		
		edges3[1][0] =  2;
		edges3[1][1] =  4;
		
		edges3[2][0] =  4;
		edges3[2][1] =  5;
		
		edges4[0][0] =  1;
		edges4[0][1] =  1;
		
		edges4[1][0] =  2;
		edges4[1][1] =  6;
		
		edges4[2][0] =  3;
		edges4[2][1] =  5;
		
		try {
			Component p = new Component(edges1, 1, nrNodes);
			Thread t = new Thread(p);
			String name = "Process" + 1;
			
			Component p2 = new Component(edges2, 2, nrNodes);
			Thread t2 = new Thread(p2);
			String name2 = "Process" + 2;
			
			Component p3 = new Component(edges3, 3, nrNodes);
			Thread t3 = new Thread(p3);
			String name3 = "Process" + 3;
			
			Component p4 = new Component(edges4, 4, nrNodes);
			Thread t4 = new Thread(p4);
			String name4 = "Process" + 4;
			
			reg1.bind(name, p);
			reg1.bind(name2, p2);
			reg1.bind(name3, p3);
			reg1.bind(name4, p4);
//			if(i <= nrNodes/2){
////				reg1.bind(name, p);
//			} else{
//				reg1.bind(name, p);
//			}
			t.start();
			t2.start();
			t3.start();
			t4.start();
			
			//System.out.println(i+" ready");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		
			//System.out.println("Created node "+i+" with edges: ");
			System.out.println("["+edges1[0][0]+"] ["+edges1[0][1]+"]");
			System.out.println("["+edges1[1][0]+"] ["+edges1[1][1]+"]");
			System.out.println("["+edges1[2][0]+"] ["+edges1[2][1]+"]");
		try {	// delay
			Thread.sleep((long)(5000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (int i = 1; i <= nrNodes; i+=100) {
			try {
				System.out.println("Main: WakeUp "+i);
				Component p = (Component) reg1.lookup("Process" + nrNodes);
				p.wakeUp();
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}

	}

}
