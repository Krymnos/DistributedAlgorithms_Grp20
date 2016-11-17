import java.awt.SecondaryLoop;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Ron
 *
 */
public class DAGrp20 extends UnicastRemoteObject implements DAGrp20_RMI, Runnable {
	//number of processes n
	private static int port = 1099;
	private static int n = 3;
	private int i;
	private int[] t;
	private int[][] v;
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
        for(int i=0; i<n; i++){
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
				DAGrp20 rec = (DAGrp20) registry.lookup("Process"+i);
				String m = "Test to "+i;
				rec.send(m, i);
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		this.setI(i); //set process index
		this.t = new int[n];
		this.v = new int[n][n];
	}

	/**
	 * @param port
	 * @param csf
	 * @param ssf
	 * @throws RemoteException
	 */
	public DAGrp20(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
		super(port, csf, ssf);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void send(String m, int recipient) throws RemoteException {
		try {	//random delay
			Thread.sleep((long)(Math.random() * 2000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		t[i]++;	//increment local time
		try {	
			DAGrp20 rec = (DAGrp20) registry.lookup("Process"+recipient);
			rec.receive(m, t, v);	//invoke remote method
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		v[recipient] = t;
	}

	@Override
	public void receive(String m, int[] tm, int[][] v) throws RemoteException {
		System.out.println("Message: "+m+" Time m: "+tm);
		// test if if(v[i])
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public int getI() {
		return i;
	}


	public void setI(int i) {
		this.i = i;
	}


	

}
