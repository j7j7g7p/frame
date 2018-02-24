package com.base.bigdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import com.base.utils.DebugUtils;
import com.base.utils.ParaMap;

public class MapReduce {
	public List<Partition> input = new ArrayList<Partition>();
	public List<List<Record>> output = new ArrayList<List<Record>>();
	public List<Count> countList = new ArrayList<Count>();

	public ForkJoinPool pool = new ForkJoinPool(20);

	/**
	 * select t1.id as t1_id,t2.id as t2_id t1.createtime from aaa01 as t1 left join bbb as t2 on t1.id=t2.refid where ... 
	 * 
	 * select t1.id,t2.id t1.createtime from aaa02 as t1 left join bbb as t2 on t1.id=t2.refid order by t1.createtime desc 
	 * 
	 * select t1.id,t2.id t1.createtime from aaa03 as t1 left join bbb as t2 on t1.id=t2.refid order by t1.createtime desc
	 * 
	 * 
	 * 
	 * t1.name t2.addr t3.age
	 * 
	 * 
	 * t1.createtime desc 
	 * 
	 * 
	 * pageCount:10 pageNum:2
	 * 
	 */

	private String sortStr;
	private String fieldStr;
	private int pageSize;
	private int pageIndex;

	public MapReduce(List<String> partitionSqlList, String sortStr,
			String fieldStr, int pageCount, int pageNum) {
		this.sortStr = sortStr;
		this.fieldStr = fieldStr;
		this.pageSize = pageCount;
		this.pageIndex = pageNum;
		//
		for (int i = 0; i < partitionSqlList.size(); i++) {
			String queryStr = partitionSqlList.get(i);
			Partition p = new Partition(queryStr, sortStr, fieldStr, pageCount);
			input.add(p);
		}
	}

	public ParaMap execute() {
		ParaMap outMap = null;
		try {
			long begin = System.currentTimeMillis();
			DebugUtils.thread_init();
			List<Future<List<Record>>> futureList = pool.invokeAll(input);
			for (int i = 0; i < futureList.size(); i++) {
				List<Record> rs = futureList.get(i).get();
				if (rs.size() > 0) {
					output.add(rs);
					countList.add(new Count(rs.get(0).p.parser.count()));
				}
			}
			DebugUtils.thread_elapse2("查询数据");
			//
			int compareDirect = 0;
			if (sortStr.indexOf("desc") > 0)
				compareDirect = 1;
			if (sortStr.indexOf("asc") > 0)
				compareDirect = -1;
			List<Record> list = Aggregate.sort(pool, output, compareDirect);
			int loaded = list.size();
			if (loaded > pageSize)
				loaded = pageSize;
			List<Record> rs = list.subList(0, loaded);
			DebugUtils.thread_elapse2("排序");
			//
			outMap = loadData(rs);
			DebugUtils.thread_elapse2("加载数据");
			//
			loadDataCount(outMap);
			DebugUtils.thread_elapse2("计算记录数");
			long end = System.currentTimeMillis();
			System.out.println("MR:" + (end - begin) + "ms");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return outMap;
	}

	/**
	 * 串行比较
	 * @param loadCount
	 * @param compareDirect
	 * @return
	 */
	public List<Record> sort(int loadCount, int compareDirect) {
		List<Record> rs = new ArrayList<Record>();
		for (int i = 0; i < loadCount; i++) {
			int index = 0;//哨岗
			for (int j = 1; j < output.size(); j++) {
				Object value1 = output.get(index).get(0).getV();
				Object value2 = output.get(j).get(0).getV();
				if (Aggregate.compare(value1, value2) == compareDirect) {
					index = j;
				}
				rs.add(output.get(index).get(0));
				output.get(index).remove(0);
			}
		}
		return rs;
	}

	private ParaMap loadData(List<Record> list) {
		ParaMap outMap = new ParaMap();
		List rs = new ArrayList();
		//
		String fsSql = input.get(0).parser.columnDesc();
		ParaMap fsMap = SQLUtils.columnInfo(fsSql);

		outMap = fsMap;
		DebugUtils.thread_elapse2("加载数据表头");
		//
		for (int i = 0; i < list.size(); i++) {
			Record r = list.get(i);
			rs.add(r.data());
		}
		outMap.put("rs", rs);
		//pageIndex":1,"pageSize":10
		outMap.put("pageIndex", pageIndex);
		outMap.put("pageSize", pageSize);

		return outMap;
	}

	private void loadDataCount(ParaMap outMap) {
		//
		long count = 0;
		try {
			List<Future<Long>> countFutures = pool.invokeAll(countList);
			for (int i = 0; i < countFutures.size(); i++) {
				count += countFutures.get(i).get();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		outMap.put("totalCount", count);
	}

}
