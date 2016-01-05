package com.outlooktest.model;

public class Data {
	private Query query;

	private String batchcomplete;

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public String getBatchcomplete() {
		return batchcomplete;
	}

	public void setBatchcomplete(String batchcomplete) {
		this.batchcomplete = batchcomplete;
	}

	@Override
	public String toString() {
		return "[query = " + query + ", batchcomplete = "
				+ batchcomplete + "]";
	}
}
