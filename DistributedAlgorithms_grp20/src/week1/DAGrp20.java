package week1;
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
	private static int port = 1098;
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
        		int r = ThreadLocalRandom.current().nextInt(1, n + 1);
				DAGrp20 rec = (DAGrp20) registry.lookup("Process"+r);
				String m = "Test to "+r;
				rec.send(m, r);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}
	

	/**
	 * @throws RemoteException
	 */
	public DAGrp20() throws RemoteException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
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
		try {	//random delay
			Thread.sleep((long)(Math.random() * 2000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		v.increment(i);	//increment local time
		try {	
			DAGrp20 recip = (DAGrp20) registry.lookup("Process"+recipient);
			recip.receive(m, s, v);	//invoke remote method
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		//insert pair into local buffer
			s.p[i] = i;
			s.vc[i] = v;
		
	}

	@Override
	public void receive(String m, Buffer s, VectorClock v) throws RemoteException {
		System.out.println("Received message: "+m+" Time "+v);
		// test if delivery condition is met
		if(s.p[i] == -1 || s.vc[i].compare(this.v) == -1){
			System.out.println("Delivered message:"+m+" to Process"+i);
			for (int i = 0; i < n; i++) {
				if(s.p[i] == -1){
					if(this.s.p[i] == -1 || this.s.vc[i].compare(s.vc[i]) == -1){
						this.s.p[i] = s.p[i];
						this.s.vc[i] = s.vc[i];
						System.out.println("Updated buffer entry "+i+" with "+s.vc[i]);
					}
				}
			}
		} else {
			//TODO read buffered messages?
			b.add(new MessageBuffer(m, s, v));
		}
		
	}
	
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}


	

}
