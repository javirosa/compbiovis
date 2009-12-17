package vieprot.structures;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Literal;
import prefuse.data.expression.Predicate;
import vieprot.lib.Constants;

public class GraphWithMetadata {
	private Graph g;
	private int numberOfNodes;
	private int numberAlignedEdges;
	private float averageAlignedEdges;
	private int module0Nodes;
	private int module1Nodes;
	
	public GraphWithMetadata(Graph g) {
		this.g = g;
		numberOfNodes = g.getNodeCount();

		module0Nodes = 0;
		Iterator<Tuple> module0Iterator = g.getNodes().tuples();
		while(module0Iterator.hasNext())
		{
			Tuple t = module0Iterator.next();
			if((boolean)((String)t.get("group")).equals(Constants.MODULE_0))
				module0Nodes++;
		}
		
		module1Nodes = 0;
		Iterator<Tuple> module1Iterator = g.getNodes().tuples();
		while(module1Iterator.hasNext())
		{
			Tuple t = module1Iterator.next();
			if((boolean)((String)t.get("group")).equals(Constants.MODULE_1))
				module1Nodes++;
		}
		
		numberAlignedEdges = 0;
		Iterator<Tuple> alignedEdgeIterator = g.getEdges().tuples();
		while(alignedEdgeIterator.hasNext())
		{
			Tuple t = alignedEdgeIterator.next();
			if((boolean)((String)t.get("group")).equals(Constants.ALIGNED_EDGES))
				numberAlignedEdges++;
		}
		
		averageAlignedEdges = (float)(numberAlignedEdges*2)/(float)numberOfNodes;
	}
	
	public int getNumberOfNodes() { return numberOfNodes; }
	public int getNumberOfM0Nodes() { return module0Nodes; }
	public int getNumberOfM1Nodes() { return module1Nodes; }
	public int getNumberOfAlignedEdges() { return numberAlignedEdges; }
	public float getAverageAlignedEdgeDegree() { return averageAlignedEdges; }
	
	public String getID() { return (String)g.getClientProperty("id"); }
	
	public String toString() {
		return String.format("%s\t|\tm0: %d\t|\tm1: %d|\tavg: %.3f",g.getClientProperty("id"), module0Nodes, module1Nodes, averageAlignedEdges);
	}
}