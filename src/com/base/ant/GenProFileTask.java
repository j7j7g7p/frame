package com.base.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.Reference;

public class GenProFileTask extends Task {
	private File file;
	private Reference ref;
	private ArrayList<GenProFileTag> tagList = new ArrayList<GenProFileTag>();

	public void setFile(File file) {
		this.file = file;
	}

	public void setRefid(Reference ref) {
		this.ref = ref;
	}

	public void addTag(GenProFileTag tag) {
		tagList.add(tag);
	}

	public void execute() {
		try {
			if (this.ref != null)
				initRegList();
			List<String> list = new ArrayList<String>();
			for (GenProFileTag tag : tagList) {
				String key = tag.getKey().trim();
				String value = tag.getValue().trim();
				list.add(key + "=" + value);
			}
			FileUtils.writeLines(file, list);
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
				tagList.add(new GenProFileTag(name, value));

			}

		}
	}
}
