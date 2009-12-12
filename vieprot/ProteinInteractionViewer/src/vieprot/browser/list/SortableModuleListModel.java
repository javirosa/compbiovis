package vieprot.browser.list;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractListModel;

import prefuse.data.Graph;

import vieprot.lib.InterfaceConstants;
import vieprot.structures.GraphWithMetadata;
import vieprot.comparators.*;

public class SortableModuleListModel extends AbstractListModel {
	
	private Vector<GraphWithMetadata> data = new Vector<GraphWithMetadata>();
	private static NumberOfNodesComparator numberOfNodesComparator = new NumberOfNodesComparator(false);
	private static IDComparator idComparator = new IDComparator();
	
	public void addElement(Graph g) {
		GraphWithMetadata gmd = new GraphWithMetadata(g);
		addElement(gmd);
	}
	
	public void addElement(GraphWithMetadata g) {
		data.add(g);
		this.fireContentsChanged(this, data.size()-1, data.size()-1);
	}
	
	public void sort(String option) {
		if(option == InterfaceConstants.SORT_OPTIONS_ID)
			sortByID();
		else if(option == InterfaceConstants.SORT_OPTIONS_NUM_NODES)
			sortByNumberOfNodes();
	}
	
	public void sortByNumberOfNodes() {
		Collections.sort(data, numberOfNodesComparator);
		this.fireContentsChanged(this, 0, data.size()-1);
	}

	public void sortByID() {
		Collections.sort(data, idComparator);
		this.fireContentsChanged(this, 0, data.size()-1);
	}
	
	@Override
	public Object getElementAt(int index) {
		return (GraphWithMetadata)data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

}
