package week1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class VectorClock implements Cloneable, Serializable{
	
	private int size;
	private int[] v;
	
	public VectorClock(int size){
		this.size = size;
		v = new int[size];
	}
	public int getElement(int i){
		return v[i];
	}
	public void setElement(int i, int t){
		v[i] = t;
	}
	public void increment(int i){
		v[i]++;
	}
	public int length(){
		return this.size;
	}
	/**
	 * compare the logical time of this VectorClock with another VC of the same size.
	 * @return -1 = happened before, 0 = concurrent, 1 = happened after
	 */
	public int compare(VectorClock vc){
		int before = 0;
		int after = 0;
		int result = 0;
		
		for(int i=0;i<size;i++){	//for each Element check before or after
			if(v[i] < vc.getElement(i)){
				before++;
			} else if(v[i] > vc.getElement(i)){
				after++;
			}
		}
		if(before > 0 && after == 0){
			result = -1;
		} else if(before == 0 && after > 0){
			result = 1;
		}
		return result;
	}
	public void update(VectorClock vc){
		for (int i = 0; i < size; i++) {
			this.v[i] = Math.max(v[i], vc.getElement(i));
		}
		
	}
	
	@Override
	public String toString(){
		String s = "VC: ";
		for(int i=0;i<size;i++){
			s += " "+v[i];
		}
		return s;
	}
	
	/*protected VectorClock deepClone() throws CloneNotSupportedException {
		try {
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos);
		     oos.writeObject(this);
		     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		     ObjectInputStream ois = new ObjectInputStream(bais);
		     return (VectorClock) ois.readObject();
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		     return null;
		   }
    }*/
	
}
