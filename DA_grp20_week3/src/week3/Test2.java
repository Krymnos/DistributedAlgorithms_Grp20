/**
 * 
 */
package week3;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Ron
 *
 */
public class Test2 {
	
	/*
	 * Not working!!!
	 * 
	 * Not working!!!
	 * 
	 * Not working!!!
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
		Registry reg1 = null;
		Registry reg2 = null;
		try {
			reg1 = LocateRegistry.createRegistry(1099);
			reg2 = LocateRegistry.createRegistry(1098);
			
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		/*
		 * large example 
		 * 3 edges per node
		 */
		int nrNodes = 4;
		Random rnd = new Random();
		List<Integer> weights = new ArrayList<Integer>();
		List<Integer> nodes = new ArrayList<Integer>();
		List<Integer> nodes2 = new ArrayList<Integer>();
		List<Edge> edgeList = new ArrayList<Edge>();
		for (int i = 1; i <= nrNodes*1.5; i++) { 
			weights.add(i);
			if(i<=nrNodes){
				nodes.add(i);
				nodes2.add(i);
			} else{
				nodes.add(i-nrNodes);
				nodes2.add(i-nrNodes);
			}
		}
		//output for debugging
		StringBuilder sb = new StringBuilder();
		for (Integer s : weights)
		{
		    sb.append(s);
		    sb.append("\t");
		}
		System.out.println(sb.toString());
		sb = new StringBuilder();
		for (Integer s : nodes)
		{
		    sb.append(s);
		    sb.append("\t");
		}
		System.out.println(sb.toString());
		
		
		for (int i = 1; i <= nrNodes*1.5; i++) { 
			int r = rnd.nextInt(weights.size());
			int r2 = rnd.nextInt(nodes.size());
			int r3 = rnd.nextInt(nodes.size());
			while(nodes.get(r2).equals(nodes2.get(r3))){
				r2 = rnd.nextInt(nodes.size());
			}
			
			Edge e = new Edge(nodes.remove(r2), nodes2.remove(r3), weights.remove(r));
			edgeList.add(e);
			
		}

		for (int i = 0; i < edgeList.size(); i++) {
			System.out.println(edgeList.get(i).toString());
		}
		
		for (int i = 1; i <= nrNodes; i++) {//create a set of edges for each node
			int[][] edges = new int[3][2];
			List<Edge> temp = new ArrayList<Edge>();
			Iterator<Edge> it = edgeList.iterator();
			for (int j = 0; j < temp.size(); j++) {
				Edge e = (Edge) it.next();
				System.out.println("edge e: "+e);
				if(e.node1 == i || e.node2 == i){
					temp.add(e);
				}
			}
			System.out.println("Temp size: "+temp.size());
			for (int j = 0; j < temp.size(); j++) {
				Edge e = temp.remove(0);
				
				if(e.node1 == i){
					edges[j][0] = e.node2;
					edges[j][1] = e.weight;
				} else{
					edges[j][0] = e.node1;
					edges[j][1] = e.weight;
				}
			}
			
			
//			*/
			try {
				Component p = new Component(edges, i, nrNodes);
				Thread t = new Thread(p);
				String name = "Process" + i;
				reg1.bind(name, p);
//				if(i < 5){
//					reg1.bind(name, p);
//				} else{
//					reg2.bind(name, p);
//				}
				t.start();
				System.out.println(i+" ready");
			} catch (RemoteException | AlreadyBoundException e) {
				e.printStackTrace();
			}
			System.out.println("Created node "+i+" with edges: ");
			System.out.println("["+edges[0][0]+"] ["+edges[0][1]+"]");
			System.out.println("["+edges[1][0]+"] ["+edges[1][1]+"]");
			System.out.println("["+edges[2][0]+"] ["+edges[2][1]+"]");
		}
		for (int i = 1; i <= nrNodes; i+=8) {
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
