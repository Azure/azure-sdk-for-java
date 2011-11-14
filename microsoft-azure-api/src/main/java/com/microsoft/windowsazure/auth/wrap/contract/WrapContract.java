package com.microsoft.windowsazure.auth.wrap.contract;

import com.microsoft.windowsazure.common.ServiceException;

public interface WrapContract {
    WrapResponse post(String uri, String name, String password, String scope) throws ServiceException;
}
