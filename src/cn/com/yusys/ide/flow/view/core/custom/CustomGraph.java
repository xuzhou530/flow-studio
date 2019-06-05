package cn.com.yusys.ide.flow.view.core.custom;

import java.text.NumberFormat;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge.
 */
public class CustomGraph extends mxGraph {
	
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	
	/**
	 * Holds the edge to be used as a template for inserting new edges.
	 */
	protected Object edgeTemplate;
	
	

	/**
	 * Custom graph that defines the alternate edge style to be used when
	 * the middle control point of edges is double clicked (flipped).
	 */
	public CustomGraph() {
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
	}

	/**
	 * Sets the edge template to be used to inserting edges.
	 */
	public void setEdgeTemplate(Object template) {
		edgeTemplate = template;
	}

	/**
	 * 画板开启了提示，会回调此方法，将元素的基本信息展示出来
	 */
	public String getToolTipForCell(Object cell) {
		System.out.println("getToolTipForCell");
		String tip ="";
		if (getModel().isEdge(cell)) {
			
		} else {
			if(cell instanceof mxCell){
				mxCell cellT = (mxCell) cell;
				Object value = cellT.getValue();
				if(value instanceof Node){
					Node node = (Node)value;
					tip = node.getClassName()+"."+node.getMathName();
				}
				System.out.println(value.getClass());				
			}
		}
		return tip;
	}

	/**
	 * Overrides the method to use the currently selected edge template for
	 * new edges.
	 * 
	 * @param graph
	 * @param parent
	 * @param id
	 * @param value
	 * @param source
	 * @param target
	 * @param style
	 * @return
	 */
	public Object createEdge(Object parent, String id, Object value, Object source, Object target, String style) {
		System.out.println(value);
		Route route = new Route();
		value = route;
		System.out.println("createEdge");
		if (edgeTemplate != null) {
			mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
			edge.setId(id);

			return edge;
		}
		return super.createEdge(parent, id, value, source, target, style);
	}

}