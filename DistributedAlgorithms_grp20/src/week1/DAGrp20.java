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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ron, Laura
 *
 */

public class DAGrp20 extends UnicastRemoteObject implements DAGrp20_RMI, Runnable {
	//number of processes n
	private static int port = 1099;
	private int n;
	private int i;		//process id
	private static VectorClock v;	//vector clock
	private static Buffer s;	//local buffer
	private ArrayList<MessageBuffer> b;
	private Registry registry;
	private int messageCounter = 0;
	
	/**
	 * This code was used to send 2 messages to a seperate machine. The first message was delayed on that machine.
	 * 
	 * @param argv takes as arguments: process id, number of processes
	 */
	public static void main(String argv[]){
		//add when using multiple machines
		//System.setSecurityManager(new RMISecurityManager());
			try {
				Thread p1 = new Thread(new DAGrp20(Integer.parseInt(argv[0]), Integer.parseInt(argv[1])));
				Thread p2 = new Thread(new DAGrp20(Integer.parseInt(argv[0]), Integer.parseInt(argv[1])));
				p1.start();
				p2.start();
			} catch (NumberFormatException | RemoteException | AlreadyBoundException e) {
				System.out.println("incorrect input");
				e.printStackTrace();
				}
	}
	

	/**
	 * creates registry and binds this to it
	 * @param id
	 * @throws RemoteException
	 * @throws AlreadyBoundException 
	 */
	public DAGrp20(int i, int n) throws RemoteException, AlreadyBoundException {
		this.setI(i); //set process id
		this.n = n;
		this.v = new VectorClock(n);
		this.s = new Buffer(n);
		this.b = new ArrayList<MessageBuffer>();
		System.setProperty("java.rmi.server.hostname", "145.94.192.78");
		registry = LocateRegistry.createRegistry(port); 
		String name = "Process" + i;
		registry.bind(name, this);
		System.err.println(name + " is ready");
		
		
	}

	@Override
	public void send(String m, int recipient) throws RemoteException {
		System.err.println("Send message: "+m);
		Buffer newS = null;
		try {
			newS = deepClone(s);	//deepClone before incrementing local time 
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		DAGrp20.v.increment(i);	//increment local time
		//insert pair into local buffer
				System.out.println("Update Buffer of "+i);
				DAGrp20.s.p[recipient] = recipient;
				DAGrp20.s.vc[recipient] = v;
		try {	
			Registry reg = LocateRegistry.getRegistry("145.94.215.68", 1099);
			DAGrp20_RMI recip = (DAGrp20_RMI) reg.lookup("Process"+recipient);
			recip.receive(m, newS, v);	//invoke remote method
		} catch (NotBoundException e) {
			e.printStackTrace();
		} 
		
		
	}

	
	
	@Override
	public void receive(String m, Buffer s, VectorClock v) throws RemoteException {
		System.out.println(i+": Received message: "+m+" "+s+" "+v);
		// test if delivery condition is met
		if(s.p[i] == -1 || s.vc[i].compare(this.v) >= 0){
			System.out.println(i+": Delivered message:"+m+" to Process"+i);
			deliver(m, s, v);
		} else {
			if(b.size() != 0){
				MessageBuffer mb = b.remove(0);
				deliver(mb.m, mb.s, mb.v);
			}
			b.add(new MessageBuffer(m, s, v));
			System.out.println("Buffered message");
		}
		
	}
	
	private void deliver(String m, Buffer s, VectorClock v){
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


	@Override
	public void run() {
		System.out.println("Running");
		try {	// delay
			Thread.sleep((long)(4000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {	//random delay
			Thread.sleep((long)(Math.random()*2000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//test messaging
		//int r = ThreadLocalRandom.current().nextInt(0, n);
		int r = 1;
		String m = "From "+i+" to "+r+" "+ new Timestamp(System.currentTimeMillis());
		try {
			send(m, r);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		
		try {	// delay
			Thread.sleep((long)(20000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("End");
		System.exit(0);
	}
	

}
