package com.inn.restimp;

public interface IRestClient {
	public static enum RequestMethod {
		GET, POST, PUT
	}

	public Response executeRequest();

	public void setNewRequest(String url, RequestMethod requestMethod);
}
