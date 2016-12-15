/**
 * 
 */
package week2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Ron
 *
 */
public class Component extends UnicastRemoteObject implements Component_RMI {
	private int i;	//id
	private int[] N;	
	private char[] S;
	private Token t;
	private static int port = 2000;
	private Registry reg = null;
	private int size;
	
	/**
	 * 
	 * @param size
	 * @param id
	 */
	public Component(int size, int id) throws RemoteException{
		this.N = new int[size];
		this.S = new char[size];
		this.i = id;
		this.size = size;
		
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
		try {
			System.setProperty("java.rmi.server.hostname", "145.94.192.78");
			reg = LocateRegistry.createRegistry(port); //TODO hostname for use across machines
			String name = "Process" + i;
			reg.bind(name, this);
			System.err.println(name + " is ready");
			System.out.println(this.toString());
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		try {	//random delay
			Thread.sleep((long)(10000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}	
		request();
		
		try {	//random delay
			Thread.sleep((long)(10000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}	
		request();
		try {	//random delay
			Thread.sleep((long)(20000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}	
		System.exit(0);
		
	}
	
	/**
	 * Send request to those that may have the token
	 */
	public void request(){
		if(S[i] == 'H')
			return; 		//sendRequestTo(i, N[i]);
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
		System.out.println("");
		System.out.println(i+": Send Request To: "+ j);	
		try {	
			Registry r = LocateRegistry.getRegistry("145.94.215.68", port);
			Component_RMI p = (Component_RMI) r.lookup("Process" + j);
			p.receiveReq(i, N[i]);	//TODO stackoverflow
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	private void sendTokenTo(int j){
		System.out.println(i+": Send Token To: " + j);
		try {
			Registry r = LocateRegistry.getRegistry("145.94.215.68", port);
			Component_RMI p = (Component_RMI) r.lookup("Process" + j);
			Token nt = deepClone(this.t);
			this.t = null;
			p.receiveToken(nt);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	public void receiveReq(int j, int r){
		System.out.println(i+": Received Message from "+j+" with r = "+r);
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
			this.S[i] = 'O';
			System.out.println(i+": Changed "+this.toString());
			this.t.TS[j] = 'R';
			this.t.TN[j] = r;
			System.out.println(i+": Changed "+this.t.toString());
			sendTokenTo(j);
			break;
		}
	}
	public void receiveToken(Token t){
		System.out.println(i+": receive Token");
		this.t= t;
		S[i] = 'E';
		/* == start critical section == */
		try {	//random delay
			Thread.sleep((long)(Math.random() * 4000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("Performed CS");
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
		System.out.println(i+": Changed "+this.t.toString());
		for (int j = 0; j < N.length; j++) {
			if(S[j] == 'R'){
				sendTokenTo(j);
			} else{		//nobody requesting
				S[i] = 'H';
			}
		}
	}
	
	protected Token deepClone(Token t) {
		try {
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos);
		     oos.writeObject(t);
		     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		     ObjectInputStream ois = new ObjectInputStream(bais);
		     return (Token) ois.readObject();
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		     return null;
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
}

	
