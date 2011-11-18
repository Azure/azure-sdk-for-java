package com.microsoft.windowsazure.services.serviceBus.implementation;

import com.microsoft.windowsazure.common.ServiceException;

public interface WrapContract {
    WrapAccessTokenResult wrapAccessToken(String uri, String name, String password, String scope)
            throws ServiceException;
}
