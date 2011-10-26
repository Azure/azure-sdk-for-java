package com.microsoft.azure.auth.wrap.contract;

import com.microsoft.azure.ServiceException;

public interface WrapContract {
	WrapResponse post(String uri, String name, String password, String scope) throws ServiceException;
}
