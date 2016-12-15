/**
 * 
 */
package week3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
	
	private int[][] edges;
	
	protected Component(int[][] edges) throws RemoteException {
		this.edges = edges;
		this.SN = enumSN.sleeping;
		this.SE = new enumSE[edges.length];
		for (int i = 0; i < edges.length; i++) {
			SE[i] = enumSE.maybe_in_MST;
		}
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
	 * @param args
	 */
	public static void main(String[] args) {

	}
	/**
	 * 
	 */
	protected void wakeUp(){
		int min = 0;
		for (int i = 1; i < edges.length; i++) {
			if(edges[i][1] < edges[min][1]){
				min = i;
				System.out.println("min: "+min);
			}
		}
		int j = min;
		SE[j] = enumSE.in_MST;
		LN = 0;
		SN = enumSN.found;
		find_count = 0;
		//TODO send(connect;0) on edge j
	}
	/**
	 * start finding the MOE 
	 * (wave of messages from core outwards)
	 */
	public void sendInitiate(){
		
	}
	public void receiveInitiate(int L, int F, enumSN S, int j) throws RemoteException{
		LN = L;
		FN = F;
		SN = S;
		in_branch = j;
		best_edge = 0;
		best_weight = 99999;//should be infinite
		for (int i = 0; i < SE.length; i++) {
			if(i==j){
				continue;
			}
			if(SE[i].equals(enumSE.in_MST)){
				//TODO send(initiate;L,F,S) on edge i	//propagate initiate in fragment
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
		for (int i = 0; i < SE.length; i++) {
			if(SE[i].equals(enumSE.maybe_in_MST))
				t = true;
				break;
		}
		if(t){
			//TODO test-edge := edge in state ?_in_MST of minimum weight 
			//TODO send(test;LN,FN) on test-edge
		} else{
			test_edge = 0;
			report();
		}
	}
	public void receiveTest(int L, int F, int j) throws RemoteException{
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wakeup at first message
		} 
		if(L < LN){	//level too high
			//TODO append message to queue
		} else{
			if(F != FN){	//other fragment
				//TODO send accept on edge j	
			} else{			//same fragment
				if(SE[j].equals(enumSE.maybe_in_MST)){
					SE[j] = enumSE.not_in_MST;
				}
				if(test_edge != j){	//optimization to avoid superfluous rejects
					//TODO send(reject) on edge j
				}else{
					test();
				}
			}
		}
	}
	/**
	 * positive answer to test message
	 */
	public void sendAccept(){
		
	}
	public void receiveAccept(int j) throws RemoteException{
		test_edge = 0;
		/* TODO add w()
		if(w(j) < best_weight){
			best_edge = j;
			best_weight = w(j);
		}
		report();
		*/
	}
	/**
	 * negative answer to test message
	 */
	public void sendReject(){
		
	}
	public void receiveReject(int j) throws RemoteException{
		if(SE[j].equals(enumSE.maybe_in_MST)){
			SE[j] = enumSE.not_in_MST;
		}
		test();
	}
	/**
	 * report on candidate MOE found 
	 * (wave of messages towards core) 
	 */
	public void report(){
		if(find_count == 0 && test_edge == 0){
			SN = enumSN.found;
			//TODO send(report;best-wt) on in-branch
			// report best edge towards core 
		}
	}
	public void receiveReport(int w, int j) throws RemoteException{
		if(j != in_branch){
			find_count--;
			if(w < best_weight){
				best_weight = w;
				best_edge = j;
			}
			report();
		} else{
			if(SN.equals(enumSN.find)){
				//TODO queue message
			}
			else{
				if(w > best_weight){
					changeRoot();
				} else{
					if(w >= 99999 && w == best_weight ){
						//TODO HALT
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
			//TODO send(change-root) on best edge
		} else{
			//TODO send(connect;LN) on best-edge
			SE[best_edge] = enumSE.in_MST;
		}
	}
	public void receiveChangeRoot(int j) throws RemoteException{
		changeRoot();
	}
	/**
	 * requesting the connection of two fragments
	 */
	public void sendConnect(){
		
	}
	public void receiveConnect(int L, int j) throws RemoteException{
		if(this.SN == enumSN.sleeping){
			wakeUp();	// wakeup at first message
		}
		if(L < LN){	//absorb lower-level fragment
			SE[j] = enumSE.in_MST;
			//TODO send(initiate;LN,FN,SN) on edge j  
			if(SN == enumSN.find){
				find_count++;
			}
		} else{
			if(SE[j] == enumSE.maybe_in_MST){	//connection cannot be made yet
				//TODO append message to queue
			} else{
				//TODO send(initiate,LN+1,w(j),find) on edge j
					//merge with fragment of same level; edge j new core; start initiate
			}
		}
	}

}
