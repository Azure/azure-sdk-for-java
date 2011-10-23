package com.microsoft.azure.auth.wrap.contract;

public interface WrapContract {
	WrapResponse post(String uri, String name, String password, String scope);
}
