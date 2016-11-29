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
	public void receiveToken(Token t);
	public void receiveReq(int j, int r);
}
