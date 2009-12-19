package vieprot.browser;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.data.Graph;
import prefuse.data.io.GraphCollection;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

import vieprot.browser.list.*;
import vieprot.browser.table.GOAnnotationTableModel;
import vieprot.viewer.ModuleViewer;
import vieprot.lib.Constants;
import vieprot.lib.InterfaceConstants;
import vieprot.structures.*;

public class ModuleBrowser extends JPanel implements ListSelectionListener, ActionListener {

	private GraphCollection modules;
	private ModuleViewer view;
    protected JTextArea moduleInfo;
	protected JList modulesList;
    
	private SortableModuleListModel graphIDs = new SortableModuleListModel();
	
	private boolean justSorted = false;
	
	public ModuleBrowser(GraphCollection gc, ModuleViewer v) {
		modules = gc;
		view = v;
		
		//Create the sorting combo box
		JLabel sortLabel = new JLabel("Sort on:", JLabel.RIGHT);
		
		String[] sortOptions = {InterfaceConstants.SORT_OPTIONS_ID, InterfaceConstants.SORT_OPTIONS_NUM_NODES, 
								InterfaceConstants.SORT_OPTIONS_ALIGNED_EDGES, InterfaceConstants.SORT_OPTIONS_ALIGNED_DEGREE};
		JComboBox sortBox = new JComboBox(sortOptions);
		sortBox.setSelectedItem("id");
		sortBox.addActionListener(this);
		
        // Create the JList with all the conserved modules
		for(int i=0; i<modules.numGraphs(); i++) {
			graphIDs.addElement(modules.getGraph(i));
		}
		
        DefaultListModel temp= new DefaultListModel();
		/*
		for(int i=0; i<modules.numGraphs(); i++) {
        	graphIDs.addElement(modules.getID(i));
        }
        */
        modulesList = new JList(graphIDs);
        //modulesList.setSelectionBackground(new Color(172,248,255));
        modulesList.setSelectionForeground(new Color(0,0,0));
        ModuleListItemRenderer renderer= new ModuleListItemRenderer();
        modulesList.setCellRenderer(renderer);
        
        modulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modulesList.setSelectedIndex(0);
        modulesList.addListSelectionListener(this);      
        JScrollPane moduleScrollPanel = new JScrollPane(modulesList);
        //moduleScrollPanel.setPreferredSize(new Dimension(100,900));
        //moduleScrollPanel.setPreferredSize(this.getSize());
        
        // Set up node info box
        moduleInfo = new JTextArea(4,20);
        moduleInfo.setEditable(false);
        Box moduleInfoBox = new Box(BoxLayout.Y_AXIS);
        moduleInfoBox.setVisible(true);
        moduleInfoBox.add(moduleInfo);
        moduleInfoBox.setBorder(BorderFactory.createTitledBorder("Module information"));
        moduleInfoBox.setPreferredSize(moduleInfo.getPreferredSize());
        initModuleInfoBox();
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        				.addComponent(sortLabel)
        				.addComponent(sortBox))
        		.addComponent(moduleScrollPanel)
        		.addComponent(moduleInfoBox)
        	);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        				.addComponent(sortLabel)
        				.addComponent(sortBox))
        		.addComponent(moduleScrollPanel)
        		.addComponent(moduleInfoBox)
        	);
	}

    public static void printGraphInfo(Graph g) {
    	System.out.println(g.getNodeTable().getSchema().toString());
    	System.out.println(g.getEdgeTable().getSchema().toString());
    	System.out.format("# nodes: %d | # edges: %d\n", g.getNodeCount(), g.getEdgeCount());
    }
    
    public void initModuleInfoBox() {
    	initModuleInfoBox((GraphWithMetadata)modulesList.getSelectedValue());
    }
    public void initModuleInfoBox(GraphWithMetadata graph) {
    	String info = String.format("# m0 modules:\t\t%d\n# m1 modules:\t\t%d\n# aligned edges:\t%d\nAverage aligned edge degree:\t%.3f",
    			graph.getNumberOfM0Nodes(), graph.getNumberOfM1Nodes(), graph.getNumberOfAlignedEdges(), graph.getAverageAlignedEdgeDegree());
    	moduleInfo.setText(info);
    }
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting() && !justSorted) {
			JList dlm = (JList)e.getSource();
			GraphWithMetadata selectedGraph = ((GraphWithMetadata)dlm.getSelectedValue());
			String selectedGraphID = selectedGraph.getID();
			Graph g = modules.getGraph(selectedGraphID);
			printGraphInfo(g);
			view.setGraph(g,"id");
			initModuleInfoBox(selectedGraph);
		}
		justSorted = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        justSorted = true;
        JComboBox cb = (JComboBox)e.getSource();
        Object selectedObject = modulesList.getSelectedValue();
        String sortOption = (String)cb.getSelectedItem();
        graphIDs.sort(sortOption);
        modulesList.setSelectedValue(selectedObject, true);
	}
}
