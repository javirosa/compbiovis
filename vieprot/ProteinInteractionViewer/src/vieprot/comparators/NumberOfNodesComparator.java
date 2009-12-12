package vieprot.comparators;

import java.util.Comparator;
import vieprot.structures.GraphWithMetadata;

public class NumberOfNodesComparator implements Comparator<GraphWithMetadata> {

	@Override
	public int compare(GraphWithMetadata o1, GraphWithMetadata o2) {
		if(o1.getNumberOfNodes() < o2.getNumberOfNodes()) return -1;
		else if(o1.getNumberOfNodes() < o2.getNumberOfNodes()) return 0;
		else return 1;
	}

}
