/**
 * 
 */
package week2;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Ron
 *
 */
public class Component implements Component_RMI, Runnable, Serializable {
	private int i;	//id
	private int[] N;	
	private char[] S;
	private Token t;
	private static int port = 2000;
	private Registry reg = null;
	
	public Component(int size, int id){
		this.N = new int[size];
		this.S = new char[size];
		this.i = id;
		
		
		//initialize state arrays
		if(id == 0){
			this.S[0] = 'H'; //holding the token
			this.t= new Token(size);
			for (int i = 1; i < N.length; i++) {
				this.S[i] = 'O';
			}
		} else{
			for (int i = 0; i < id; i++) {
				this.S[i] = 'R'; //previous ones may have token
			}
			for (int i = id; i < N.length; i++) {
				this.S[i] = 'O'; //previous ones may have token
			}
		}
	}
	/**
	 * Send request to those that may have the token
	 */
	public void request(){
		if(S[i] == 'H')
			return;
		S[i] = 'R';	//set own state to requesting
		N[i]++;		//increment request number
		for (int j = 0; j < this.i; j++) {
			if(S[j] == 'R'){
				sendRequestTo(j);
			}
		}
		for (int j = this.i+1; j < N.length; j++) {	//leave out this.i
			if(S[j] == 'R'){
				sendRequestTo(j);
			}
		}
	}
	
	private void sendRequestTo(int j){
		//System.out.println(i+": Send Request To: "+ j);
		try {	
			Registry r = LocateRegistry.getRegistry(port+j);
			Component p = (Component) r.lookup("Process" + j);
			p.receiveReq(i, N[i]);	//TODO stackoverflow
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	private void sendTokenTo(int j){
		System.out.println(i+": "+this.t.toString());
		System.out.println(i+": Send Token To: " + j);
		try {	
			Component p = (Component) reg.lookup("Process" + j);
			Token nt = this.t.deepClone();
			this.t = null;
			p.receiveToken(nt);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	public void receiveReq(int j, int r){
		N[j] = r;
		switch (S[i]) {
		case 'O':
			S[j] = 'R';			
			break;
		case 'E':
			S[j] = 'R';	
			break;
		case 'R':
			if(S[j] != 'R')
				S[j] = 'R';	
				sendRequestTo(j); //TODO stackoverflow
			break;
		case 'H':
			S[j] = 'R';
			S[i] = 'O';
			this.t.TS[j] = 'R';
			this.t.TN[j] = r;
			sendTokenTo(j);
			break;
		}
	}
	public void receiveToken(Token t){
		this.t= t;
		S[i] = 'E';
		/* == start critical section == */
		try {	//random delay
			Thread.sleep((long)(Math.random() * 4000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		/* == end critical section == */
		S[i] = 'O';
		t.TS[i] = 'O';
		for (int j = 0; j < N.length; j++) {//update info on other processes
			if(N[j] > t.TN[j]){	//local process more up-to-date than token
				t.TN[j] = N[j];
				t.TS[j] = S[j];
			} else{
				N[j] = t.TN[j];
				S[j] = t.TS[j];
			}
		}
		System.out.println(i+": "+this.t.toString());
		for (int j = 0; j < N.length; j++) {
			if(S[j] == 'R'){
				sendTokenTo(j);
			} else{		//nobody requesting
				S[i] = 'H';
			}
		}
	}
	
	@Override
	public String toString(){
		String s ="State array of "+this.i+": (";
		for (int i = 0; i < N.length; i++) {
			s += this.S[i]+" ";
		}
		return s+")";
		
	}
	@Override
	public void run() {
		try {
			reg = LocateRegistry.createRegistry(port+i);
			String name = "Process" + i;
			reg.bind(name, this);
			System.err.println(name + " is ready");
			System.out.println(this.toString());
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		
		try {	//random delay
			Thread.sleep((long)(Math.random() * 9000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		request();
		
	}
}
