package com.base.ant;

import org.apache.tools.ant.ProjectComponent;

public class GenProFileTag extends ProjectComponent {
	private String key;
	private String value;

	public GenProFileTag() {
	}

	public GenProFileTag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return key.trim() + "=" + value.trim();
	}

}
