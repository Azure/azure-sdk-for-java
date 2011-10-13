package com.microsoft.azure.http;

import java.net.URI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public abstract class ClientBase {
	Client client;
	URI uri;

	public ClientBase(Client client, URI uri) {
		this.client = client;
		this.uri = uri;
	}

	protected WebResource resource() {
		return client.resource(uri);
	}
}
