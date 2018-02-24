package com.base.ant;

import org.apache.tools.ant.ProjectComponent;

public class ReplaceFileTag extends ProjectComponent {
	private String reg;
	private String value;

	public ReplaceFileTag() {
	}

	public ReplaceFileTag(String reg, String value) {
		this.reg = reg;
		this.value = value;
	}

	public String getReg() {
		return reg;
	}

	public void setReg(String reg) {
		this.reg = reg;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return "reg:" + reg + " value:" + value;
	}
}
