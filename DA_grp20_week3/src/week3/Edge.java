package week3;

public class Edge {
	int node1;
	int node2;
	int weight;
	
	
	public Edge(int node1, int node2, int weight) {
		this.node1 = node1;
		this.node2 = node2;
		this.weight = weight;
	}
	
	@Override
	public String toString(){
		return "[ "+node1+", "+node2+", "+weight+" ]";
	}
	

}
