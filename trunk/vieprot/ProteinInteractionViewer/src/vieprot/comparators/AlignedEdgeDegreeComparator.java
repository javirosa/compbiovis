package vieprot.comparators;

import java.util.Comparator;

import vieprot.structures.GraphWithMetadata;

public class AlignedEdgeDegreeComparator implements
		Comparator<GraphWithMetadata> {
	
	boolean ascending;
	
	public AlignedEdgeDegreeComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	@Override
	public int compare(GraphWithMetadata o1, GraphWithMetadata o2) {
		if(o1.getAverageAlignedEdgeDegree() < o2.getAverageAlignedEdgeDegree()) return ascending ? -1 : 1;
		else if(o1.getAverageAlignedEdgeDegree() < o2.getAverageAlignedEdgeDegree()) return 0;
		else return ascending ? 1 : -1;
	}

}