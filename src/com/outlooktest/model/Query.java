package com.outlooktest.model;

import java.util.Map;

public class Query {

	private Map<String, PageInfo> pages;

	public Map<String, PageInfo> getPages() {
		return pages;
	}

	public void setPages(Map<String, PageInfo> pages) {
		this.pages = pages;
	}

	@Override
	public String toString() {
		return "[pages = " + pages + "]";
	}
}
