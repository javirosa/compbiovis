import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Literal;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.GraphCollection;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphCollectionMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

public class ModuleViewer extends JPanel implements ActionListener {

    private static final String graph = "graph";
    private static final String nodes = "graph.nodes";
    private static final String edges = "graph.edges";
    private static final String interEdges = "graph.interEdges";

    private static final String currentlyVisible = "currentlyVisible";
    
    private Visualization m_vis;
    
    private JCheckBox alignedEdges;
    
    public ModuleViewer(Graph g, String label) {
    	super(new BorderLayout());
    	
        // create a new, empty visualization for our data
        m_vis = new Visualization();
        
        // --------------------------------------------------------------------
        // set up the renderers
        
        LabelRenderer tr = new LabelRenderer();
        tr.setRoundedCorner(8, 8);
        m_vis.setRendererFactory(new DefaultRendererFactory(tr));

        // --------------------------------------------------------------------
        // register the data with a visualization
        
        // adds graph to visualization and sets renderer label field
        setGraph(g, label);
        
        // fix selected focus nodes
        TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS); 
        //TupleSet focusGroup = m_vis.getGroup(nodes);
        focusGroup.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
            {
                for ( int i=0; i<rem.length; ++i )
                    ((VisualItem)rem[i]).setFixed(false);
                for ( int i=0; i<add.length; ++i ) {
                    ((VisualItem)add[i]).setFixed(false);
                    ((VisualItem)add[i]).setFixed(true);
                }
                if ( ts.getTupleCount() == 0 ) {
                    ts.addTuple(rem[0]);
                    ((VisualItem)rem[0]).setFixed(false);
                }
                m_vis.run("draw");
            }
        });
        
        // --------------------------------------------------------------------
        // create actions to process the visual data

        int hops = 30;
        final GraphDistanceFilter filter = new GraphDistanceFilter(graph, nodes, hops);

        ColorAction fill = new ColorAction(nodes, 
                VisualItem.FILLCOLOR, ColorLib.rgb(200,200,255));
        fill.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
        
        ActionList draw = new ActionList();
        draw.add(filter);
        draw.add(fill);
        draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
        draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));
        draw.add(new ColorAction(VieprotLib.Constants.INTERNAL_EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
        draw.add(new ColorAction(VieprotLib.Constants.INTERNAL_EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));
        draw.add(new ColorAction(VieprotLib.Constants.ALIGNED_EDGES, VisualItem.FILLCOLOR, ColorLib.rgba(200,0,0,30)));
        draw.add(new ColorAction(VieprotLib.Constants.ALIGNED_EDGES, VisualItem.STROKECOLOR, ColorLib.rgba(200,0,0,30)));
        draw.add(new SizeAction(nodes));
        
        ActionList animate = new ActionList(Activity.INFINITY);
        
        ForceDirectedLayout internalEdgeLayout = new ForceDirectedLayout(graph, true);
        internalEdgeLayout.setDataGroups(nodes, VieprotLib.Constants.INTERNAL_EDGES);
        animate.add(internalEdgeLayout);

        // For later: add lighter forces to the aligned edges.
        ForceDirectedLayout alignedEdgeLayout = new ForceDirectedLayout(graph, true);
        alignedEdgeLayout.setDataGroups(nodes, VieprotLib.Constants.ALIGNED_EDGES);
        ForceSimulator alignedEdgeForces = alignedEdgeLayout.getForceSimulator();
        
        //animate.add(new ForceDirectedLayout(graph, true));
        animate.add(fill);
        animate.add(new RepaintAction());
        
        // finally, we register our ActionList with the Visualization.
        // we can later execute our Actions by invoking a method on our
        // Visualization, using the name we've chosen below.
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);

        m_vis.runAfter("draw", "layout");
        
        // --------------------------------------------------------------------
        // set up a display to show the visualization
        Display display = new Display(m_vis);
        display.setSize(700,700);
        display.pan(350, 350);
        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);
        
        // main display controls
        display.addControlListener(new FocusControl(1));
        display.addControlListener(new DragControl());
        display.addControlListener(new PanControl());
        display.addControlListener(new ZoomControl());
        display.addControlListener(new WheelZoomControl());
        display.addControlListener(new ZoomToFitControl());
        display.addControlListener(new NeighborHighlightControl());
        display.addControlListener(new ToolTipControl("weight"));
        
        // --------------------------------------------------------------------        
        // launch the visualization
        
        /*
         * create a panel for editing force values
         */
        ForceSimulator fsim = ((ForceDirectedLayout)animate.get(0)).getForceSimulator();
        JForcePanel fpanel = new JForcePanel(fsim);
        
        final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                filter.setDistance(slider.getValue().intValue());
                m_vis.run("draw");
            }
        });
        slider.setBackground(Color.WHITE);
        slider.setPreferredSize(new Dimension(300,30));
        slider.setMaximumSize(new Dimension(300,30));
        
        Box cf = new Box(BoxLayout.Y_AXIS);
        cf.add(slider);
        cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
        fpanel.add(cf);
        
        /*
         * create a radio button group to test module visibility filter
         */
        //Create the radio buttons.
        JRadioButton module0Button = new JRadioButton("m0");
        module0Button.setActionCommand("m0");

        JRadioButton module1Button = new JRadioButton("m1");
        module1Button.setActionCommand("m1");

        JRadioButton bothButton = new JRadioButton("both");
        bothButton.setActionCommand("both");
        bothButton.setSelected(true);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(module0Button);
        group.add(module1Button);
        group.add(bothButton);
        
        module0Button.addActionListener(this);
        module1Button.addActionListener(this);
        bothButton.addActionListener(this);
        
        //Put the radio buttons in a row in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(1,0));
        radioPanel.add(module0Button);
        radioPanel.add(module1Button);
        radioPanel.add(bothButton);
        radioPanel.setBackground(Color.WHITE);
        radioPanel.setPreferredSize(new Dimension(300,30));
        radioPanel.setMaximumSize(new Dimension(300,30));
        
        Box bgbox = new Box(BoxLayout.Y_AXIS);
        bgbox.add(radioPanel);
        bgbox.setBorder(BorderFactory.createTitledBorder("Module visibility filter"));
        fpanel.add(bgbox);
        
        /*
         * create a check box for aligned edges
         */
        alignedEdges = new JCheckBox("Show aligned edges");
        alignedEdges.setSelected(true);
        alignedEdges.setActionCommand("aligned");
        alignedEdges.addActionListener(this);
        
        JPanel checkPanel = new JPanel(new GridLayout(1,0));
        checkPanel.add(alignedEdges);
        checkPanel.setBackground(Color.WHITE);
        checkPanel.setPreferredSize(new Dimension(300,30));
        checkPanel.setMaximumSize(new Dimension(300,30));
        
        Box aebox = new Box(BoxLayout.Y_AXIS);
        aebox.add(checkPanel);
        aebox.setBorder(BorderFactory.createTitledBorder("Show aligned edges"));
        fpanel.add(aebox);
        
        // Create the JList with all the conserved modules
        DefaultListModel graphIDs= new DefaultListModel();
        //listModel.addElement("Debbie Scott");
        //listModel.addElement("Scott Hommel");
        //listModel.addElement("Sharon Zakhour");
        JList modules = new JList();
        
        
        JScrollPane moduleScrollPanel = new JScrollPane(modules);
        
        // create a new JSplitPane to present the interface
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(display);
        split.setRightComponent(fpanel);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(false);
        split.setDividerLocation(700);
        
        // now we run our action list
        m_vis.run("draw");
        
        add(split);
    }
    
    public void setGraph(Graph g, String label) {
        // update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory)
                                                m_vis.getRendererFactory();
        ((LabelRenderer)drf.getDefaultRenderer()).setTextField(label);
        
        // update graph
        m_vis.removeGroup(graph);
        VisualGraph vg = m_vis.addGraph(graph, g);
        
        /*
         * Initalize the groups
         */
        
        // Aligned edge group
        m_vis.removeGroup(VieprotLib.Constants.ALIGNED_EDGES);
        m_vis.addFocusGroup(VieprotLib.Constants.ALIGNED_EDGES);
        TupleSet ts = m_vis.getFocusGroup(VieprotLib.Constants.ALIGNED_EDGES);
		Iterator alignedEdgeTuples = m_vis.items(getVieprotGroup(VieprotLib.Constants.ALIGNED_EDGES));
		while(alignedEdgeTuples.hasNext()) {
			ts.addTuple((Tuple)alignedEdgeTuples.next());
		}
		
		// Internal edge group
		m_vis.removeGroup(VieprotLib.Constants.INTERNAL_EDGES);
		m_vis.addFocusGroup(VieprotLib.Constants.INTERNAL_EDGES);
		TupleSet internalEdges = m_vis.getFocusGroup(VieprotLib.Constants.INTERNAL_EDGES);
		Iterator internalEdgeTuples = getInternalEdges();
		while(internalEdgeTuples.hasNext()) {
			internalEdges.addTuple((Tuple)internalEdgeTuples.next());
		}

        //m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
        VisualItem f = (VisualItem)vg.getNode(0);
        m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
        f.setFixed(false);
    }
 
    // ------------------------------------------------------------------------
    // Event listeners
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//m_vis.getGroup(Visualization.FOCUS_ITEMS).clear();
		//TupleSet ts = m_vis.getVisualGroup("graph.nodes");
		TupleSet ts = m_vis.getVisualGroup(graph);
		// TODO Auto-generated method stub
		if(e.getActionCommand() == "m0") {
			setAllVisible(false);
			Predicate cp = getVieprotGroup(VieprotLib.Constants.MODULE_0);
			setVisible(ts,cp,true);
			
			alignedEdges.setEnabled(false);
		}
		else if(e.getActionCommand() == "m1") {
			setAllVisible(false);
			Predicate cp = getVieprotGroup(VieprotLib.Constants.MODULE_1);
			setVisible(ts,cp,true);
			
			alignedEdges.setEnabled(false);
		}
		else if(e.getActionCommand() == "both") {
			alignedEdges.setEnabled(true);
			alignedEdges.setSelected(true);
			setAllVisible(true);
		}
		else if(e.getActionCommand() == "aligned") {
			Predicate cp = getVieprotGroup(VieprotLib.Constants.ALIGNED_EDGES);
			
			JCheckBox jcb = (JCheckBox)e.getSource();
			if(jcb.isSelected()) {
				System.out.println("selected");
				setVisible(m_vis.getVisualGroup(edges),cp,true);
			}
			else {
				System.out.println("deselected");
				setVisible(m_vis.getVisualGroup(edges),cp,false);
			}
		}
	}
    
	private Predicate getVieprotGroup(String s) {
		ColumnExpression ce = new ColumnExpression("group");
		ComparisonPredicate cp = new ComparisonPredicate(ComparisonPredicate.EQ, ce, Literal.getLiteral(s, String.class));
		return cp;
	}
	
	private Iterator getAlignedEdges() {
		return (Iterator)m_vis.getGroup(graph).tuples(getVieprotGroup(VieprotLib.Constants.ALIGNED_EDGES));
	}
	
	private Iterator getInternalEdges() {
		OrPredicate ap = new OrPredicate(getVieprotGroup(VieprotLib.Constants.MODULE_0), getVieprotGroup(VieprotLib.Constants.MODULE_1));
		return (Iterator)m_vis.getGroup(edges).tuples(ap);
	}
	
	private void setAllVisible(boolean visible) {
		Iterator visibleItems = m_vis.getGroup(graph).tuples();
		while(visibleItems.hasNext()) {
			VisualItem vi = (VisualItem)(visibleItems.next());
			vi.setVisible(visible);
		}
	}
	
	private void setVisible(TupleSet ts, Predicate p, boolean visible) {
		Iterator newTuples;
		if(p != null) newTuples = ts.tuples(p);
		else newTuples = ts.tuples();
		while(newTuples.hasNext()) {
			VisualItem vi = (VisualItem)newTuples.next();
			vi.setVisible(visible);
		}
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
    }
    
    public static JFrame demo() {
        return demo((String)null, "label");
    }
    
    public static JFrame demo(String datafile, String label) {
        Graph g = null;
        if ( datafile == null ) {
            g = GraphLib.getGrid(15,15);
            label = "label";
        } else {
            try {
                GraphCollection gc = new GraphCollectionMLReader().readGraphCollection(datafile);
            	label = "id";
            	g = gc.getGraph(0);
                printGraphInfo(gc.getGraph(0));
            } catch ( Exception e ) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return demo(g, label);
    }
    
    public static void printGraphInfo(Graph g) {
    	System.out.println(g.getNodeTable().getSchema().toString());
    	System.out.println(g.getEdgeTable().getSchema().toString());
    	System.out.format("# nodes: %d | # edges: %d\n", g.getNodeCount(), g.getEdgeCount());
    }
    
    public static JFrame demo(Graph g, String label) {
        final ModuleViewer view = new ModuleViewer(g, label);
        
        // launch window
        JFrame frame = new JFrame("v i e p r o t");
        frame.setContentPane(view);
        frame.pack();
        frame.setVisible(true);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                view.m_vis.run("layout");
            }
            public void windowDeactivated(WindowEvent e) {
                view.m_vis.cancel("layout");
            }
        });
        
        return frame;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Swing menu action that loads a graph into the graph viewer.
     */
    public abstract static class GraphMenuAction extends AbstractAction {
        private ModuleViewer m_view;
        public GraphMenuAction(String name, String accel, ModuleViewer view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, name);
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                          KeyStroke.getKeyStroke(accel));
        }
        public void actionPerformed(ActionEvent e) {
            m_view.setGraph(getGraph(), "label");
        }
        protected abstract Graph getGraph();
    }
    
    public static class OpenGraphAction extends AbstractAction {
        private ModuleViewer m_view;

        public OpenGraphAction(ModuleViewer view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, "Open File...");
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                          KeyStroke.getKeyStroke("ctrl O"));
        }
        public void actionPerformed(ActionEvent e) {
            Graph g = IOLib.getGraphFile(m_view);
            if ( g == null ) return;
            String label = getLabel(m_view, g);
            if ( label != null ) {
                m_view.setGraph(g, label);
            }
        }
        public static String getLabel(Component c, Graph g) {
            // get the column names
            Table t = g.getNodeTable();
            int  cc = t.getColumnCount();
            String[] names = new String[cc];
            for ( int i=0; i<cc; ++i )
                names[i] = t.getColumnName(i);
            
            // where to store the result
            final String[] label = new String[1];

            // -- build the dialog -----
            // we need to get the enclosing frame first
            while ( c != null && !(c instanceof JFrame) ) {
                c = c.getParent();
            }
            final JDialog dialog = new JDialog(
                    (JFrame)c, "Choose Label Field", true);
            
            // create the ok/cancel buttons
            final JButton ok = new JButton("OK");
            ok.setEnabled(false);
            ok.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   dialog.setVisible(false);
               }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    label[0] = null;
                    dialog.setVisible(false);
                }
            });
            
            // build the selection list
            final JList list = new JList(names);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int sel = list.getSelectedIndex(); 
                    if ( sel >= 0 ) {
                        ok.setEnabled(true);
                        label[0] = (String)list.getModel().getElementAt(sel);
                    } else {
                        ok.setEnabled(false);
                        label[0] = null;
                    }
                }
            });
            JScrollPane scrollList = new JScrollPane(list);
            
            JLabel title = new JLabel("Choose a field to use for node labels:");
            
            // layout the buttons
            Box bbox = new Box(BoxLayout.X_AXIS);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(Box.createHorizontalGlue());
            bbox.add(ok);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(cancel);
            bbox.add(Box.createHorizontalStrut(5));
            
            // put everything into a panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(title, BorderLayout.NORTH);
            panel.add(scrollList, BorderLayout.CENTER);
            panel.add(bbox, BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));
            
            // show the dialog
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(c);
            dialog.setVisible(true);
            dialog.dispose();
            
            // return the label field selection
            return label[0];
        }
    }
    
    public static class FitOverviewListener implements ItemBoundsListener {
        private Rectangle2D m_bounds = new Rectangle2D.Double();
        private Rectangle2D m_temp = new Rectangle2D.Double();
        private double m_d = 15;
        public void itemBoundsChanged(Display d) {
            d.getItemBounds(m_temp);
            GraphicsLib.expand(m_temp, 25/d.getScale());
            
            double dd = m_d/d.getScale();
            double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
            double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
            double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
            double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
            if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
                m_bounds.setFrame(m_temp);	
                DisplayLib.fitViewToBounds(d, m_bounds, 0);
            }
        }
    }
    
} // end of class Viewer
