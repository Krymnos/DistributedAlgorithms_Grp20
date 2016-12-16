/**
 * 
 */
package week3;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ron
 *
 */
public class Component extends UnicastRemoteObject implements Component_RMI {
	
	public enum enumSE { maybe_in_MST, in_MST, not_in_MST }
	enumSE[] SE;	//state of each adjacent edge
	int LN;	//level of the current fragment it is part of 
	int FN; //name of the current fragment it is part of 
	enum enumSN {sleeping, find, found}
	enumSN SN; //state of the node (find/found)
	int in_branch; //edge towards core (sense of direction) 
	int test_edge; //edge checked whether other end in same fragment
	int best_edge; //local direction of candidate MOE 
	int best_weight; //weight of current candidate MOE 
	int find_count; //number of report messages expected 
	
	Registry reg;
	private int[][] edges;
	private int id;
	private List<Message> msgQueue;
	
	/**
	 * 
	 * @param edges
	 * @param id
	 * @param n Number of processes
	 * @throws RemoteException
	 */
	protected Component(int[][] edges, int id, int n) throws RemoteException {
		this.id = id;
		this.edges = edges;
		this.SN = enumSN.sleeping;
		this.SE = new enumSE[edges.length];
		for (int j = 0; j < SE.length; j++) {
			SE[j] = enumSE.maybe_in_MST;
		}
		this.reg = LocateRegistry.getRegistry(1099);
		this.msgQueue = new ArrayList<Message>();
	}
	
//	@Override
//	public void run() {
//		try {	//random delay
//			Thread.sleep((long)(Math.random()* 2000));
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}	
//		wakeUp();
//	}

	/**
	 * 
	 */
	protected void wakeUp(){
		int min = 0;
		for (int i = 0; i < edges.length; i++) {
			if(edges[i][1] < edges[min][1]){
				min = i;
				System.out.println("min: "+min);
			}
		}
		int j = min; //adjacent edge with minimum weight
		SE[j] = enumSE.in_MST;
		LN = 0;
		SN = enumSN.found;
		find_count = 0;
		// send(connect;0) on edge j
		try {
			Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[j][0]);
			System.out.println(id+": Send Connect to "+ edges[j][0]);
			p.receiveConnect(LN, id);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * start finding the MOE 
	 * (wave of messages from core outwards)
	 */
	public void sendInitiate(){
		
	}
	public void receiveInitiate(int L, int F, enumSN S, int edge) throws RemoteException{
		int j = findInEdges(edge); //find edge e in local edges array
		System.out.println(id+": Received INITIATE from "+edge);
		LN = L;
		FN = F;
		SN = S;
		in_branch = j;
		best_edge = 0;
		best_weight = 9999;//should be infinite
		checkMsgQueue();
		for (int i = 0; i < SE.length; i++) {
			if(i==j){
				continue;
			}
			if(SE[i].equals(enumSE.in_MST)){
				// send(initiate;L,F,S) on edge i	//propagate initiate in fragment
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[i][0]);
					System.out.println(id+": Send Initiate(L,F,S) to "+ edges[i][0]);
					p.receiveInitiate(L, F, S, id);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				if(S.equals(enumSN.find)){
					find_count++;
				}
			}
		}
		if(S.equals(enumSN.find)){	//and test own edges as potential MOE
			test();
		}
	}
	/**
	 * check an edge for being a candidate MOE 
	 */
	public void test(){
		boolean t = false; //are there any edges in state maybe_in_MST
		int min = 0;
		for (int i = 0; i < edges.length; i++) {
			if(SE[i].equals(enumSE.maybe_in_MST)){
				t = true;
				if (edges[i][1] < edges[min][1]) {
					min = i;
				}}
		}
		if(t){
			// test-edge := edge in state ?_in_MST of minimum weight 
			test_edge = min;
			// send(test;LN,FN) on test-edge
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[min][0]);
				System.out.println(id+": Send Test(LN, FN) to "+ edges[min][0]);
				p.receiveTest(LN, FN, id);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		} else{
			test_edge = 0;
			report();
		}
	}
	public void receiveTest(int L, int F, int edge) throws RemoteException{
		int j = findInEdges(edge);
		System.out.println(id+": Received Test from "+edge);
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wakeup at first message
		} 
		if(L < LN){	//level too high
			// append message to queue
			msgQueue.add(new Message(Message.Type.TEST, L , edge,  F, 0));
		} else{
			if(F != FN){	//other fragment
				// send accept on edge j	
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[j][0]);
					System.out.println(id+": Send Accept to "+ edges[j][0]);
					p.receiveAccept(id);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			} else{			//same fragment
				if(SE[j].equals(enumSE.maybe_in_MST)){
					SE[j] = enumSE.not_in_MST;
					checkMsgQueue();
				}
				if(test_edge != j){	//optimization to avoid superfluous rejects
					// send(reject) on edge j
					try {
						Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
						System.out.println(id+": Send Reject to "+ edge);
						p.receiveReject(id);
					} catch (RemoteException | NotBoundException e) {
						e.printStackTrace();
					}
				}else{
					test();
				}
			}
		}
	}
	/**
	 * positive answer to test message
	 */
	public void receiveAccept(int edge) throws RemoteException{
		int j = findInEdges(edge);
		System.out.println(id+": Received Accept from "+edge);
		test_edge = 0;
		if(edges[j][1] < best_weight){
			best_edge = j;
			best_weight = edges[j][1];
		}
		report();
		
	}
	/**
	 * negative answer to test message
	 */
	public void receiveReject(int edge) throws RemoteException{
		int j = findInEdges(edge);
		System.out.println(id+": Received REJECT from "+edge);
		if(SE[j].equals(enumSE.maybe_in_MST)){
			SE[j] = enumSE.not_in_MST;
		}
		test();	//try the next local edge
	}
	/**
	 * report on candidate MOE found 
	 * (wave of messages towards core) 
	 */
	public void report(){
		if(find_count == 0 && test_edge == 0){
			SN = enumSN.found;
			checkMsgQueue();
			// send(report;best-wt) on in-branch
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[in_branch][0]);
				System.out.println(id+": Send Report to "+ edges[in_branch][0]);
				p.receiveReport(best_weight, id);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			// report best edge towards core 
		}
	}
	public void receiveReport(int w, int edge) throws RemoteException{
		int j = findInEdges(edge);
		System.out.println(id+": Received Report from "+edge);
		if(j != in_branch){
			find_count--;
			if(w < best_weight){
				best_weight = w;
				best_edge = j;
			}
			report();
		} else{
			if(SN.equals(enumSN.find)){
				// queue message
				msgQueue.add(new Message(Message.Type.Report, 0 , edge,  0, w));
			}
			else{
				if(w > best_weight){
					changeRoot();
				} else{
					if(w >= 99999 && w == best_weight ){
						//TODO HALT
						System.out.println(id+": HALT");
						return;
					}
				}
			}
		}
	}
	/**
	 * modify sense of direction towards new core
	 */
	public void changeRoot(){
		if(SE[best_edge].equals(enumSE.in_MST)){
			// send(change-root) on best edge
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[best_edge][0]);
				System.out.println(id+": Send Report to "+ edges[best_edge][0]);
				p.receiveChangeRoot(id);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		} else{
			// send(connect;LN) on best-edge
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[best_edge][0]);
				System.out.println(id+": Send Report to "+ edges[best_edge][0]);
				p.receiveConnect(LN, id);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			SE[best_edge] = enumSE.in_MST;
			checkMsgQueue();
		}
	}
	public void receiveChangeRoot(int edge) throws RemoteException{
		System.out.println(id+": Received ChangeRoot from "+edge);
		changeRoot();
	}
	/**
	 * requesting the connection of two fragments
	 */
	public void sendConnect(){
		
	}
	public void receiveConnect(int L, int edge) throws RemoteException{
		int j = findInEdges(edge);
		System.out.println(id+": Received Connect from "+edge);
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wake up at first message
		}		
		if(L < LN){	//absorb lower-level fragment
			System.out.println(id+": ABSORB "+edge);
			SE[j] = enumSE.in_MST;
			checkMsgQueue();
			//send(initiate;LN,FN,SN) on edge j 
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
				System.out.println(id+": Send Initiate to "+ edge);
				p.receiveInitiate(LN, FN, SN, id);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			if(SN == enumSN.find){
				find_count++;
			}
		} else{
			if(SE[j] == enumSE.maybe_in_MST){	//connection cannot be made yet
				System.out.println(id+": Queued initiate message from "+edge);
				// append message to queue
				msgQueue.add(new Message(Message.Type.Connect, L , edge,  0, 0));
			} else{
				// send(initiate,LN+1,w(j),find) on edge j
				//merge with fragment of same level; edge j new core; start initiate
				System.out.println(id+": MERGE with "+edge);
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
					System.out.println(id+": Send INITIATE(LN+1,w(j),find) to "+ edge);
					p.receiveInitiate(LN+1, edges[j][1], enumSN.find, id);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void checkMsgQueue(){
		for (int i = 0; i < msgQueue.size(); i++) {
			Message m = msgQueue.remove(0);
			switch (m.t) {
			case Connect:
				System.out.println(id+": Deliver Connect message from msgQueue");
				try {
					this.receiveConnect(m.L, m.edge);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case Report:
				System.out.println(id+": Deliver Report message from msgQueue");
				try {
					this.receiveReport(m.w, m.edge);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case TEST:
				System.out.println(id+": Deliver Test message from msgQueue");
				try {
					this.receiveTest(m.L, m.F, m.edge);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
	
			default:
				break;
			}
		}
	}
	/**
	 * find edge e in local edges array
	 * @param e
	 * @return
	 */
	private int findInEdges(int e){
		for (int i = 0; i < edges.length; i++) {
			if(edges[i][0] == e){
				return i;
			}
		}
		return -1;
	}
}
