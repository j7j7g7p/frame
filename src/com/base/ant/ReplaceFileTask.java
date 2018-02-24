package com.base.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.Reference;

public class ReplaceFileTask extends Task {
	private File file;
	private Reference ref;
	private ArrayList<ReplaceFileTag> regList = new ArrayList<ReplaceFileTag>();

	public void setFile(File file) {
		this.file = file;
	}

	public void setRefid(Reference ref) {
		this.ref = ref;
	}

	public void addToken(ReplaceFileTag tag) {
		regList.add(tag);
	}

	public ArrayList<ReplaceFileTag> getToken() {
		return regList;
	}

	public void execute() {
		try {
			if (this.ref != null)
				initRegList();
			String content = FileUtils.readFileToString(file);
			// System.out.println("************before:\n" + content);
			for (int i = 0; i < regList.size(); i++) {
				ReplaceFileTag tag = regList.get(i);
				System.out.println(tag);
				content = content.replaceAll(tag.getReg(), tag.getValue());
				// content = content.replaceAll("versionMode=\\w+",
				// "versionMode=demo");
			}
			// System.out.println("*************after:\n" + content);
			FileUtils.writeStringToFile(file, content);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initRegList() {
		Object obj = this.ref.getReferencedObject(getProject());
		System.out.println(obj.getClass().getName());
		if (Target.class.getName().equals(obj.getClass().getName())) {
			Target target = (Target) obj;
			Task[] tasks = target.getTasks();
			//			System.out.println(tasks.length);
			for (Task task : tasks) {
				UnknownElement pro = (UnknownElement) task;
				Hashtable ht = pro.getWrapper().getAttributeMap();
				String name = String.valueOf(ht.get("name"));
				String value = String.valueOf(ht.get("value"));
				//				System.out.println(ht.get("name") + "\t" + ht.get("value"));
				regList.add(new ReplaceFileTag(name, value));

			}

		}
	}
}
