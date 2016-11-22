package week1;
import java.rmi.Remote;


/**
 * @author Ron
 *
 */
public interface DAGrp20_RMI extends Remote {
	public void send(String m, int recipient) throws java.rmi.RemoteException;
	public void receive(String m, Buffer s, VectorClock v) throws java.rmi.RemoteException;
}
