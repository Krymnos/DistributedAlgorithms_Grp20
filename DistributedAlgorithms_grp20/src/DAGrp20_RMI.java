import java.rmi.Remote;

/**
 * 
 */

/**
 * @author Ron
 *
 */
public interface DAGrp20_RMI extends Remote {
	public void send(String m, int recipient) throws java.rmi.RemoteException;
	public void receive(String m, int[] tm, int[][] v) throws java.rmi.RemoteException;
}
