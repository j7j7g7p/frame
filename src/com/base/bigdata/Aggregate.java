package com.base.bigdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class Aggregate implements Callable<List<Record>> {
	private List<Record> l1;
	private List<Record> l2;
	private int compareDirect;

	public Aggregate(List<Record> l1, List<Record> l2, int compareDirect) {
		this.l1 = l1;
		this.l2 = l2;
		this.compareDirect = compareDirect;
	}

	@Override
	public List<Record> call() throws Exception {
		return sort(l1, l2);
	}

	public List<Record> sort(List<Record> l1, List<Record> l2) {
		List<Record> list = new ArrayList<Record>();
		int i1 = 0, i2 = 0;
		while (i1 < l1.size() && i2 < l2.size()) {
			if (compare(l1.get(i1).getV(), l2.get(i2).getV()) == compareDirect) {
				list.add(l1.get(i1));
				i1++;
			} else {
				list.add(l2.get(i2));
				i2++;
			}
		}

		if (i1 < l1.size())
			list.addAll(l1.subList(i1, l1.size()));
		if (i2 < l2.size())
			list.addAll(l2.subList(i2, l2.size()));
		return list;
	}

	public static List<Record> sort(ForkJoinPool pool, List<List<Record>> list,
			int compareDirect) throws Exception {
		int mid = list.size() / 2;
		int mod = list.size() % 2;
		if (list.size() == 1)
			return list.get(0);
		else if (list.size() == 0) {
			return new ArrayList<Record>();
		} else {
			List<Aggregate> list2 = new ArrayList<Aggregate>();
			for (int i = 0; i < mid; i++) {
				List<Record> l1 = list.get(i);
				List<Record> l2 = list.get(i + mid);
				Aggregate aggreagte = new Aggregate(l1, l2, compareDirect);
				list2.add(aggreagte);
			}
			List<Future<List<Record>>> list3 = pool.invokeAll(list2);
			List<List<Record>> list4 = new ArrayList<List<Record>>();
			if (mod == 1)
				list4.add(list.get(list.size() - 1));
			for (int i = 0; i < list3.size(); i++) {
				Future<List<Record>> future = list3.get(i);
				list4.add(future.get());
			}
			return sort(pool, list4, compareDirect);
		}

	}

	public static int compare(Object o1, Object o2) {
		if (o1 instanceof Long && o2 instanceof Long) {
			Long l1 = (Long) o1;
			Long l2 = (Long) o2;
			if (l1 > l2)
				return 1;
			if (l1 < l2)
				return -1;

		}
		return 0;
	}

	public static void main(String[] args) {
		List<Integer> l1 = new ArrayList<Integer>();
		for (int i = 1; i < 10000; i++)
			l1.add(i + 3);
		//
		List<Integer> l2 = new ArrayList<Integer>();
		for (int i = 1; i < 10000; i++)
			l2.add(i + 1);
		//

	}

}
