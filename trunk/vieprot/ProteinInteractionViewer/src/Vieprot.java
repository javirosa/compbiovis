import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.data.Graph;
import prefuse.data.io.GraphCollection;
import prefuse.data.io.GraphCollectionMLReader;
import prefuse.util.GraphLib;
import prefuse.util.ui.UILib;


public class Vieprot extends JPanel implements ListSelectionListener {

	private GraphCollection modules;
	private ModuleViewer view;
	
	public Vieprot() {
		
	}
	
    public Vieprot(GraphCollection gc, String label) {
    	super(new BorderLayout());
        
    	modules = gc;
    	
        // --------------------------------------------------------------------        
        // launch the visualization
        view = new ModuleViewer(modules.getGraph(0), label);
    	
        // --------------------------------------------------------------------        
        // launch the visualization
    	
        // Create the JList with all the conserved modules
        /*
        DefaultListModel graphIDs= new DefaultListModel();
        for(int i=0; i<modules.numGraphs(); i++) {
        	graphIDs.addElement(modules.getID(i));
        }
        JList modules = new JList(graphIDs);
        modules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modules.setSelectedIndex(0);
        modules.addListSelectionListener(this);      
        JScrollPane moduleScrollPanel = new JScrollPane(modules);
        */
        ModuleBrowser moduleScrollPanel = new ModuleBrowser(modules, view);
        
        // create a new JSplitPane to present the interface
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(moduleScrollPanel);
        split.setRightComponent(view);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(false);
        split.setDividerLocation(200);
        
        add(split);
    }
	
    // ------------------------------------------------------------------------
    // Main and demo methods
    
    public static void main(String[] args) {
        UILib.setPlatformLookAndFeel();
        
        // create graphview
        String datafile = null;
        String label = "label";
        if ( args.length > 1 ) {
            datafile = args[0];
            label = args[1];
        }
        
        datafile = "208Modules.xml";
        
        JFrame frame = demo(datafile, label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
    }
    
    public static JFrame demo() {
        return demo((String)null, "label");
    }
    
    public static JFrame demo(String datafile, String label) {
        GraphCollection gc = null;
        if ( datafile == null ) {
        	gc = new GraphCollection();
            Graph g = GraphLib.getGrid(15,15);
            g.putClientProperty("id", "0");
            label = "label";
        } else {
            try {
                //g = new GraphMLReader().readGraph(datafile);
                gc = new GraphCollectionMLReader().readGraphCollection(datafile);
                gc.createMasterGraph();
            	label = "id";
                printGraphInfo(gc.getGraph(0));
            } catch ( Exception e ) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return demo(gc, label);
    }
    
    public static void printGraphInfo(Graph g) {
    	System.out.println(g.getNodeTable().getSchema().toString());
    	System.out.println(g.getEdgeTable().getSchema().toString());
    	System.out.format("# nodes: %d | # edges: %d\n", g.getNodeCount(), g.getEdgeCount());
    }
    
    public static JFrame demo(GraphCollection gc, String label) {
        Vieprot browser = new Vieprot(gc, label);
    	
    	// launch window
        JFrame frame = new JFrame("v i e p r o t");
        frame.setContentPane(browser);
        frame.pack();
        frame.setVisible(true);
        
        return frame;
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
}
