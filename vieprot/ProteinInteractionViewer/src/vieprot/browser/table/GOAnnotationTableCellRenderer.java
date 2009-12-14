package vieprot.browser.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class GOAnnotationTableCellRenderer extends JLabel implements
		TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		String val = (String)table.getModel().getValueAt(row, column);
		setToolTipText(val);
		this.setText(val);
		return this;
	}

}
