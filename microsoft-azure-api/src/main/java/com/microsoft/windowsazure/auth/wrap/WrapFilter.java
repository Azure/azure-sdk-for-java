package com.microsoft.windowsazure.auth.wrap;

import com.microsoft.windowsazure.ServiceException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class WrapFilter extends ClientFilter {
    private WrapClient client;

    public WrapFilter(WrapClient client) {
        this.client = client;
    }

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {

        String accessToken;
        try {
            accessToken = client.getAccessToken();
        }
        catch (ServiceException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }

        cr.getHeaders().add("Authorization",
                "WRAP access_token=\"" + accessToken + "\"");

        return this.getNext().handle(cr);
    }
}
