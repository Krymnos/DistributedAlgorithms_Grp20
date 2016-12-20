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

import week3.Message.Type;

/**
 * @author Ron
 *
 */
public class Component extends UnicastRemoteObject implements Component_RMI, Runnable {
	
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
	
	@Override
	public void run() {
		while(true){
			try {	// delay
				Thread.sleep((long)(500));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}	
			checkMsgQueue();
		}
	}

	/**
	 * 
	 */
	protected void wakeUp(){
		if(!this.SN.equals(enumSN.sleeping)){
			return;}
		int min = 0;
		for (int i = 0; i < edges.length; i++) {
			if(edges[i][1] < edges[min][1]){
				min = i;
				System.out.println("min: "+min);
			}
		}
		int j = min; //adjacent edge with minimum weight
		SE[j] = enumSE.in_MST;
		System.out.println(id+": (WakeUp) Include edge "+edges[j][1]+" in MST");
		LN = 0;
		SN = enumSN.found;
		find_count = 0;
		// send(connect;0) on edge j
		try {
			Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[j][0]);
			System.out.println(id+": Send Connect to "+ edges[j][0]);
			
			p.receive(Type.Connect, id, null, -1, -1, LN);
			
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * start finding the MOE 
	 * (wave of messages from core outwards)
	 */
	public void receiveInitiate(int L, int F, enumSN S, int edge){
		int j = findInEdges(edge); //find edge e in local edges array
		System.out.println(id+": Received INITIATE from "+edge);
		LN = L;
		FN = F;
		System.out.println(id+": FN := "+FN);
		SN = S;
		in_branch = j;
		System.out.println(id+": in-branch := "+edges[j][1]);
		best_edge = -1;
		best_weight = 9999;//should be infinite
		for (int i = 0; i < edges.length; i++) {
			if(i==j){
				continue;
			}
			if(SE[i].equals(enumSE.in_MST)){
				// send(initiate;L,F,S) on edge i	//propagate initiate in fragment
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[i][0]);
					System.out.println(id+": Send Initiate(L,F,S) to "+ edges[i][0]);
					p.receive(Type.Initiate, id, S, -1, F, L);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				if(S.equals(enumSN.find)){
					find_count++;
				}
			}
		}
		System.out.println(id+": LN after initiate "+LN);
		if(S.equals(enumSN.find)){	//and test own edges as potential MOE
			test();
		}
	}
	/**
	 * check an edge for being a candidate MOE 
	 */
	public void test(){
		boolean t = false; //are there any edges in state maybe_in_MST
		int min = -1;
		for (int i = 0; i < edges.length; i++) {
			if(SE[i].equals(enumSE.maybe_in_MST)){
				t = true;
				min = i;
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
				System.out.println(id+": Send Test(LN = "+LN+", FN) to "+ edges[min][0]);
				p.receive(Type.TEST, id, null, -1, FN, LN); 
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		} else{
			test_edge = -1;
			report();
		}
	}
	public void receiveTest(int L, int F, int edge){
		int j = findInEdges(edge);
		System.out.println(id+": Received Test from "+edge);
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wakeup at first message
		} 
		if(L < LN){	//level too high
			// append message to queue
			msgQueue.add(new Message(Type.TEST, edge , null, -1, F, L));
		} else{
			if(F != FN){	//other fragment
				// send accept on edge j	
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[j][0]);
					System.out.println(id+": Send Accept to "+ edges[j][0]);
					p.receive(Type.Accept, id, null, -1, -1, -1);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			} else{			//same fragment
				System.out.println(id+": Same fragment = "+F);
				if(SE[j].equals(enumSE.maybe_in_MST)){
					SE[j] = enumSE.not_in_MST;
					System.out.println(id+": Edge "+edges[j][1]+" is not in MST");
					
				}
				if(test_edge != j){	//optimization to avoid superfluous rejects
					// send(reject) on edge j
					try {
						Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
						System.out.println(id+": Send Reject to "+ edge);
						p.receive(Type.Reject, id, null, -1, -1, -1);
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
	public void receiveAccept(int edge){
		int j = findInEdges(edge);
		System.out.println(id+": Received Accept from "+edge);
		test_edge = -1;
		if(edges[j][1] < best_weight){
			best_edge = j;
			best_weight = edges[j][1];
			System.out.println(id+": Best edge := "+edges[j][1]);
		}
		report();
		
	}
	/**
	 * negative answer to test message
	 */
	public void receiveReject(int edge) {
		int j = findInEdges(edge);
		System.out.println(id+": Received REJECT from "+edge);
		if(SE[j].equals(enumSE.maybe_in_MST)){
			SE[j] = enumSE.not_in_MST;
			System.out.println(id+": Edge "+edges[j][1]+" is not in MST");
		}
		test();	//try the next local edge
	}
	/**
	 * report on candidate MOE found 
	 * (wave of messages towards core) 
	 */
	public void report(){
		if(find_count == 0 && test_edge == -1){
			SN = enumSN.found;
			// send(report;best-wt) on in-branch
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[in_branch][0]);
				System.out.println(id+": Send Report to "+ edges[in_branch][0]);
				p.receive(Type.Report, id, null, best_weight, -1, -1); 
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			// report best edge towards core 
		}
	}
	public void receiveReport(int w, int edge){
		int j = findInEdges(edge);
		System.out.println(id+": Received Report(w="+w+" from "+edge);
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
				msgQueue.add(new Message(Type.Report, edge , null, w, -1, -1));
			}
			else{
				if(w > best_weight){
					changeRoot();
				} else{
					if(w >= 999 && w == best_weight ){
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
				System.out.println(id+": Send change-root to "+ edges[best_edge][0]);
				p.receive(Type.ChangeRoot, id, null, -1, -1, -1);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		} else{
			// send(connect;LN) on best-edge
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edges[best_edge][0]);
				System.out.println(id+": Send Connect to "+ edges[best_edge][0]);
				p.receive(Type.Connect, id, null, -1, -1, LN);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			SE[best_edge] = enumSE.in_MST;
			System.out.println(id+": (changeRoot) Include edge "+edges[best_edge][1]+" in MST");
			
		}
	}
	public void receiveChangeRoot(int edge) {
		System.out.println(id+": Received ChangeRoot from "+edge);
		changeRoot();
	}
	/**
	 * requesting the connection of two fragments
	 */
	public void sendConnect(){
		
	}
	public void receiveConnect(int L, int edge) {
		int j = findInEdges(edge);
		System.out.println(id+": Received Connect from "+edge);
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wake up at first message
		}		
		if(L < LN){	//absorb lower-level fragment
			System.out.println(id+": ABSORB "+edge+" L: "+L+" LN: "+LN);
			SE[j] = enumSE.in_MST;
			System.out.println(id+": (receiveConnect) Include edge "+edges[j][1]+" in MST");
			
			//send(initiate;LN,FN,SN) on edge j 
			try {
				Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
				System.out.println(id+": Send Initiate to "+ edge);
				p.receive(Type.Initiate, id, SN, -1, FN, LN);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			if(SN == enumSN.find){
				find_count++;
			}
		} else{
			if(SE[j] == enumSE.maybe_in_MST){	//connection cannot be made yet
				System.out.println(id+": Queued connect message from "+edge);
				// append message to queue
				msgQueue.add(new Message(Type.Connect, edge , null, -1, -1, L));
			} else{
				// send(initiate,LN+1,w(j),find) on edge j
				//merge with fragment of same level; edge j new core; start initiate
				System.out.println(id+": MERGE with "+edge);
//				FN = edges[j][1];
//				in_branch = j;
//				LN++;
				try {
					Component_RMI p = (Component_RMI) reg.lookup("Process" + edge);
					System.out.println(id+": Send INITIATE(LN+1,w(j),find) to "+ edge);
					System.out.println(id+": LN before initiate "+LN);
					p.receive(Type.Initiate, id, enumSN.find, -1, edges[j][1], LN+1);
					
//					
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void receive(Type t, int edge, enumSN S, int w, int F, int L ) throws RemoteException{
		//  add new Message ( type, level, edge, F, weight)
		msgQueue.add(new Message(t, edge , S, w, F, L));
	}
	
	
	private void checkMsgQueue(){
		for (int i = 0; i < msgQueue.size(); i++) {
			Message m = msgQueue.remove(0);
			switch (m.t) {
			case Connect:
				System.out.println(id+": Deliver Connect message from msgQueue");
				this.receiveConnect(m.L, m.edge);
				break;
			case Report:
				System.out.println(id+": Deliver Report message from msgQueue");
				this.receiveReport(m.w, m.edge);
				break;
			case TEST:
				System.out.println(id+": Deliver Test message from msgQueue");
				this.receiveTest(m.L, m.F, m.edge);
				break;
			case Initiate:
				System.out.println(id+": Deliver Initiate message from msgQueue");
				this.receiveInitiate(m.L, m.F, m.s, m.edge);
				break;
			case Accept:
				System.out.println(id+": Deliver Accept message from msgQueue");
				this.receiveAccept(m.edge); 
				break;
			case ChangeRoot:
				System.out.println(id+": Deliver ChangeRoot message from msgQueue");
				this.receiveChangeRoot(m.edge);
				break;
			case Reject:
				System.out.println(id+": Deliver Reject message from msgQueue");
				this.receiveReject(m.edge);
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
