package cn.com.yusys.ide.flow.view.core;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

import cn.com.yusys.ide.flow.view.core.custom.CustomGraph;
import cn.com.yusys.ide.flow.view.core.custom.Node;

public class BasicGraphEditor extends JPanel {

	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * 画板
	 */
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
	
	final mxGraph graph;

	public BasicGraphEditor(String appTitle, mxGraphComponent component) {
		mxResources.add("cn/com/yusys/ide/flow/other/resources/editor");

		this.appTitle = appTitle;
		updateTitle();
		graphComponent = component;
		graph = graphComponent.getGraph();
		graph.setAllowDanglingEdges(false);// 禁止线拖动
		graph.setCellsResizable(false);// 禁止元素被改变大小
		graph.setEdgeLabelsMovable(false);// 禁止线上的lable被移动		
		
		// Do not change the scale and translation after files have been loaded
		graph.setResetViewOnRootChange(false);

		// Updates the modified flag if the graph model changes
		mxIEventListener changeTracker = new mxIEventListener() {
			public void invoke(Object source, mxEventObject evt) {
				System.out.println("graph.CHANGE");
				setModified(true);
			}
		};
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		mxIEventListener undoHandler1 = new mxIEventListener() {
			public void invoke(Object source, mxEventObject evt) {
				System.out.println("graph.undoHandler");
				undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
			}
		};
		graph.getModel().addListener(mxEvent.UNDO, undoHandler1);
		graph.getView().addListener(mxEvent.UNDO, undoHandler1);

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener() {
			public void invoke(Object source, mxEventObject evt) {
				System.out.println("undoManager.undoHandler");
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph.getSelectionCellsForChanges(changes));
			}
		};
		undoManager = new mxUndoManager();// 撤销操作
		undoManager.addListener(mxEvent.UNDO, undoHandler);// 画板编辑回退操作
		undoManager.addListener(mxEvent.REDO, undoHandler);

		// Creates the graph outline component
		graphOutline = new mxGraphOutline(graphComponent);

		// Creates the library pane that contains the tabs with the palettes
		libraryPane = new JTabbedPane();

		// 图形界面含左侧拖拽区和鹰眼
		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT, libraryPane, graphOutline);
		inner.setDividerLocation(320);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		JTabbedPane properityPane = new JTabbedPane();
		
		JSplitPane outer0 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphComponent,properityPane);
		outer0.setOneTouchExpandable(true);
		outer0.setDividerLocation(600);
		outer0.setDividerSize(10);
		outer0.setBorder(null);
		
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inner, outer0);
		outer.setOneTouchExpandable(true);
		outer.setDividerLocation(150);
		outer.setDividerSize(10);
		outer.setBorder(null);

		// 最下方横线
		statusBar = new JLabel(mxResources.get("ready"));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		// 面板重绘制时触发
		graphComponent.getGraph().addListener(mxEvent.REPAINT, new mxIEventListener() {
			public void invoke(Object source, mxEventObject evt) {
				System.out.println("installRepaintListener");
				String buffer = (graphComponent.getTripleBuffer() != null) ? "" : " (unbuffered)";
				mxRectangle dirty = (mxRectangle) evt.getProperty("region");

				if (dirty == null) {
					status("Repaint all" + buffer);
				} else {
					status("Repaint: x=" + (int) (dirty.getX()) + " y=" + (int) (dirty.getY()) + " w="
							+ (int) (dirty.getWidth()) + " h=" + (int) (dirty.getHeight()) + buffer);
				}
			}
		});
		// 选中节点时触发
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{	
			public void mouseReleased(MouseEvent e)
			{
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				if(cell instanceof mxCell){
					mxCell cellT = (mxCell) cell;
					Object value = cellT.getValue();
					System.out.println(value.getClass());

				}
				if (cell != null)
				{
					System.out.println("cell="+graph.getLabel(cell));
				}
			}
		});

		setLayout(new BorderLayout());
		add(new EditorToolBar(this, JToolBar.HORIZONTAL), BorderLayout.NORTH);// 上方工具栏
		add(outer, BorderLayout.CENTER);// 绘图区
		add(statusBar, BorderLayout.SOUTH);

		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();
		updateTitle();
				
		// Creates the shapes palette
		EditorPalette imagesPalette = insertPalette(mxResources.get("images"));
		imagesPalette.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");
				System.out.println("HHHH");
				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];

					if (graph.getModel().isEdge(cell)) {
						((CustomGraph) graph).setEdgeTemplate(cell);
					}
				}
			}

		});
		Node node = new Node();
		node.setClassName("中文");
		node.setMathName("bbb");
		imagesPalette.addTemplate("pt",
				new ImageIcon(this.getClass().getResource("/cn/com/yusys/ide/flow/other/images/bell.png")),
				"image;image=/cn/com/yusys/ide/flow/other/images/bell.png", 50, 50, node);
		imagesPalette.addTemplate("Box",
				new ImageIcon(this.getClass().getResource("/cn/com/yusys/ide/flow/other/images/box.png")),
				"image;image=/cn/com/yusys/ide/flow/other/images/box.png", 50, 50, "Box");			
	}

	/**
	 * 
	 */
	protected void installHandlers() {
		rubberband = new mxRubberband(graphComponent);
		keyboardHandler = new EditorKeyboardHandler(graphComponent);
	}

	/**
	 * 
	 */
	public EditorPalette insertPalette(String title) {
		final EditorPalette palette = new EditorPalette();// 可拖拽区
		final JScrollPane scrollPane = new JScrollPane(palette);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		libraryPane.add(title, scrollPane);

		// Updates the widths of the palettes if the container size changes
		libraryPane.addComponentListener(new ComponentAdapter() {
			
			public void componentResized(ComponentEvent e) {
				int w = scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth();
				palette.setPreferredWidth(w);
			}

		});

		return palette;
	}
	
	protected void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) {
			graphComponent.zoomIn();
		} else {
			graphComponent.zoomOut();
		}

		status(mxResources.get("scale") + ": " + (int) (100 * graphComponent.getGraph().getView().getScale()) + "%");
	}

	protected void showOutlinePopupMenu(MouseEvent e) {
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(mxResources.get("magnifyPage"));
		item.setSelected(graphOutline.isFitPage());

		item.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				graphOutline.setFitPage(!graphOutline.isFitPage());
				graphOutline.repaint();
			}
		});

		JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(mxResources.get("showLabels"));
		item2.setSelected(graphOutline.isDrawLabels());

		item2.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				graphOutline.setDrawLabels(!graphOutline.isDrawLabels());
				graphOutline.repaint();
			}
		});

		JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(mxResources.get("buffering"));
		item3.setSelected(graphOutline.isTripleBuffered());

		item3.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				graphOutline.setTripleBuffered(!graphOutline.isTripleBuffered());
				graphOutline.repaint();
			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.add(item);
		menu.add(item2);
		menu.add(item3);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}

	/**
	 * 
	 */
	protected void mouseLocationChanged(MouseEvent e) {
		status(e.getX() + ", " + e.getY());
	}

	/**
	 * 
	 */
	protected void installListeners() {
		// Installs mouse wheel listener for zooming
		MouseWheelListener wheelTracker = new MouseWheelListener() {
			/**
			 * 
			 */
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getSource() instanceof mxGraphOutline || e.isControlDown()) {
					BasicGraphEditor.this.mouseWheelMoved(e);
				}
			}

		};

		// Handles mouse wheel events in the outline and graph component
		graphOutline.addMouseWheelListener(wheelTracker);
		graphComponent.addMouseWheelListener(wheelTracker);

		// Installs the popup menu in the outline
		graphOutline.addMouseListener(new MouseAdapter() {

			/**
			 * 
			 */
			public void mousePressed(MouseEvent e) {
				// Handles context menu on the Mac where the trigger is on
				// mousepressed
				mouseReleased(e);
			}

			/**
			 * 
			 */
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showOutlinePopupMenu(e);
				}
			}

		});

		// Installs the popup menu in the graph component
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				// Handles context menu on the Mac where the trigger is on
				// mousepressed
				System.out.println("mousePressed");
				mouseReleased(e);
			}

			
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					System.out.println("mouseReleased");
				}
			}

		});

		// Installs a mouse motion listener to display the mouse location
		graphComponent.getGraphControl().addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				//System.out.println("mouseDragged");
				mouseLocationChanged(e);
			}

			public void mouseMoved(MouseEvent e) {
				//System.out.println("mouseMoved");
				mouseDragged(e);
			}

		});
	}

	/**
	 * 
	 */
	public void setCurrentFile(File file) {
		File oldValue = currentFile;
		currentFile = file;

		firePropertyChange("currentFile", oldValue, file);

		if (oldValue != file) {
			updateTitle();
		}
	}

	/**
	 * 
	 */
	public File getCurrentFile() {
		return currentFile;
	}

	/**
	 * 将文件设置为编辑状态
	 * 
	 * @param modified
	 */
	public void setModified(boolean modified) {
		boolean oldValue = this.modified;
		this.modified = modified;

		firePropertyChange("modified", oldValue, modified);

		if (oldValue != modified) {
			updateTitle();
		}
	}

	/**
	 * 
	 * @return whether or not the current graph has been modified
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * 
	 */
	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	/**
	 * 
	 */
	public mxGraphOutline getGraphOutline() {
		return graphOutline;
	}

	/**
	 * 
	 */
	public JTabbedPane getLibraryPane() {
		return libraryPane;
	}

	/**
	 * 
	 */
	public mxUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name
	 */
	public Action bind(String name, final Action action) {
		return bind(name, action, null);
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name and icon
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl) {
		AbstractAction newAction = new AbstractAction(name,
				(iconUrl != null) ? new ImageIcon(BasicGraphEditor.class.getResource(iconUrl)) : null) {
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(new ActionEvent(getGraphComponent(), e.getID(), e.getActionCommand()));
			}
		};

		newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));

		return newAction;
	}

	/**
	 * 
	 * @param msg
	 */
	public void status(String msg) {
		statusBar.setText(msg);
	}

	/**
	 * 更新标题
	 */
	public void updateTitle() {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		if (frame != null) {
			String title = (currentFile != null) ? currentFile.getAbsolutePath() : mxResources.get("newDiagram");
			if (modified) {
				title += "*";
			}
			frame.setTitle(title + " - " + appTitle);
		}
	}

	/**
	 * 退出按钮操作
	 */
	public void exit() {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		if (frame != null) {
			frame.dispose();
		}
	}

	/**
	 * 
	 */
	public void setLookAndFeel(String clazz) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			try {
				UIManager.setLookAndFeel(clazz);
				SwingUtilities.updateComponentTreeUI(frame);

				// Needs to assign the key bindings again
				//keyboardHandler = new EditorKeyboardHandler(graphComponent);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}


	/**
	 * Creates an action that executes the specified layout.
	 * 
	 * @param key
	 *            Key to be used for getting the label from mxResources and also
	 *            to create the layout instance for the commercial graph editor
	 *            example.
	 * @return an action that executes the specified layout
	 */
	@SuppressWarnings("serial")
	public Action graphLayout(final String key, boolean animate) {
		final mxIGraphLayout layout = createLayout(key, animate);
		if (layout != null) {
			return new AbstractAction(mxResources.get(key)) {
				public void actionPerformed(ActionEvent e) {
					final mxGraph graph = graphComponent.getGraph();
					Object cell = graph.getSelectionCell();

					if (cell == null || graph.getModel().getChildCount(cell) == 0) {
						cell = graph.getDefaultParent();
					}

					graph.getModel().beginUpdate();
					try {
						long t0 = System.currentTimeMillis();
						layout.execute(cell);
						status("Layout: " + (System.currentTimeMillis() - t0) + " ms");
					} finally {
						mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

						morph.addListener(mxEvent.DONE, new mxIEventListener() {

							public void invoke(Object sender, mxEventObject evt) {
								graph.getModel().endUpdate();
							}

						});

						morph.startAnimation();
					}

				}

			};
		} else {
			return new AbstractAction(mxResources.get(key)) {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noLayout"));
				}

			};
		}
	}

	/**
	 * Creates a layout instance for the given identifier.
	 */
	protected mxIGraphLayout createLayout(String ident, boolean animate) {
		mxIGraphLayout layout = null;

		if (ident != null) {
			mxGraph graph = graphComponent.getGraph();

			if (ident.equals("verticalHierarchical")) {
				layout = new mxHierarchicalLayout(graph);
			} else if (ident.equals("horizontalHierarchical")) {
				layout = new mxHierarchicalLayout(graph, JLabel.WEST);
			} else if (ident.equals("verticalTree")) {
				layout = new mxCompactTreeLayout(graph, false);
			} else if (ident.equals("horizontalTree")) {
				layout = new mxCompactTreeLayout(graph, true);
			} else if (ident.equals("parallelEdges")) {
				layout = new mxParallelEdgeLayout(graph);
			} else if (ident.equals("placeEdgeLabels")) {
				layout = new mxEdgeLabelLayout(graph);
			} else if (ident.equals("organicLayout")) {
				layout = new mxOrganicLayout(graph);
			}
			if (ident.equals("verticalPartition")) {
				layout = new mxPartitionLayout(graph, false) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return graphComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("horizontalPartition")) {
				layout = new mxPartitionLayout(graph, true) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return graphComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("verticalStack")) {
				layout = new mxStackLayout(graph, false) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return graphComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("horizontalStack")) {
				layout = new mxStackLayout(graph, true) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return graphComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("circleLayout")) {
				layout = new mxCircleLayout(graph);
			}
		}

		return layout;
	}

}
