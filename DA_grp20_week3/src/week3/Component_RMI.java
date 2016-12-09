/**
 * 
 */
package week3;

import java.rmi.Remote;
import java.rmi.RemoteException;

import week3.Component.enumSN;

/**
 * @author Ron
 *
 */
public interface Component_RMI extends Remote {
	public void receiveInitiate(int L, int F, enumSN S, int j) throws RemoteException;
	public void receiveTest(int L, int F, int j) throws RemoteException;
	public void receiveAccept(int j) throws RemoteException;
	public void receiveReject(int j) throws RemoteException;
	public void receiveReport(int w, int j) throws RemoteException;
	public void receiveChangeRoot(int j) throws RemoteException;
	public void receiveConnect(int L, int j) throws RemoteException;

}
