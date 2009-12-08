package prefuse.data.io;

import java.util.ArrayList;
import java.util.HashMap;

import prefuse.data.Graph;

public class GraphCollection {
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
}
