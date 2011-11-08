package com.microsoft.azure.http;


public interface ServiceFilter {
	Response handle(Request request, Next next);
	
	public interface Next {
		Response handle(Request request);
	}
	
	public interface Request {
	}
	
	public interface Response {
	}
}
