package com.microsoft.azure.auth.wrap;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.azure.auth.wrap.contract.WrapContract;

public class WrapClient {
	WrapContract contract;
	private String uri;
	private String name;
	private String password;
	private String scope;

	@Inject
	public WrapClient(
			WrapContract contract, 
			@Named("wrap.uri") String uri,
			@Named("wrap.scope") String scope,
			@Named("wrap.name") String name,
			@Named("wrap.password") String password) {
		this.contract = contract;
		this.uri = uri;
		this.scope = scope;
		this.name = name;
		this.password = password;
	}
	

	/**
	 * @return the contract
	 */
	public WrapContract getContract() {
		return contract;
	}

	/**
	 * @param contract the contract to set
	 */
	public void setContract(WrapContract contract) {
		this.contract = contract;
	}

	public String getAccessToken() {
		return getContract().post(uri, name, password, scope).getAccessToken();
	}
}
