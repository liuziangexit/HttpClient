package com.game.http;

import java.util.Map;

public class HttpResponse {

    public int getStatusCode() {
	return statusCode;
    }

    public Map<String, String> getHeaders() {
	return headers;
    }

    public String getBody() {
	return body;
    }

    public void setStatusCode(int statusCode) {
	this.statusCode = statusCode;
    }

    public void setHeaders(Map<String, String> headers) {
	this.headers = headers;
    }

    public void setBody(String body) {
	this.body = body;
    }

    private int statusCode;
    private Map<String, String> headers;
    private String body;

}
