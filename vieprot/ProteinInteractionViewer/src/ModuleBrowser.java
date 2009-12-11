import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.data.Graph;
import prefuse.data.io.GraphCollection;

public class ModuleBrowser extends JPanel implements ListSelectionListener, ActionListener {

	private GraphCollection modules;
	private ModuleViewer view;

	public ModuleBrowser(GraphCollection gc, ModuleViewer v) {
		modules = gc;
		view = v;
		
		//Create the sorting combo box
		JLabel sortLabel = new JLabel("Sort on:", JLabel.RIGHT);
		
		String[] sortOptions = {"# proteins", "# aligned edges", "Avg. degree of node"};
		JComboBox sortBox = new JComboBox(sortOptions);
		
        // Create the JList with all the conserved modules
        DefaultListModel graphIDs= new DefaultListModel();
        for(int i=0; i<modules.numGraphs(); i++) {
        	graphIDs.addElement(modules.getID(i));
        }
        JList modules = new JList(graphIDs);
        modules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modules.setSelectedIndex(0);
        modules.addListSelectionListener(this);      
        JScrollPane moduleScrollPanel = new JScrollPane(modules);
        //moduleScrollPanel.setPreferredSize(new Dimension(100,900));
        //moduleScrollPanel.setPreferredSize(this.getSize());
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        				.addComponent(sortLabel)
        				.addComponent(sortBox))
        		.addComponent(moduleScrollPanel)
        	);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        				.addComponent(sortLabel)
        				.addComponent(sortBox))
        		.addComponent(moduleScrollPanel)
        	);
	}

    public static void printGraphInfo(Graph g) {
    	System.out.println(g.getNodeTable().getSchema().toString());
    	System.out.println(g.getEdgeTable().getSchema().toString());
    	System.out.format("# nodes: %d | # edges: %d\n", g.getNodeCount(), g.getEdgeCount());
    }
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) {
			JList dlm = (JList)e.getSource();
			Graph g = modules.getGraph((String)dlm.getSelectedValue());
			printGraphInfo(g);
			view.setGraph(g,"id");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String sortOption = (String)cb.getSelectedItem();
	}
}
