/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Test fixture for IdempotentClientFilter class
 * 
 */
public class IdempotentClientFilterTest {

    @Test
    public void filterRunsFirstTime() throws Exception {
        CountingFilter filterInTest = new CountingFilter();

        Client client = getClient();
        client.addFilter(filterInTest);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());

        assertEquals(1, filterInTest.count);
    }

    @Test
    public void filterRunsOnlyOnceWhenRequestIsRetried() throws Exception {
        RetryFilter retry = new RetryFilter();
        retry.triesLeft = 3;
        CountingFilter filterInTest = new CountingFilter();

        Client client = getClient();
        client.addFilter(filterInTest);
        client.addFilter(retry);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());
        assertEquals(1, filterInTest.count);
    }

    @Test
    public void multipleIdempotentFiltersRunFirstTime() throws Exception {
        CountingFilter filter1 = new CountingFilter();
        CountingFilter filter2 = new CountingFilter();
        filter2.key = "secondfilter";

        Client client = getClient();
        client.addFilter(filter1);
        client.addFilter(filter2);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());

        assertEquals(1, filter1.count);
        assertEquals(1, filter2.count);

    }

    @Test
    public void multipleIdempotentFiltersRunOnceWhenRetried() throws Exception {
        CountingFilter filter1 = new CountingFilter();
        CountingFilter filter2 = new CountingFilter();
        filter2.key = "secondfilter";

        RetryFilter retry = new RetryFilter();
        retry.triesLeft = 4;

        Client client = getClient();
        client.addFilter(filter1);
        client.addFilter(filter2);
        client.addFilter(retry);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());

        assertEquals(1, filter1.count);
        assertEquals(1, filter2.count);
    }

    @Test
    public void idempotentFilterRunsAgainOnSecondRequest() throws Exception {
        CountingFilter filterInTest = new CountingFilter();

        Client client = getClient();
        client.addFilter(filterInTest);

        WebResource r = client.resource("http://test.example");
        r.get(ClientResponse.class);

        r = client.resource("http://test.example");
        r.get(ClientResponse.class);

        assertEquals(2, filterInTest.count);
    }

    private Client getClient() {
        Client client = Client.create();
        client.addFilter(new MockSinkFilter());
        return client;
    }

    private class RetryFilter extends ClientFilter {
        int triesLeft = 1;

        /* (non-Javadoc)
         * @see com.sun.jersey.api.client.filter.ClientFilter#handle(com.sun.jersey.api.client.ClientRequest)
         */
        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            ClientResponse response = null;
            while (triesLeft > 0) {
                --triesLeft;
                response = getNext().handle(cr);
            }
            return response;
        }
    }

    private class CountingFilter extends IdempotentClientFilter {
        public int count;
        private String key = null;

        @Override
        protected ClientResponse doHandle(ClientRequest cr) throws ClientHandlerException {
            ++count;
            return getNext().handle(cr);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.core.IdempotentClientFilter#getKey()
         */
        @Override
        protected String getKey() {
            if (key != null) {
                return key;
            }
            return super.getKey();
        }
    }

    private class MockSinkFilter extends ClientFilter {

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            ClientResponse response = Mockito.mock(ClientResponse.class);
            Mockito.when(response.getClientResponseStatus()).thenReturn(ClientResponse.Status.ACCEPTED);
            return response;
        }
    }
}
