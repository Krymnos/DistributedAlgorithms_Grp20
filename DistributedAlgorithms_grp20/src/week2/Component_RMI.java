/**
 * 
 */
package week2;

import java.rmi.Remote;

/**
 * @author Ron
 *
 */
public interface Component_RMI extends Remote {
	public void receiveToken(Token t) throws java.rmi.RemoteException;
	public void receiveReq(int j, int r) throws java.rmi.RemoteException;
}
