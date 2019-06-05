package cn.com.yusys.ide.flow.view;

import javax.swing.JFrame;

import com.mxgraph.util.mxResources;

import cn.com.yusys.ide.flow.view.core.BasicGraphEditor;
import cn.com.yusys.ide.flow.view.core.custom.CustomGraph;
import cn.com.yusys.ide.flow.view.core.custom.CustomGraphComponent;


public class FlowViewStart {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.getContentPane().add(new BasicGraphEditor("Á÷³Ì±àÅÅ", new CustomGraphComponent(new CustomGraph())));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 600);
		frame.setVisible(true);
	}
}
