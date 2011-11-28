package com.microsoft.windowsazure.services.serviceBus.implementation;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class WrapFilter extends ClientFilter {
    private WrapTokenManager tokenManager;

    public WrapFilter(WrapTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {

        String accessToken;
        try {
            accessToken = tokenManager.getAccessToken();
        }
        catch (ServiceException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }

        cr.getHeaders().add("Authorization", "WRAP access_token=\"" + accessToken + "\"");

        return this.getNext().handle(cr);
    }
}
