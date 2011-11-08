package com.microsoft.azure.http;

import com.microsoft.azure.http.ServiceFilter.Request;
import com.microsoft.azure.http.ServiceFilter.Response;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ClientFilterAdapter extends ClientFilter {
	ServiceFilter filter;

	public ClientFilterAdapter(ServiceFilter filter) {
		this.filter = filter;
	}

	@Override
	public ClientResponse handle(ClientRequest clientRequest)
			throws ClientHandlerException {

		final ClientRequest cr = clientRequest;
		Response resp = filter.handle(
				new ServiceFilterRequest(clientRequest),
				new ServiceFilter.Next() {
					public Response handle(
							Request request) {
						return new ServiceFilterResponse(
								getNext().handle(cr));
					}
				});

		return ((ServiceFilterResponse) resp).clientResponse;
	}
}

class ServiceFilterRequest implements ServiceFilter.Request {
	ClientRequest clientRequest;

	public ServiceFilterRequest(ClientRequest clientRequest) {
		this.clientRequest = clientRequest;
	}

}

class ServiceFilterResponse implements ServiceFilter.Response {
	ClientResponse clientResponse;

	public ServiceFilterResponse(ClientResponse clientResponse) {
		this.clientResponse = clientResponse;
	}

}