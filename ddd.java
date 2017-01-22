 package com.mxgraph.examples.swing.editor;
 
 import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.view.mxGraph;
 
 public class BasicGraphEditor extends JPanel
 {
   private static final long serialVersionUID = -6561623072112577140L;
   protected mxGraphComponent graphComponent;
   protected mxGraphOutline graphOutline;
   protected JTabbedPane libraryPane;
   protected mxUndoManager undoManager;
   protected String appTitle;
   protected JLabel statusBar;
   protected File currentFile;
   protected boolean modified = false;
   protected mxRubberband rubberband;
   protected mxKeyboardHandler keyboardHandler;
   protected mxEventSource.mxIEventListener undoHandler = new mxEventSource.mxIEventListener() {
	
	public void invoke(Object paramObject, mxEventObject evt) {
		undoManager.undoableEditHappened((mxUndoableEdit)
			       evt.getProperty("edit"));
		
	}
};
 
   protected mxEventSource.mxIEventListener changeTracker = new mxEventSource.mxIEventListener() {
	
	public void invoke(Object paramObject, mxEventObject parammxEventObject) {
		setModified(true);
		
	}
};
 
   static
   {
     try
     {
       mxResources.add("com/mxgraph/examples/swing/resources/editor");
     }
     catch (Exception localException)
     {
     }
   }
 
   public BasicGraphEditor(String appTitle, mxGraphComponent component)
   {
     this.appTitle = appTitle;
 
     this.graphComponent = component;
     mxGraph graph = this.graphComponent.getGraph();
     this.undoManager = createUndoManager();
 
     graph.setResetViewOnRootChange(false);
 
     graph.getModel().addListener("change", this.changeTracker);
 
     graph.getModel().addListener("undo", this.undoHandler);
     graph.getView().addListener("undo", this.undoHandler);
 
     mxEventSource.mxIEventListener undoHandler = new BasicGraphEditor.3(this, graph);
 
     this.undoManager.addListener("undo", undoHandler);
     this.undoManager.addListener("redo", undoHandler);
 
     this.graphOutline = new mxGraphOutline(this.graphComponent);
 
     this.libraryPane = new JTabbedPane();
 
     JSplitPane inner = new JSplitPane(0, 
       this.libraryPane, this.graphOutline);
     inner.setDividerLocation(320);
     inner.setResizeWeight(1.0D);
     inner.setDividerSize(6);
     inner.setBorder(null);
 
     JSplitPane outer = new JSplitPane(1, inner, 
       this.graphComponent);
     outer.setOneTouchExpandable(true);
     outer.setDividerLocation(200);
     outer.setDividerSize(6);
     outer.setBorder(null);
 
     this.statusBar = createStatusBar();
 
     installRepaintListener();
 
     setLayout(new BorderLayout());
     add(outer, "Center");
     add(this.statusBar, "South");
     installToolBar();
 
     installHandlers();
     installListeners();
     updateTitle();
   }
 
   protected mxUndoManager createUndoManager()
   {
     return new mxUndoManager();
   }
 
   protected void installHandlers()
   {
     this.rubberband = new mxRubberband(this.graphComponent);
     this.keyboardHandler = new EditorKeyboardHandler(this.graphComponent);
   }
 
   protected void installToolBar()
   {
     add(new EditorToolBar(this, 0), "North");
   }
 
   protected JLabel createStatusBar()
   {
     JLabel statusBar = new JLabel(mxResources.get("ready"));
     statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
 
     return statusBar;
   }
 
   protected void installRepaintListener()
   {
     this.graphComponent.getGraph().addListener("repaint", 
       new BasicGraphEditor.4(this));
   }
 
   public EditorPalette insertPalette(String title)
   {
     EditorPalette palette = new EditorPalette();
     JScrollPane scrollPane = new JScrollPane(palette);
     scrollPane
       .setVerticalScrollBarPolicy(22);
     scrollPane
       .setHorizontalScrollBarPolicy(31);
     this.libraryPane.add(title, scrollPane);
 
     this.libraryPane.addComponentListener(new BasicGraphEditor.5(this, scrollPane, palette));
 
     return palette;
   }
 
   protected void mouseWheelMoved(MouseWheelEvent e)
   {
     if (e.getWheelRotation() < 0)
     {
       this.graphComponent.zoomIn();
     }
     else
     {
       this.graphComponent.zoomOut();
     }
 
     status(mxResources.get("scale") + ": " + 
       (int)(100.0D * this.graphComponent.getGraph().getView().getScale()) + 
       "%");
   }
 
   protected void showOutlinePopupMenu(MouseEvent e)
   {
     Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), 
       this.graphComponent);
     JCheckBoxMenuItem item = new JCheckBoxMenuItem(
       mxResources.get("magnifyPage"));
     item.setSelected(this.graphOutline.isFitPage());
 
     item.addActionListener(new BasicGraphEditor.6(this));
 
     JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(
       mxResources.get("showLabels"));
     item2.setSelected(this.graphOutline.isDrawLabels());
 
     item2.addActionListener(new BasicGraphEditor.7(this));
 
     JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(
       mxResources.get("buffering"));
     item3.setSelected(this.graphOutline.isTripleBuffered());
 
     item3.addActionListener(new BasicGraphEditor.8(this));
 
     JPopupMenu menu = new JPopupMenu();
     menu.add(item);
     menu.add(item2);
     menu.add(item3);
     menu.show(this.graphComponent, pt.x, pt.y);
 
     e.consume();
   }
 
   protected void showGraphPopupMenu(MouseEvent e)
   {
     Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), 
       this.graphComponent);
     EditorPopupMenu menu = new EditorPopupMenu(this);
     menu.show(this.graphComponent, pt.x, pt.y);
 
     e.consume();
   }
 
   protected void mouseLocationChanged(MouseEvent e)
   {
     status(e.getX() + ", " + e.getY());
   }
 
   protected void installListeners()
   {
     MouseWheelListener wheelTracker = new BasicGraphEditor.9(this);
 
     this.graphOutline.addMouseWheelListener(wheelTracker);
     this.graphComponent.addMouseWheelListener(wheelTracker);
 
     this.graphOutline.addMouseListener(new BasicGraphEditor.10(this));
 
     this.graphComponent.getGraphControl().addMouseListener(new BasicGraphEditor.11(this));
 
     this.graphComponent.getGraphControl().addMouseMotionListener(
       new BasicGraphEditor.12(this));
   }
 
   public void setCurrentFile(File file)
   {
     File oldValue = this.currentFile;
     this.currentFile = file;
 
     firePropertyChange("currentFile", oldValue, file);
 
     if (oldValue != file)
     {
       updateTitle();
     }
   }
 
   public File getCurrentFile()
   {
     return this.currentFile;
   }
 
   public void setModified(boolean modified)
   {
     boolean oldValue = this.modified;
     this.modified = modified;
 
     firePropertyChange("modified", oldValue, modified);
 
     if (oldValue != modified)
     {
       updateTitle();
     }
   }
 
   public boolean isModified()
   {
     return this.modified;
   }
 
   public mxGraphComponent getGraphComponent()
   {
     return this.graphComponent;
   }
 
   public mxGraphOutline getGraphOutline()
   {
     return this.graphOutline;
   }
 
   public JTabbedPane getLibraryPane()
   {
     return this.libraryPane;
   }
 
   public mxUndoManager getUndoManager()
   {
     return this.undoManager;
   }
 
   public Action bind(String name, Action action)
   {
     return bind(name, action, null);
   }
 
   public Action bind(String name, Action action, String iconUrl)
   {
     return new BasicGraphEditor.13(this, name, iconUrl != null ? 
       new ImageIcon(BasicGraphEditor.class.getResource(iconUrl)) : null, action);
   }
 
   public void status(String msg)
   {
     this.statusBar.setText(msg);
   }
 
   public void updateTitle()
   {
     JFrame frame = (JFrame)SwingUtilities.windowForComponent(this);
 
     if (frame != null)
     {
       String title = this.currentFile != null ? 
         this.currentFile.getAbsolutePath() : mxResources.get("newDiagram");
 
       if (this.modified)
       {
         title = title + "*";
       }
 
       frame.setTitle(title + " - " + this.appTitle);
     }
   }
 
   public void about()
   {
     JFrame frame = (JFrame)SwingUtilities.windowForComponent(this);
 
     if (frame != null)
     {
       EditorAboutFrame about = new EditorAboutFrame(frame);
       about.setModal(true);
 
       int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
       int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
       about.setLocation(x, y);
 
       about.setVisible(true);
     }
   }
 
   public void exit()
   {
     JFrame frame = (JFrame)SwingUtilities.windowForComponent(this);
 
     if (frame != null)
     {
       frame.dispose();
     }
   }
 
   public void setLookAndFeel(String clazz)
   {
     JFrame frame = (JFrame)SwingUtilities.windowForComponent(this);
 
     if (frame != null)
     {
       try
       {
         UIManager.setLookAndFeel(clazz);
         SwingUtilities.updateComponentTreeUI(frame);
 
         this.keyboardHandler = new EditorKeyboardHandler(this.graphComponent);
       }
       catch (Exception e1)
       {
         e1.printStackTrace();
       }
     }
   }
 
   public JFrame createFrame(JMenuBar menuBar)
   {
     JFrame frame = new JFrame();
     frame.getContentPane().add(this);
     frame.setDefaultCloseOperation(3);
     frame.setJMenuBar(menuBar);
     frame.setSize(870, 640);
 
     updateTitle();
 
     return frame;
   }
 
   public Action graphLayout(String key, boolean animate)
   {
     mxIGraphLayout layout = createLayout(key, animate);
 
     if (layout != null)
     {
       return new BasicGraphEditor.14(this, mxResources.get(key), layout);
     }
 
     return new BasicGraphEditor.15(this, mxResources.get(key));
   }
 
   protected mxIGraphLayout createLayout(String ident, boolean animate)
   {
     mxIGraphLayout layout = null;
 
     if (ident != null)
     {
       mxGraph graph = this.graphComponent.getGraph();
 
       if (ident.equals("verticalHierarchical"))
       {
         layout = new mxHierarchicalLayout(graph);
       }
       else if (ident.equals("horizontalHierarchical"))
       {
         layout = new mxHierarchicalLayout(graph, 7);
       }
       else if (ident.equals("verticalTree"))
       {
         layout = new mxCompactTreeLayout(graph, false);
       }
       else if (ident.equals("horizontalTree"))
       {
         layout = new mxCompactTreeLayout(graph, true);
       }
       else if (ident.equals("parallelEdges"))
       {
         layout = new mxParallelEdgeLayout(graph);
       }
       else if (ident.equals("placeEdgeLabels"))
       {
         layout = new mxEdgeLabelLayout(graph);
       }
       else if (ident.equals("organicLayout"))
       {
         layout = new mxOrganicLayout(graph);
       }
       if (ident.equals("verticalPartition"))
       {
         layout = new BasicGraphEditor.16(this, graph, false);
       }
       else if (ident.equals("horizontalPartition"))
       {
         layout = new BasicGraphEditor.17(this, graph, true);
       }
       else if (ident.equals("verticalStack"))
       {
         layout = new BasicGraphEditor.18(this, graph, false);
       }
       else if (ident.equals("horizontalStack"))
       {
         layout = new BasicGraphEditor.19(this, graph, true);
       }
       else if (ident.equals("circleLayout"))
       {
         layout = new mxCircleLayout(graph);
       }
     }
 
     return layout;
   }
 }

/* Location:           I:\鏂板缓鏂囦欢澶筡flow-cw.zip
 * Qualified Name:     flow-cw.WEB-INF.classes.com.mxgraph.examples.swing.editor.BasicGraphEditor
 * JD-Core Version:    0.6.0
 */
