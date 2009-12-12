package vieprot.browser.list;

import java.util.Collections;
import java.util.LinkedList;

import javax.swing.AbstractListModel;

import prefuse.data.Graph;

import vieprot.structures.GraphWithMetadata;
import vieprot.comparators.*;

public class SortableModuleListModel extends AbstractListModel {
	
	private LinkedList<GraphWithMetadata> data = new LinkedList<GraphWithMetadata>();
	private static NumberOfNodesComparator numberOfNodesComparator = new NumberOfNodesComparator();
	
	public void addElement(Graph g) {
		GraphWithMetadata gmd = new GraphWithMetadata(g);
		addElement(gmd);
	}
	
	public void addElement(GraphWithMetadata g) {
		data.add(g);
		this.fireContentsChanged(this, data.size()-1, data.size()-1);
	}
	
	public void sortByNumberOfNodes() {
		Collections.sort(data, numberOfNodesComparator);
		this.fireContentsChanged(this, 0, data.size()-1);
	}
	
	@Override
	public Object getElementAt(int index) {
		return data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

}
