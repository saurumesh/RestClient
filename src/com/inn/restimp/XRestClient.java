package com.inn.restimp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

public class XRestClient implements IRestClient {
	private static final int CONNECTION_TIME_OUT = 15 * 1000;
	private static final int SOCKET_TIME_OUT_MIN = 15 * 1000; // 15 seconds
	private static final int SOCKET_TIME_OUT_AVG = 30 * 1000;
	private static final int SOCKET_TIME_OUT_MAX = 1 * 60 * 1000;

	private static Context mContext;
	public static HttpClient mHttpclient;

	private String url;
	private RequestMethod requestMethod;
	private List<NameValuePair> headerList;
	private List<NameValuePair> paramList;
	//	public int statusCode;
	//	public String responseMsg;
	//	public String errorMsg;

	static {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIME_OUT);
		mHttpclient = new DefaultHttpClient(httpParameters);
	}

	public XRestClient() {
	}

	public XRestClient(Context pContext) {
		mContext = pContext;
	}

	/**
	 * set a new url and requestMethod, requestMethod may be anyone of GET,
	 * POST, PUT, also it clears the headerList and paramList to make new
	 * request.
	 * 
	 * @param url
	 * @param requestMethod
	 */
	@Override
	public void setNewRequest(String url, RequestMethod requestMethod) {
		headerList = new ArrayList<NameValuePair>();
		paramList = new ArrayList<NameValuePair>();
		this.url = url;
		this.requestMethod = requestMethod;
	}

	/**
	 * Adds a parameter to the parameterList.
	 * 
	 * @param name
	 * @param value
	 */
	public void addParam(String name, String value) {
		paramList.add(new BasicNameValuePair(name, value));
	}

	/**
	 * Adds a header to the headerList.
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		headerList.add(new BasicNameValuePair(name, value));
	}

	/**
	 * Executes httpUriRequest depending on RequestMehode passed in the parameter,
	 * requestMethod may be anyone of GET, POST, PUT
	 * 
	 * @param requestMethod
	 * @return HttpUriRequest, this will be anyone of HttpGet, HttpPost, HttpPut
	 * @throws UnsupportedEncodingException
	 */
	private HttpUriRequest executeHttpRequest(RequestMethod requestMethod) throws UnsupportedEncodingException {
		HttpUriRequest request = null;
		switch (requestMethod) {
		case GET:
			request = prepareHttpGetRequest(url);
			break;
		case POST:
			request = prepareHttpPostRequest(url);
			break;
		case PUT:
			request = prepareHttpPutRequest(url);
			break;
		}
		return request;
	}

	/**
	 * Executes a HttpGet request, add all the parameters as query-params and add
	 * all headers to the request.
	 * 
	 * @param url
	 * @return HttpGet
	 * @throws UnsupportedEncodingException
	 */
	private HttpGet prepareHttpGetRequest(String url) throws UnsupportedEncodingException {
		//add parameters
		String queryString = "";
		if (!paramList.isEmpty()) {
			queryString += "?";
			for (int i = 0; i < paramList.size(); i++) {
				NameValuePair param = paramList.get(i);
				if (i == 0) {
					queryString = "?" + param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
				} else {
					queryString = "&" + param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
				}
			}
		}

		HttpGet request = new HttpGet(url + queryString);

		//add headers
		for (NameValuePair header : headerList) {
			request.addHeader(header.getName(), header.getValue());
		}
		return request;
	}

	/**
	 * Executes a HttpPost request, add all the parameters as form entity and add
	 * all headers to the request.
	 * 
	 * @param url
	 * @return HttpPost
	 * @throws UnsupportedEncodingException
	 */
	private HttpPost prepareHttpPostRequest(String url) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(url);

		//add headers
		for (NameValuePair header : headerList) {
			request.addHeader(header.getName(), header.getValue());
		}

		//add parameters
		if (!paramList.isEmpty()) {
			request.setEntity(new UrlEncodedFormEntity(paramList));
		}
		return request;
	}

	/**
	 * Executes a HttpPut request, add all the parameters as form entity and add
	 * all headers to the request.
	 * 
	 * @param url
	 * @return HttpPut
	 */
	private HttpPut prepareHttpPutRequest(String url) {
		HttpPut request = new HttpPut(url);

		//add headers
		for (NameValuePair header : headerList) {
			request.addHeader(header.getName(), header.getValue());
		}
		return request;
	}

	@Override
	public Response executeRequest() {
		try {
			HttpUriRequest request = executeHttpRequest(requestMethod);
			HttpResponse res = mHttpclient.execute(request);
			return processResponse(res);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			mHttpclient.getConnectionManager().closeExpiredConnections();
		}
		return null;
	}

	private Response processResponse(HttpResponse res) {
		Response response = new Response();
		int statusCode = res.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = res.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					response.responseType = Response.TYPE.OK;
					response.responseMsg = AppUtil.readData(new InputStreamReader(inputStream));
				} catch (IllegalStateException e) {
					e.printStackTrace();
					response.responseType = Response.TYPE.FAIL;
					response.responseMsg = e.getMessage();
				} catch (IOException e) {
					e.printStackTrace();
					response.responseType = Response.TYPE.FAIL;
					response.responseMsg = e.getMessage();
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			response.responseType = Response.TYPE.FAIL;
			response.responseMsg = "No Data Found";
		}
		return response;
	}
}
