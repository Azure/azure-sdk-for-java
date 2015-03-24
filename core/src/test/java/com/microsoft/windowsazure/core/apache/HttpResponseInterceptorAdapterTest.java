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
package com.microsoft.windowsazure.core.apache;

import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;

public class HttpResponseInterceptorAdapterTest extends MockIntegrationTestBase {
    @Test
    public void responseFilterShouldWork() throws Exception {
        ServiceResponseFilter filterFirst = new ServiceResponseFilter() {
            @Override
            public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                response.setHeader("header1", "value1");
            }
        };

        ServiceResponseFilter filterLast = new ServiceResponseFilter() {
            @Override
            public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                response.setHeader("header1", "value2");
            }
        };

        class DummyClient extends ServiceClient<DummyClient> {
            public URI baseUri;

            public DummyClient()
            {
                super(HttpClientBuilder.create(), null);
            }

            @Override
            protected DummyClient newInstance(HttpClientBuilder httpClientBuilder, ExecutorService executorService) {
                return new DummyClient();
            }
        }

        DummyClient client = new DummyClient();
        client.baseUri = new URI("http://www.microsoft.com");

        addClient((ServiceClient<?>) client, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        });
        setupTest();

        client.withResponseFilterFirst(filterFirst);
        client.withResponseFilterFirst(filterLast);

        HttpGet request = new HttpGet(client.baseUri);
        HttpResponse response = null;
        try {
            response = client.getHttpClient().execute(request);
        }
        finally {
            assertEquals(response.getFirstHeader("header1").getValue(), "value1");
            resetTest();
        }
    }
}
