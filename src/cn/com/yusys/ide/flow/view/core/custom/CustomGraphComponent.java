package cn.com.yusys.ide.flow.view.core.custom;

import java.awt.Color;
import java.awt.Point;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

/**
 * ����
 */
public class CustomGraphComponent extends mxGraphComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6833603133512882012L;

	/**
	 * 
	 * @param graph
	 */
	public CustomGraphComponent(mxGraph graph) {
		super(graph);

		// Sets switches typically used in an editor
		//setPageVisible(true); //���û�����Ƿ���ҳ��
		setGridVisible(false);// ���û����Ƿ�������
		setToolTips(true);  // �Ƿ�Ի����е�Ԫ�ؿ�����ʾ
		getConnectionHandler().setCreateTarget(false);// ����û��Ŀ��ʱ�Ƿ�����Ŀ��

		// Loads the defalt stylesheet from an external file
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(
				this.getClass().getResource("/cn/com/yusys/ide/flow/other/resources/default-style.xml").toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		
		getViewport().setOpaque(true);
		getViewport().setBackground(Color.WHITE);// ���û������ɫ
	}

	/**
	 * Overrides drop behaviour to set the cell style if the target is not a
	 * valid drop target and the cells are of the same type (eg. both
	 * vertices or both edges).
	 */
	public Object[] importCells(Object[] cells, double dx, double dy, Object target, Point location) {
		
		System.out.println("importCells");
		if (target == null && cells.length == 1 && location != null) {
			target = getCellAt(location.x, location.y);

			if (target instanceof mxICell && cells[0] instanceof mxICell) {
				mxICell targetCell = (mxICell) target;
				mxICell dropCell = (mxICell) cells[0];

				if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
					mxIGraphModel model = graph.getModel();
					model.setStyle(target, model.getStyle(cells[0]));
					graph.setSelectionCell(target);

					return null;
				}
			}
		}
		// �����������������
		return super.importCells(cells, dx, dy, target, location);
	}

}