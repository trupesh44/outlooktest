package com.outlooktest.model;

public class Thumbnail {

	private String height;

	private String source;

	private String width;

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return "[height = " + height + ", source = " + source
				+ ", width = " + width + "]";
	}
}
