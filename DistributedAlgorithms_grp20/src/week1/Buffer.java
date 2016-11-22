package week1;

public class Buffer {
	int p;
	VectorClock vc;
	
	public Buffer(int p, VectorClock vc){
		this.p = p;
		this.vc = vc;
	}
}
