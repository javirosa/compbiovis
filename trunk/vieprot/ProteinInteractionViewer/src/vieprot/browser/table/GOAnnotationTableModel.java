package vieprot.browser.table;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import vieprot.lib.Constants;

public class GOAnnotationTableModel extends AbstractTableModel {

	protected Vector<String[]> annotations;
	
	public GOAnnotationTableModel() {
		annotations = new Vector<String[]>();
	}
	
	@Override
	public int getColumnCount() {
		return Constants.GO_CATEGORIES.length;
	}

	@Override
	public int getRowCount() {
		return annotations.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return annotations.get(rowIndex)[columnIndex];
	}

}
