package com.inn.restimp;

import com.inn.restimp.IRestClient.RequestMethod;

public class RequestHandler {
	private static RequestHandler _instance;

	private RequestHandler() {
	}

	public RequestHandler getInstance() {
		if (_instance == null)
			_instance = new RequestHandler();
		return _instance;
	}

	public Object sendLoginRequest(String url, String username, String password) {
		XRestClient client = new XRestClient();
		client.setNewRequest("urls", RequestMethod.GET);
		client.addParam("username", "suresh.singh");
		client.addParam("password", "Pass_123");
		Response response = client.executeRequest();
		if (response.responseType == Response.TYPE.OK) {
			// parse user json and return User object
			return "";
		} else {
			return response;
		}
	}
}
