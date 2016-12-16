/**
 * 
 */
package week3;

/**
 * @author Ron
 *
 */
public class Message {
	enum Type{ TEST, Report, Connect}
	Type t;
	int L;
	int edge;
	int F;
	int w;
	public Message(Type t, int l, int edge, int f, int w) {
		this.t= t;
		L = l;
		this.edge = edge;
		F = f;
		this.w = w;
	}
	
	
	
}
