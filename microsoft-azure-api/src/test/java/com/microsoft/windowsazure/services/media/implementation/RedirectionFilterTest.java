package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.*;

import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.header.InBoundHeaders;

public class RedirectionFilterTest {
    private final String originalBaseURI = "https://base.somewhere.example/API/";
    private final String redirectedBaseURI = "http://redirected.somewhere.example/Stuff/";

    private class RequestRecordingFilter extends ClientFilter {
        public ClientRequest request;

        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
            this.request = request;

            ClientResponse response = Mockito.mock(ClientResponse.class);
            Mockito.when(response.getStatus()).thenReturn(200);
            return response;
        }

    }

    @Test
    public void whenInvokedAndNotRedirected_shouldAddBaseURIToRequest() throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectFilter(new ResourceLocationManager(originalBaseURI)));

        c.resource("Files").get(ClientResponse.class);
        new URI(null);

        assertEquals(originalBaseURI + "Files", sink.request.getURI().toString());
    }

    @Test
    public void whenInvokedAndRedirected_shouldHaveRedirectedURIInRequest() throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();

        // Filter to fake the server redirecting the request.
        ClientFilter redirector = new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

                if (request.getURI().toString().startsWith(originalBaseURI)) {
                    ClientResponse response = Mockito.mock(ClientResponse.class);
                    Mockito.when(response.getStatus()).thenReturn(301);
                    MultivaluedMap<String, String> headers = new InBoundHeaders();
                    headers.add("location", redirectedBaseURI);
                    Mockito.when(response.getHeaders()).thenReturn(headers);
                    return response;
                }
                else {
                    return getNext().handle(request);
                }
            }
        };

        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(redirector);

        c.addFilter(new RedirectFilter(new ResourceLocationManager(originalBaseURI)));

        ClientResponse response = c.resource("Things").get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals(redirectedBaseURI + "Things", sink.request.getURI().toString());
    }
}
