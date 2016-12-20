/**
 * 
 */
package week3;

import java.rmi.Remote;
import java.rmi.RemoteException;

import week3.Component.enumSN;
import week3.Message.Type;

/**
 * @author Ron
 *
 */
public interface Component_RMI extends Remote {
	public void receive(Type t, int edge, enumSN S, int w, int F, int L ) throws RemoteException;

}
