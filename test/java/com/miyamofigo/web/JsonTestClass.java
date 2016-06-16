package com.miyamofigo.web;

public class JsonTestClass {

	private int number;
 	private String address;

	public JsonTestClass() {}

	public JsonTestClass(int number, String address) {
		this.number = number;
		this.address = address;
	}

	public int getNumber() { 
		return number; 
	}

	public void setNumber(int number) { 
		this.number = number; 
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}

