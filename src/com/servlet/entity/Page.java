package com.servlet.entity;

import java.util.List;

public class Page<T> {
	private Integer page;// ��ǰ�ڼ�ҳ
	private Integer pageSize = 5;// ÿҳ��ʾ������
	private Integer pageSum;// ���ж�������¼
	private Integer pageCount;// ��ҳ��
	private String url;// ����·��
	private String params;// �������
	private List<T> datas;// ��ǰҳ������

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageSum() {
		return pageSum;
	}

	public void setPageSum(Integer pageSum) {
		this.pageSum = pageSum;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "Page [page=" + page + ", pageSize=" + pageSize + ", pageSum=" + pageSum + ", pageCount=" + pageCount
				+ ", datas=" + datas + "]";
	}
}
