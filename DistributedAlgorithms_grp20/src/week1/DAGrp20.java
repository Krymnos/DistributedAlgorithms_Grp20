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
 * @author Ron
 *
 */
public class DAGrp20 extends UnicastRemoteObject implements DAGrp20_RMI {
	//number of processes n
	private static int port = 1099;
	private static int n = 3;
	private int i;		//process id
	private VectorClock v;	//vector clock
	private Buffer s;	//local buffer
	private ArrayList<MessageBuffer> b;
	private static Registry registry = null;
	
	public static void main(String argv[]){
		
		//add when using multiple machines
		//System.setSecurityManager(new RMISecurityManager());
		
			
        	//create registry
        try {
			registry = LocateRegistry.createRegistry(port);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
        for(int i=0; i<n; i++){	//create processes
			try {
				//create stub
				DAGrp20 process = new DAGrp20(i);
				
		        // Bind the remote object's stub in the registry
				String name = "Process"+i;
				registry.bind(name, process);
				
			    System.err.println(name+" is ready");
	        } catch (Exception e) {
	            System.err.println("Server exception: " + e.toString());
	            e.printStackTrace();
	        }
		}
        try {	//test messaging
        	for(int i=0; i<n; i++){
        		DAGrp20 send = (DAGrp20) registry.lookup("Process"+i);
        		int r = ThreadLocalRandom.current().nextInt(0, n);
				String m = "From "+i+" to "+r;
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				send.send(m, r);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.exit(0);
	}
	

	/**
	 * @param id
	 * @throws RemoteException
	 */
	public DAGrp20(int i) throws RemoteException {
		this.setI(i); //set process id
		this.v = new VectorClock(n);
		this.s = new Buffer(n);
		this.b = new ArrayList<MessageBuffer>();
	}

	@Override
	public void send(String m, int recipient) throws RemoteException {
		/*try {	//random delay
			Thread.sleep((long)(Math.random() * 2000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		
		this.v.increment(i);	//increment local time
		try {	
			DAGrp20 recip = (DAGrp20) registry.lookup("Process"+recipient);
			recip.receive(m, deepClone(s), v);	//invoke remote method
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		//insert pair into local buffer
		System.out.println("update Buffer of "+i);
		this.s.p[recipient] = recipient;
		this.s.vc[recipient] = v;
		
	}

	@Override
	public void receive(String m, Buffer s, VectorClock v) throws RemoteException {
		System.out.println("Received message: "+m+" "+s+" "+v);
		// test if delivery condition is met
		if(s.p[i] == -1 || s.vc[i].compare(this.v) >= 0){
			System.out.println("Delivered message:"+m+" to Process"+i);
			for (int i = 0; i < n; i++) {
				if(s.p[i] != -1){
					if(this.s.p[i] == -1 || this.s.vc[i].compare(s.vc[i]) == -1){
						this.s.p[i] = s.p[i];
						this.s.vc[i] = s.vc[i];
						System.out.println("Updated buffer entry "+i+" with "+s.vc[i]);
					}
				}
			}
			this.v.update(v);
			this.v.increment(i);
		} else {
			//TODO read buffered messages?
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
