package week1;

public class Buffer {
	int size;
	int[] p;
	VectorClock[] vc;
	
	
	public Buffer(int size){
		this.p = new int[size];
		for (int i = 0; i < size; i++) {
	        p[i] = -1;
	    }
		this.vc = new VectorClock[size];
	}
	
	@Override
	public String toString(){
		String s = "[";
		for (int i = 0; i < size; i++) {
			s+="("+p[i]+", "+vc[i]+")";	//TODO not working
		}
		return s+="]";
	}
}
