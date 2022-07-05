package com.ikno.itracclient;

public class ColorCode {

	static final ColorCode WHITE = new ColorCode("White",255,255,255);
	
	private int r,g,b;
	private String name;
	public ColorCode(String name,int r, int g, int b) {
		this.name = name;
		this.r = r;
		this.g = g;
		this.b = b;
	}
}
