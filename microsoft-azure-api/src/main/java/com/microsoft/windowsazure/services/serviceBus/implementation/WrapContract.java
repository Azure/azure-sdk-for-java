package com.microsoft.windowsazure.services.serviceBus.implementation;

import com.microsoft.windowsazure.services.core.ServiceException;

public interface WrapContract {
    WrapAccessTokenResult wrapAccessToken(String uri, String name, String password, String scope)
            throws ServiceException;
}
