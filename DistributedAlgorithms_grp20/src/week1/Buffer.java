package week1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Buffer implements Cloneable, Serializable{
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
		for (int i = 0; i < p.length; i++) {
			s+="("+this.p[i]+", "+this.vc[i]+")";
		}
		return s+="]";
	}
	/*
	@Override
    protected Object clone() throws CloneNotSupportedException {
		return deepClone(this);
    }
	
	protected Buffer deepClone(Buffer b) throws CloneNotSupportedException {
		try {
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos);
		     oos.writeObject(b);
		     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		     ObjectInputStream ois = new ObjectInputStream(bais);
		     return (Buffer) ois.readObject();
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		     return null;
		   }
    }*/
}
