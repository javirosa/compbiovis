package prefuse.data.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.SAXException;

import prefuse.data.Graph;
import prefuse.data.Schema;
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
			Schema nodeSchema = (Schema)g0.getNodeTable().getSchema().clone();
			nodeSchema.addColumn(vieprot.lib.Constants.MODULE_PARTICIPATION, int.class);
			nodeSchema.addColumn(vieprot.lib.Constants.TOTAL_DEGREE, int.class);
			Table masterNodes = nodeSchema.instantiate();
			Table masterEdges = g0.getEdgeTable().getSchema().instantiate();
		
			int curNodeRow = 0;
			int curEdgeRow = 0;
			HashMap<Integer, Integer> globalDegree = new HashMap<Integer, Integer>();
			for(int i=0; i<graphs.size(); i++) {
				Graph g = graphs.get(i);
				Iterator nodes = g.getNodes().tuples();
				while(nodes.hasNext()) {
					Tuple n = (Tuple)nodes.next();
					String id = n.getString("id");
					if(!nodeMap.containsKey(id)) {
						Tuple t = masterNodes.addTuple(n);
						t.setInt(vieprot.lib.Constants.MODULE_PARTICIPATION, 1);
						nodeMap.put(id, curNodeRow);
						curNodeRow++;
					}
					else {
						Tuple t = masterNodes.getTuple(nodeMap.get(id));
						t.setInt(vieprot.lib.Constants.MODULE_PARTICIPATION, t.getInt(vieprot.lib.Constants.MODULE_PARTICIPATION)+1);
					}
				}
				
				Iterator edges = g.getEdges().tuples();
				while(edges.hasNext()) {
					Tuple e = (Tuple)edges.next();
					String id = e.getString("id");
					if(!edgeMap.containsKey(id)) {
						masterEdges.addTuple(e);
						edgeMap.put(id, curEdgeRow);
						int source = e.getInt("source");
						int target = e.getInt("target");
						if(!globalDegree.containsKey(source))
							globalDegree.put(source, 0);
						if(!globalDegree.containsKey(target))
							globalDegree.put(target, 0);
						
						int newVal = globalDegree.get(source) + 1;
						globalDegree.remove(source);
						globalDegree.put(source, newVal);
						
						newVal = globalDegree.get(target) + 1;
						globalDegree.remove(target);
						globalDegree.put(target, newVal);
						
						curEdgeRow++;
					}
				}
			}
		
			masterGraph = new Graph(masterNodes, masterEdges, false);
		}
	}
} // End of class GraphCollection.java
