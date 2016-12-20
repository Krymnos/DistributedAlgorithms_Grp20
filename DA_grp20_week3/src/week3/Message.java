/**
 * 
 */
package week3;

import week3.Component.enumSN;

/**
 * @author Ron
 *
 */
public class Message {
	enum Type{ TEST, Report, Connect, Initiate, Accept, ChangeRoot, Reject}
	Type t;
	enumSN s;
	int L;
	int edge;
	int F;
	int w;
	
	/**
	 *  add new Message ( type, level, edge, F, weight)
	 * @param t
	 * @param l
	 * @param edge
	 * @param f
	 * @param w
	 */
	public Message(Type t, int edge, enumSN s, int w, int f, int l) {
		this.t= t;
		this.s = s;
		L = l;
		this.edge = edge;
		F = f;
		this.w = w;
	}
	
	
	
}
