package vieprot.browser.list;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import sun.swing.DefaultLookup;

public class ModuleListItemRenderer extends JLabel implements ListCellRenderer {

	   /**
	    * An empty <code>Border</code>. This field might not be used. To change the
	    * <code>Border</code> used by this renderer override the 
	    * <code>getListCellRendererComponent</code> method and set the border
	    * of the returned component directly.
	    */
	    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		//TODO: Change the background when it's selected
		Color selectionBackground = list.getSelectionBackground();
		Color selectionForeground = list.getSelectionForeground();
		Color background = list.getBackground();
		Color foreground = list.getForeground();
		
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());

        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        };
        
        
        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);
        
    	setEnabled(list.isEnabled());
        setText(value.toString());
        return this;
	}
	
    private Border getNoFocusBorder() {
        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) return border;
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if (border != null &&
                    (noFocusBorder == null ||
                    noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
                return border;
            }
            return noFocusBorder;
        }
    }

}
