package week1;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ron, Laura
 *
 */

public class DAGrp20 extends UnicastRemoteObject implements DAGrp20_RMI {
	//number of processes n
	private static int port = 1099;
	private int n;
	private int i;		//process id
	private VectorClock v;	//vector clock
	private Buffer s;	//local buffer
	private ArrayList<MessageBuffer> b;
	private Registry registry;
	
	/**
	 * 
	 * @param argv takes as arguments: process id, number of processes
	 */
	public static void main(String argv[]){
		//add when using multiple machines
		//System.setSecurityManager(new RMISecurityManager());
			try {
				DAGrp20 process = new DAGrp20(Integer.parseInt(argv[0]), Integer.parseInt(argv[1]));
			} catch (NumberFormatException | RemoteException | AlreadyBoundException e) {
				System.out.println("incorrect input");
				e.printStackTrace();
				}
        
			/*
        try {	//test messaging
        	for(int i=0; i<n; i++){
        		DAGrp20 send = (DAGrp20) registry.lookup("Process"+i);
        		int r = ThreadLocalRandom.current().nextInt(0, n);
				String m = "From "+i+" to "+r;
				System.out.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.out.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.out.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.out.println("Send message: "+m);
				send.send(m, r);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
        System.exit(0);
	}
	

	/**
	 * creates registry and binds this to it
	 * @param id
	 * @throws RemoteException
	 * @throws AlreadyBoundException 
	 */
	public DAGrp20(int i, int n) throws RemoteException, AlreadyBoundException {
		this.setI(i); //set process id
		this.v = new VectorClock(n);
		this.s = new Buffer(n);
		this.b = new ArrayList<MessageBuffer>();
		registry = LocateRegistry.createRegistry(port+i); //TODO hostname for use across machines
		String name = "Process" + i;
		registry.bind(name, this);
		System.err.println(name + " is ready");
	}

	@Override
	public void send(String m, int recipient) throws RemoteException {
		try {	//random delay
			Thread.sleep((long)(Math.random() * 2000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			s = deepClone(s);	//deepClone before incrementing local time 
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		this.v.increment(i);	//increment local time
		try {	
			DAGrp20 recip = (DAGrp20) registry.lookup("Process"+recipient);
			recip.receive(m, s, v);	//invoke remote method
		} catch (NotBoundException e) {
			e.printStackTrace();
		} 
		//insert pair into local buffer
		System.out.println("Update Buffer of "+i);
		this.s.p[recipient] = recipient;
		this.s.vc[recipient] = v;
		
	}

	
	
	@Override
	public void receive(String m, Buffer s, VectorClock v) throws RemoteException {
		System.out.println(i+": Received message: "+m+" "+s+" "+v);
		// test if delivery condition is met
		if(s.p[i] == -1 || s.vc[i].compare(this.v) >= 0){
			System.out.println(i+": Delivered message:"+m+" to Process"+i);
			for (int i = 0; i < n; i++) {
				if(s.p[i] != -1){
					if(this.s.p[i] == -1 || this.s.vc[i].compare(s.vc[i]) == -1){
						this.s.p[i] = s.p[i];
						this.s.vc[i] = s.vc[i];
						System.out.println(i+": Updated buffer entry "+i+" with "+s.vc[i]);
					}
				}
			}
			this.v.update(v);
			this.v.increment(i);
		} else {
			//TODO read buffered messages?
			if(b.size() != 0){
				MessageBuffer mb = b.get(0);
				receive(mb.m, mb.s, mb.v);
			}
			b.add(new MessageBuffer(m, s, v));
			System.out.println("Buffered message");
		}
		
	}
	
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	
	private Buffer deepClone(Buffer b) throws CloneNotSupportedException {
		try {
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos);
		     oos.writeObject(b);
		     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		     ObjectInputStream ois = new ObjectInputStream(bais);
		     return (Buffer) ois.readObject();
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		     return null;
		   }
    }

}
