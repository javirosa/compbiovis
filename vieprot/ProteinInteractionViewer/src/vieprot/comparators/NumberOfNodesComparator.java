package vieprot.comparators;

import java.util.Comparator;
import vieprot.structures.GraphWithMetadata;

public class NumberOfNodesComparator implements Comparator<GraphWithMetadata> {

	boolean ascending;
	
	public NumberOfNodesComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	@Override
	public int compare(GraphWithMetadata o1, GraphWithMetadata o2) {
		if(o1.getNumberOfNodes() < o2.getNumberOfNodes()) return ascending ? -1 : 1;
		else if(o1.getNumberOfNodes() < o2.getNumberOfNodes()) return 0;
		else return ascending ? 1 : -1;
	}

}
