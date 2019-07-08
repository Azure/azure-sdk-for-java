/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.header.InBoundHeaders;

public class RedirectionFilterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String originalBaseURI = "https://base.somewhere.example/API/";
    private final String redirectedBaseURI = "http://redirected.somewhere.example/Stuff/";

    @Test
    public void whenInvokedAndNotRedirected_shouldAddBaseURIToRequest()
            throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectFilter(new ResourceLocationManager(
                originalBaseURI)));

        c.resource("Files").get(ClientResponse.class);

        assertEquals(originalBaseURI + "Files", sink.request.getURI()
                .toString());
    }

    @Test
    public void whenInvokedAndRedirected_shouldHaveRedirectedURIInRequest()
            throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectingTestFilter(originalBaseURI,
                redirectedBaseURI));

        c.addFilter(new RedirectFilter(new ResourceLocationManager(
                originalBaseURI)));

        ClientResponse response = c.resource("Things")
                .get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        assertEquals(redirectedBaseURI + "Things", sink.request.getURI()
                .toString());
    }

    @Test
    public void whenRedirectedMultipleTimes_requestEndsUpAtFinalRediret()
            throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectingTestFilter("https://a.example/API/",
                "https://b.example/API/"));
        c.addFilter(new RedirectingTestFilter("https://b.example/API/",
                "https://c.example/API/"));
        c.addFilter(new RedirectingTestFilter("https://c.example/API/",
                "https://final.example/Code/"));

        c.addFilter(new RedirectFilter(new ResourceLocationManager(
                "https://a.example/API/")));

        ClientResponse response = c.resource("Stuff").get(ClientResponse.class);

        assertEquals(200, response.getStatus());

        assertEquals("https://final.example/Code/Stuff", sink.request.getURI()
                .toString());
    }

    @Test
    public void whenRedirectingToNull_shouldGetClientException()
            throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectingTestFilter(originalBaseURI, null));
        c.addFilter(new RedirectFilter(new ResourceLocationManager(
                originalBaseURI)));

        thrown.expect(ClientHandlerException.class);
        c.resource("Something").get(ClientResponse.class);
    }

    @Test
    public void whenRedirectingToBadURI_shouldGetClientException()
            throws Exception {
        RequestRecordingFilter sink = new RequestRecordingFilter();
        Client c = Client.create();
        c.addFilter(sink);
        c.addFilter(new RedirectingTestFilter(originalBaseURI,
                "no way this is valid!"));
        c.addFilter(new RedirectFilter(new ResourceLocationManager(
                originalBaseURI)));

        thrown.expect(ClientHandlerException.class);
        c.resource("Something").get(ClientResponse.class);
    }

    // Test support classes

    //
    // Filter that acts as a "sink" so the request doesn't go out over
    // the wire. Also holds onto the request object that went through
    // the pipeline so that it can be asserted against in the test.
    //
    private class RequestRecordingFilter extends ClientFilter {
        public ClientRequest request;

        @Override
        public ClientResponse handle(ClientRequest request)
                throws ClientHandlerException {
            this.request = request;

            ClientResponse response = Mockito.mock(ClientResponse.class);
            Mockito.when(response.getStatus()).thenReturn(200);
            return response;
        }
    }

    //
    // Filter that will 301-redirect requests depending on which URI
    // the request goes to.
    //

    private class RedirectingTestFilter extends ClientFilter {
        private final String uriToRedirect;
        private final String uriRedirectedTo;

        public RedirectingTestFilter(String uriToRedirect,
                String uriRedirectedTo) {
            this.uriToRedirect = uriToRedirect;
            this.uriRedirectedTo = uriRedirectedTo;
        }

        @Override
        public ClientResponse handle(ClientRequest request)
                throws ClientHandlerException {

            if (request.getURI().toString().startsWith(uriToRedirect)) {
                ClientResponse response = Mockito.mock(ClientResponse.class);
                Mockito.when(response.getClientResponseStatus()).thenReturn(
                        ClientResponse.Status.MOVED_PERMANENTLY);
                MultivaluedMap<String, String> headers = new InBoundHeaders();
                headers.add("location", uriRedirectedTo);
                Mockito.when(response.getHeaders()).thenReturn(headers);
                return response;
            } else {
                return getNext().handle(request);
            }
        }
    }
}
