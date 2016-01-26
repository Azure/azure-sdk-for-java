/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.core.pipeline.apache.HttpRequestInterceptorBackAdapter;
import com.microsoft.windowsazure.core.pipeline.apache.HttpRequestInterceptorFrontAdapter;
import com.microsoft.windowsazure.core.pipeline.apache.HttpResponseInterceptorBackAdapter;
import com.microsoft.windowsazure.core.pipeline.apache.HttpResponseInterceptorFrontAdapter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public abstract class ServiceClient<TClient> implements
        FilterableService<TClient>, Closeable {
    private final ExecutorService executorService;

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    private CloseableHttpClient httpClient;
    private HttpRequestInterceptorFrontAdapter httpRequestInterceptorFrontAdapter;
    private HttpRequestInterceptorBackAdapter httpRequestInterceptorBackAdapter;
    private HttpResponseInterceptorFrontAdapter httpResponseInterceptorFrontAdapter;
    private HttpResponseInterceptorBackAdapter httpResponseInterceptorBackAdapter;

    public CloseableHttpClient getHttpClient() {
        if (this.httpClient == null) {
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            if ((proxyHost != null) && (proxyPort != null)) {
                httpClientBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
            }

            this.httpClient = httpClientBuilder.build();
        }

        return this.httpClient;
    }

    private final HttpClientBuilder httpClientBuilder;

    protected ServiceClient(HttpClientBuilder httpClientBuilder,
            ExecutorService executorService) {
        this.httpClientBuilder = httpClientBuilder;
        this.executorService = executorService;
        this.httpClientBuilder.addInterceptorFirst(new UserAgentFilterAdapter(new UserAgentFilter()));
    }

    protected abstract TClient newInstance(HttpClientBuilder httpClientBuilder,
            ExecutorService executorService);

    @Override
    public TClient withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        if (httpRequestInterceptorFrontAdapter == null) {
            httpRequestInterceptorFrontAdapter = new HttpRequestInterceptorFrontAdapter();
            httpClientBuilder.addInterceptorFirst(httpRequestInterceptorFrontAdapter);
        }
        httpRequestInterceptorFrontAdapter.addFront(serviceRequestFilter);
        return this.newInstance(httpClientBuilder, executorService);
    }

    @Override
    public TClient withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        if (httpRequestInterceptorBackAdapter == null) {
            httpRequestInterceptorBackAdapter = new HttpRequestInterceptorBackAdapter();
            httpClientBuilder.addInterceptorLast(httpRequestInterceptorBackAdapter);
        }
        httpRequestInterceptorBackAdapter.addBack(serviceRequestFilter);
        return this.newInstance(httpClientBuilder, executorService);
    }

    @Override
    public TClient withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        if (httpResponseInterceptorFrontAdapter == null) {
            httpResponseInterceptorFrontAdapter = new HttpResponseInterceptorFrontAdapter();
            httpClientBuilder.addInterceptorFirst(httpResponseInterceptorFrontAdapter);
        }
        httpResponseInterceptorFrontAdapter.addFront(serviceResponseFilter);
        return this.newInstance(httpClientBuilder, executorService);
    }

    @Override
    public TClient withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        if (httpResponseInterceptorBackAdapter == null) {
            httpResponseInterceptorBackAdapter = new HttpResponseInterceptorBackAdapter();
            httpClientBuilder.addInterceptorLast(httpResponseInterceptorBackAdapter);
        }
        httpResponseInterceptorBackAdapter.addBack(serviceResponseFilter);
        return this.newInstance(httpClientBuilder, executorService);
    }

    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
