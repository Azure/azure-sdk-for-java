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

package com.microsoft.windowsazure.services.core;

import static org.junit.Assert.*;

import org.junit.Before;
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
    Client client;
    RetryFilter retry;
    CountingFilter filter1;
    CountingFilter filter2;

    @Before
    public void setup() {
        client = Client.create();
        client.addFilter(new MockSinkFilter());

        retry = new RetryFilter();
        filter1 = new CountingFilter();
        filter2 = new CountingFilter();
        filter2.key = "secondfilter";
    }

    @Test
    public void filterRunsFirstTime() throws Exception {
        client.addFilter(filter1);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());

        assertEquals(1, filter1.count);
    }

    @Test
    public void filterRunsOnlyOnceWhenRequestIsRetried() throws Exception {
        retry.triesLeft = 3;
        client.addFilter(filter1);
        client.addFilter(retry);

        WebResource r = client.resource("http://test.example");
        ClientResponse response = r.get(ClientResponse.class);

        assertEquals(ClientResponse.Status.ACCEPTED, response.getClientResponseStatus());
        assertEquals(1, filter1.count);
    }

    @Test
    public void multipleIdempotentFiltersRunFirstTime() throws Exception {
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
        retry.triesLeft = 4;

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
        client.addFilter(filter1);

        WebResource r = client.resource("http://test.example");
        r.get(ClientResponse.class);

        r = client.resource("http://test.example");
        r.get(ClientResponse.class);

        assertEquals(2, filter1.count);
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
