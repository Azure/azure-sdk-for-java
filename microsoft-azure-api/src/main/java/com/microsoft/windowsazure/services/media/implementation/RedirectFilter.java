package com.microsoft.windowsazure.services.media.implementation;

import java.net.URI;
import java.net.URISyntaxException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class RedirectFilter extends ClientFilter {
    private final ResourceLocationManager locationManager;

    public RedirectFilter(ResourceLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        URI originalURI = request.getURI();
        request.setURI(locationManager.getRedirectedURI(originalURI));

        ClientResponse response = getNext().handle(request);
        while (response.getStatus() == 301) {
            try {
                locationManager.setRedirectedURI(response.getHeaders().getFirst("Location"));
            }
            catch (NullPointerException ex) {
                throw new ClientHandlerException("HTTP Redirect did not include Location header");
            }
            catch (URISyntaxException ex) {
                throw new ClientHandlerException("HTTP Redirect location is not a valid URI");
            }

            request.setURI(locationManager.getRedirectedURI(originalURI));
            response = getNext().handle(request);
        }
        return response;
    }
}
