package vieprot.browser.table;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import prefuse.visual.VisualItem;

import vieprot.lib.Constants;

public class GOAnnotationTableModel extends AbstractTableModel {

	protected Vector<String[]> annotations;
	
	public GOAnnotationTableModel() {
		annotations = new Vector<String[]>();
	}
	
	public void initData(VisualItem ni) {
		annotations.clear();
		String annsRaw = (String)ni.get(Constants.GO_ANNOTATION);
		String[] annsSplit = annsRaw.split("[|]");
		for(int i=0; i<annsSplit.length; i++) {
			annotations.add(annsSplit[i].split(","));
		}
		this.fireTableDataChanged();
	}
	
	public void clearData() {
		annotations.clear();
	}
	
	@Override
	public int getColumnCount() {
		return Constants.GO_CATEGORIES.length;
	}
	
	public String getColumnName(int col) {
		return Constants.GO_CATEGORIES[col];
	}
	
	public Class getColumnClass(int c) {
		return getValueAt(0,c).getClass();
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
