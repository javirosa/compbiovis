package prefuse.controls;

import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JTable;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import vieprot.browser.table.GOAnnotationTableModel;

public class VieprotFocusControl extends FocusControl {
    private String group = Visualization.FOCUS_ITEMS;
    private Box tableBox;
	private JTable annotationTable;
    
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
                tableBox.setVisible(true);
                ((GOAnnotationTableModel)annotationTable.getModel()).initData(item);
                runActivity(vis);
                
            } else {
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                ts.removeTuple(item);
                //tableBox.setVisible(false);
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
}
