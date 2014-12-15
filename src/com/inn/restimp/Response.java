package com.inn.restimp;

public class Response {
	public enum TYPE {
		OK, FAIL
	}

	public TYPE responseType;
	public String responseMsg;
}
