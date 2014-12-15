package com.inn.restimp;

import android.app.Activity;
import android.os.Bundle;

import com.inn.restimp.IRestClient.RequestMethod;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		XRestClient client = new XRestClient();
		client.setNewRequest("urls", RequestMethod.GET);
		client.addParam("username", "suresh.singh");
		client.addParam("password", "Pass_123");
		Response response = client.executeRequest();
		System.out.println("response.responseType.toString() : " + response.responseType.toString());
	}
}
