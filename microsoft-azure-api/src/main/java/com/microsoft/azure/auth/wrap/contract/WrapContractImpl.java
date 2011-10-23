package com.microsoft.azure.auth.wrap.contract;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.representation.Form;

public class WrapContractImpl implements WrapContract {
	Client channel;
	
	@Inject
	public WrapContractImpl(Client channel) {
		this.channel = channel;
	}

	public WrapResponse post(String uri, String name, String password, String scope) {
		Form requestForm = new Form();
		requestForm.add("wrap_name", name);
		requestForm.add("wrap_password", password);
		requestForm.add("wrap_scope", scope);
		
		Form responseForm = channel.resource(uri)
			.accept(MediaType.APPLICATION_FORM_URLENCODED)
			.type(MediaType.APPLICATION_FORM_URLENCODED)
			.post(Form.class, requestForm);
		
		WrapResponse response = new WrapResponse();
		response.setAccessToken(responseForm.getFirst("wrap_access_token"));
		response.setExpiresIn(responseForm.getFirst("wrap_access_token_expires_in"));
		return response;
	}
}
