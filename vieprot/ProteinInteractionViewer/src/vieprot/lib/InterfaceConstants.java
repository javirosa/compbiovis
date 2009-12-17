package vieprot.lib;

import prefuse.util.ColorLib;

public class InterfaceConstants {

	public static String SORT_OPTIONS_ID = "id";
	public static String SORT_OPTIONS_NUM_NODES = "# proteins";
	public static String SORT_OPTIONS_ALIGNED_EDGES = "# aligned edges";
	public static String SORT_OPTIONS_ALIGNED_DEGREE = "aligned degree";
	
	// Colors
	public static int MODULE_0_FILL_COLOR = ColorLib.rgb(255, 162, 162);
	public static int MODULE_0_HIGHLIGHT_COLOR = ColorLib.rgb(255,84,84);
	public static int MODULE_1_FILL_COLOR = ColorLib.rgb(121, 174, 255);
	public static int MODULE_1_HIGHLIGHT_COLOR = ColorLib.rgb(63,108,255);
	public static int FIXED_COLOR = ColorLib.rgb(255,100,100);
	
	public static int ALIGNED_EDGE_STROKE_COLOR = ColorLib.rgba(200,0,0,30);
	public static int ALIGNED_EDGE_HIGHLIGHT_COLOR = ColorLib.rgba(200, 0, 0, 255);
	public static int[] INTERPOLATED_PALETTE = ColorLib.getInterpolatedPalette(ALIGNED_EDGE_STROKE_COLOR, ALIGNED_EDGE_HIGHLIGHT_COLOR);

}
