package vieprot.comparators;

import java.util.Comparator;

import vieprot.structures.GraphWithMetadata;

public class IDComparator implements Comparator<GraphWithMetadata> {

	@Override
	public int compare(GraphWithMetadata o1, GraphWithMetadata o2) {
		int o1ID = Integer.parseInt(o1.getID());
		int o2ID = Integer.parseInt(o2.getID());
		if(o1ID < o2ID) return -1;
		else if(o1ID == o2ID) return 0;
		else return 1;
	}

}
