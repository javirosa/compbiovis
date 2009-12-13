package prefuse.controls;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.data.Tuple;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import vieprot.lib.Constants;
import vieprot.lib.InterfaceConstants;

public class EdgeHighlighter extends ControlAdapter {

    private String activity = null;
	
    /**
     * Creates a new highlight control.
     */
    public EdgeHighlighter() {
        this(null);
    }

    /**
     * Creates a new highlight control that runs the given activity
     * whenever the neighbor highlight changes.
     * @param activity the update Activity to run
     */
    public EdgeHighlighter(String activity) {
        this.activity = activity;
    }
    
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if ( item instanceof NodeItem )
            setAlignedEdgeHighlight((NodeItem)item, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if ( item instanceof NodeItem )
            setAlignedEdgeHighlight((NodeItem)item, false);
    }
    
    /**
     * Set the highlighted state of the neighbors of a node.
     * @param n the node under consideration
     * @param state the highlighting state to apply to neighbors
     */
    protected void setAlignedEdgeHighlight(NodeItem n, boolean state) {
        Iterator iter = n.edges();
        while ( iter.hasNext() ) {
			EdgeItem t = (EdgeItem)iter.next();
			if((boolean)((String)t.get("group")).equals(Constants.ALIGNED_EDGES))
				setStrokeColor(t,state);
        }
        if ( activity != null )
            n.getVisualization().run(activity);
    }
    
    protected void setStrokeColor(EdgeItem ei, boolean state) {
    	if(state)
    		ei.setStrokeColor(InterfaceConstants.ALIGNED_EDGE_HIGHLIGHT_COLOR);
    	else
    		ei.setStrokeColor(InterfaceConstants.ALIGNED_EDGE_STROKE_COLOR);
    }
}
