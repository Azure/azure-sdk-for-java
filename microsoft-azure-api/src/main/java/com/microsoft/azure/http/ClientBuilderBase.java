package com.microsoft.azure.http;

import java.net.URI;

import com.sun.jersey.api.client.Client;

public abstract class ClientBuilderBase {
	protected Client client = new Client();
	
	URI url;

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}
}
