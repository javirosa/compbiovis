package prefuse.data.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.SAXException;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.util.collections.IntIterator;

public class GraphCollection {
	private Graph masterGraph;
	private ArrayList<Graph> graphs;
	private HashMap<String, Graph> ids;
	
	public GraphCollection() {
		graphs = new ArrayList<Graph>();
		ids = new HashMap<String, Graph>();
	}
	
	public void addGraph(Graph g) {
		graphs.add(g);
		ids.put((String)g.getClientProperty("id"), g);
	}
	
	public Graph getGraph(int index) {
		return graphs.get(index);
	}
	
	public Graph getGraph(String id) {
		return ids.get(id);
	}
	
	public String getID(int index) {
		return (String)graphs.get(index).getClientProperty("id");
	}
	
	public int numGraphs() {
		return graphs.size();
	}
	
	public void createMasterGraph() {
		if(graphs.size() > 0) {

			HashMap<String, Integer> nodeMap = new HashMap<String, Integer>();
			HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();
			
			Graph g0 = graphs.get(0);
			Table masterNodes = g0.getNodeTable().getSchema().instantiate();
			Table masterEdges = g0.getEdgeTable().getSchema().instantiate();
		
			int curNodeRow = 0;
			int curEdgeRow = 0;
			for(int i=0; i<graphs.size(); i++) {
				Graph g = graphs.get(i);
				Iterator nodes = g.getNodes().tuples();
				while(nodes.hasNext()) {
					Tuple n = (Tuple)nodes.next();
					String id = n.getString("id");
					if(!nodeMap.containsKey(id)) {
						masterNodes.addTuple(n);
						nodeMap.put(id, curNodeRow);
						curNodeRow++;
					}
				}
				
				Iterator edges = g.getEdges().tuples();
				while(edges.hasNext()) {
					Tuple e = (Tuple)edges.next();
					String id = e.getString("id");
					if(!edgeMap.containsKey(id)) {
						masterEdges.addTuple(e);
						edgeMap.put(id, curEdgeRow);
						curEdgeRow++;
					}
				}
			}
		
			masterGraph = new Graph(masterNodes, masterEdges, false);
		}
	}
} // End of class GraphCollection.java
