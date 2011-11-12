package com.microsoft.windowsazure.auth.wrap.contract;

import com.microsoft.windowsazure.ServiceException;

public interface WrapContract {
    WrapResponse post(String uri, String name, String password, String scope) throws ServiceException;
}
