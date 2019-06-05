package cn.com.yusys.ide.flow.view.core.custom;

import java.io.Serializable;

public class Node implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String className;
	private String mathName;
	private String type;
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMathName() {
		return mathName;
	}

	public void setMathName(String mathName) {
		this.mathName = mathName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "╨хак";
	}
}
