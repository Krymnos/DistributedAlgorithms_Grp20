
package week1;

import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;



public class TestCases {
	private static int n = 10;
	private static Registry registry = null;
	
	@Before
	public void init(){
		registry = DAGrp20.createRegistry();
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
       
        
	}
	
	/*
	 * send a lot of messages and checks if they all arrive at the right time
	*/
	@Test
	public void test() {
		try {	//test messaging
	    	for(int i=0; i<n; i++){
	    		DAGrp20 send = (DAGrp20) registry.lookup("Process"+i);
	    		int r = ThreadLocalRandom.current().nextInt(0, n);
				String m = "From "+i+" to "+r;
				System.err.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.err.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.err.println("Send message: "+m);
				send.send(m, r);
				r = ThreadLocalRandom.current().nextInt(0, n);
				m = "From "+i+" to "+r;
				System.err.println("Send message: "+m);
				send.send(m, r);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    System.exit(0);
		}
}
