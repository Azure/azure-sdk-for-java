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
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

public class SASTokenFilterTest {

    private final String sampleSASToken = "st=2012-10-05T21%3A10%3A25Z&se=2012-10-05T21%3A15%3A25Z&sr=c&si=89fa57b4-293f-40df-a0cb-9d84ee493b8c&sig=iMDPr8V%2FIJrYG8t2GeSqBh5tTUdM7ykOObFVICa%2F%2F1Q%3D";
    private RequestRecordingFilter sink;
    private Client client;

    @Before
    public void setup() throws Exception {
        sink = new RequestRecordingFilter();
        client = Client.create();
        client.addFilter(sink);
        client.addFilter(new SASTokenFilter(sampleSASToken));
    }

    @Test
    public void filterAddsQueryParameterToRequestUrl() throws Exception {

        WebResource r = client
                .resource("http://astorageaccount.blob.something.example/asset-abcd");

        r.get(ClientResponse.class);

        assertContainsSASToken(sink.request.getURI());
    }

    @Test
    public void filterPreservesQueryParameters() throws Exception {
        client.resource("http://storage.service.example/asset-efgh")
                .queryParam("param0", "first").queryParam("param1", "second")
                .get(ClientResponse.class);

        assertContainsSASToken(sink.request.getURI());

        Map<String, String> queryParams = parseQueryParameters(sink.request
                .getURI());
        assertTrue(queryParams.containsKey("param0"));
        assertTrue(queryParams.containsKey("param1"));
        assertEquals("first", queryParams.get("param0"));
        assertEquals("second", queryParams.get("param1"));
    }

    // Test support code

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

    // Assertion helpers

    private void assertContainsSASToken(URI uri) {
        Map<String, String> queryParams = parseQueryParameters(uri);

        assertTrue(queryParams.containsKey("st"));
        assertTrue(queryParams.containsKey("se"));
        assertTrue(queryParams.containsKey("sr"));
        assertTrue(queryParams.containsKey("si"));
        assertTrue(queryParams.containsKey("sig"));

        assertEquals("iMDPr8V%2FIJrYG8t2GeSqBh5tTUdM7ykOObFVICa%2F%2F1Q%3D",
                queryParams.get("sig"));
    }

    // Simplistic parsing of query parameters into map so we can assert against
    // contents
    // easily.
    private Map<String, String> parseQueryParameters(URI uri) {
        HashMap<String, String> queryParameters = new HashMap<String, String>();
        String queryString = uri.getRawQuery();
        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        String[] parameters = queryString.split("&");

        for (String param : parameters) {
            int firstEqualIndex = param.indexOf('=');
            String paramName = param.substring(0, firstEqualIndex);
            String value = param.substring(firstEqualIndex + 1);

            queryParameters.put(paramName, value);
        }
        return queryParameters;
    }

}
