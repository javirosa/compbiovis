package prefuse.controls;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JTable;
import javax.swing.JTextArea;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import vieprot.browser.table.GOAnnotationTableModel;
import vieprot.lib.Constants;

public class VieprotFocusControl extends FocusControl {
    private String group = Visualization.FOCUS_ITEMS;
    private Box tableBox;
	private JTable annotationTable;
    private Box nodeInfoBox;
    private JTextArea nodeInfoArea;
	
	public VieprotFocusControl() {
		super();
	}
	
	public VieprotFocusControl(int clicks) {
		ccount = clicks;
	}
	
	public void setBox(Box b) {
		tableBox = b;
	}
	
	public void setTable(JTable t) {
		annotationTable = t;
	}
	
	public void setActivity(String activity) {
		this.activity = activity;
	}
	
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( !filterCheck(item) ) return;
        if ( UILib.isButtonPressed(e, button) &&
             e.getClickCount() == ccount )
        {
            if ( item != curFocus ) {
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                    
                boolean ctrl = e.isControlDown();
                if ( !ctrl ) {
                    curFocus = item;
                    ts.setTuple(item);
                } else if ( ts.containsTuple(item) ) {
                    ts.removeTuple(item);
                } else {
                    ts.addTuple(item);
                }
                nodeInfoBox.setVisible(true);
                tableBox.setVisible(true);
                nodeInfoArea.setText(getNodeInfo((NodeItem)item));
                ((GOAnnotationTableModel)annotationTable.getModel()).initData(item);
                runActivity(vis);
                
            } else {
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                ts.removeTuple(item);
                //tableBox.setVisible(false);
                nodeInfoArea.setText("");
                ((GOAnnotationTableModel)annotationTable.getModel()).clearData();
                curFocus = null;
                runActivity(vis);
            }
        }
    }
    
    private void runActivity(Visualization vis) {
        if ( activity != null ) {
            vis.run(activity);
        }
    }

	public void setNodeInfoBox(Box nodeInfoBox) {
		this.nodeInfoBox = nodeInfoBox;
	}

	public void setNodeInfoArea(JTextArea nodeInfo) {
		this.nodeInfoArea = nodeInfo;
	}
	
	public String getNodeInfo(NodeItem n) {
		int numAlignedEdges = 0, numInternalEdges = 0, numGOAnnotations = 0, globalDegree = 0;
        Iterator iter = n.edges();
        while ( iter.hasNext() ) {
			EdgeItem t = (EdgeItem)iter.next();
			if((boolean)((String)t.get("group")).equals(Constants.ALIGNED_EDGES))
				numAlignedEdges++;
			else
				numInternalEdges++;
        }
        
		String annsRaw = (String)n.get(Constants.GO_ANNOTATION);
		String[] annsSplit = annsRaw.split("[|]");
		numGOAnnotations = annsSplit.length;
		globalDegree = n.getInt(Constants.ANALYSIS_ANNOTATION);
		
		return String.format("Global degree:\t\t%d\n# Aligned edges:\t%d\n# Internal edges:\t%d\n# GO Annotations:\t%d",
				globalDegree,numAlignedEdges,numInternalEdges,numGOAnnotations);
	}
}
