package vieprot.comparators;

import java.util.Comparator;

import vieprot.structures.GraphWithMetadata;

public class NumberOfAlignedEdgesComparator implements Comparator<GraphWithMetadata> {

	boolean ascending;
	
	public NumberOfAlignedEdgesComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	@Override
	public int compare(GraphWithMetadata o1, GraphWithMetadata o2) {
		if(o1.getNumberOfAlignedEdges() < o2.getNumberOfAlignedEdges()) return ascending ? -1 : 1;
		else if(o1.getNumberOfAlignedEdges() < o2.getNumberOfAlignedEdges()) return 0;
		else return ascending ? 1 : -1;
	}

}
