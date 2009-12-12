package vieprot.structures;

import prefuse.data.Graph;

public class GraphWithMetadata {
	private Graph g;
	private int numberOfNodes;
	
	public GraphWithMetadata(Graph g) {
		this.g = g;
		numberOfNodes = g.getNodeCount();
	}
	
	public int getNumberOfNodes() { return numberOfNodes; }
	
	public String getID() { return (String)g.getClientProperty("id"); }
	
	public String toString() {
		return String.format("%s\t|\t# nodes: %d",g.getClientProperty("id"), numberOfNodes);
	}
}