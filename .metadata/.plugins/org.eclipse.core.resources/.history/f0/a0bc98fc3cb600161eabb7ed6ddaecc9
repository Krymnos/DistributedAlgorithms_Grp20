/**
 * 
 */
package week2;

/**
 * @author Ron
 *
 */
public class Component implements Component_RMI {
	private int i;	//id
	private int[] N;	
	private char[] S;
	private Token t;
	
	public Component(int size, int id){
		this.N = new int[size];
		this.S = new char[size];
		this.i = id;
		
		
		//initialize state arrays
		if(id == 0){
			this.S[0] = 'H'; //holding the token
			this.t= new Token(size);
			for (int i = 1; i < N.length; i++) {
				this.S[i] = 'O';
			}
		} else{
			for (int i = 0; i < id; i++) {
				this.S[i] = 'R'; //previous ones may have token
			}
			for (int i = id; i < N.length; i++) {
				this.S[i] = 'O'; //previous ones may have token
			}
		}
	}
	public void request(){
		S[i] = 'R';	//set own state to requesting
		N[i]++;		//increment request number
		for (int i = 0; i < this.i; i++) {
			
		}
		for (int i = this.i; i < N.length; i++) {
			
		}
	}
	
	public void sendReq(){
		for (int i = 0; i < N.length; i++) {
			
		}
		
	}
	public void receiveReq(int j, int r){
		N[j] = r;
		switch (S[i]) {
		case 'O':
			S[j] = 'R';			
			break;
		case 'E':
			S[j] = 'R';	
			break;
		case 'R':
			if(S[j] != 'R')
				S[j] = 'R';	
				//TODO Send request to j
			break;
		case 'H':
			S[j] = 'R';
			S[i] = 'O';
			this.t.TS[j] = 'R';
			this.t.TN[j] = r;
			//TODO send token
			break;
		}
	}
	public void receiveToken(Token t){
		S[i] = 'E';
		/* == start critical section == */
		try {	//random delay
			Thread.sleep((long)(Math.random() * 4000));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		/* == end critical section == */
		S[i] = 'O';
		t.TS[i] = 'O';
		for (int j = 0; j < N.length; j++) {
			if(N[j] > t.TN[j]){
				t.TN[j] = N[j];
				t.TS[j] = S[j];
			} else{
				N[j] = t.TN[j];
				S[j] = t.TS[j];
			}
		}
		for (int j = 0; j < N.length; j++) {
			if(S[j] == 'R'){
				//TODO send token to j
			}
		}
		
	}
	
	@Override
	public String toString(){
		String s ="State array of "+this.i+": (";
		for (int i = 0; i < N.length; i++) {
			s += this.S[i]+" ";
		}
		return s+")";
		
	}
}
