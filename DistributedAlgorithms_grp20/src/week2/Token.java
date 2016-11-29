package week2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import week1.Buffer;

/**
 * @author Ron
 *
 */
public class Token implements Serializable {
	int[] TN;
	char[] TS;
	
	/**
	 * 
	 * @param size Number of Processes in the system
	 */
	public Token(int size){
		this.TN = new int[size];
		this.TS = new char[size];
		
		for (int i = 0; i < TN.length; i++) {
			this.TS[i] = 'O';
		}
	}
	protected Token deepClone() {
		try {
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos);
		     oos.writeObject(this);
		     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		     ObjectInputStream ois = new ObjectInputStream(bais);
		     return (Token) ois.readObject();
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		     return null;
		   }
    }
	@Override
	public String toString(){
		String s = "TN: (";
		for (int i = 0; i < TN.length; i++) {
			s += TN[i] + " ";
		}
		s += ") TS: (";
		for (int i = 0; i < TN.length; i++) {
			s += TS[i] + " ";
		}
		return s + ")";
	}
}
