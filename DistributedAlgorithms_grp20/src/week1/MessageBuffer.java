package week1;

public class MessageBuffer {
	String m;
	Buffer s;
	VectorClock v;
	
	public MessageBuffer(String m, Buffer s, VectorClock v) {
		this.m = m;
		this.s = s;
		this.v = v;
	}
	
}
