/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.apache.HttpRequestInterceptorAdapter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.apache.HttpResponseInterceptorAdapter;
import java.util.concurrent.ExecutorService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public abstract class ServiceClient<TClient> implements FilterableService<TClient> {
    private final ExecutorService executorService;
    
    public ExecutorService getExecutorService() { return this.executorService; }

    protected CloseableHttpClient httpClient;
    
    public CloseableHttpClient getHttpClient()
    { 
        if (this.httpClient == null)
        {
            this.httpClient = httpClientBuilder.build();
        }
        
        return this.httpClient;
    }
    
    private final HttpClientBuilder httpClientBuilder;

    protected ServiceClient(HttpClientBuilder httpClientBuilder, ExecutorService executorService)
    {
        this.httpClientBuilder = httpClientBuilder;
        this.executorService = executorService;
    }
    
    protected abstract TClient newInstance(
            HttpClientBuilder httpClientBuilder,
            ExecutorService executorService);
    
    @Override
    public TClient withRequestFilterFirst(ServiceRequestFilter serviceRequestFilter)
    {
        httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptorAdapter(serviceRequestFilter));
        return this.newInstance(httpClientBuilder, executorService);
    }
    
    @Override
    public TClient withRequestFilterLast(ServiceRequestFilter serviceRequestFilter)
    {
        httpClientBuilder.addInterceptorLast(new HttpRequestInterceptorAdapter(serviceRequestFilter));
        return this.newInstance(httpClientBuilder, executorService);
    }
    
    @Override
    public TClient withResponseFilterFirst(ServiceResponseFilter serviceResponseFilter)
    {
        httpClientBuilder.addInterceptorFirst(new HttpResponseInterceptorAdapter(serviceResponseFilter));
        return this.newInstance(httpClientBuilder, executorService);
    }
    
    @Override
    public TClient withResponseFilterLast(ServiceResponseFilter serviceResponseFilter)
    {
        httpClientBuilder.addInterceptorLast(new HttpResponseInterceptorAdapter(serviceResponseFilter));
        return this.newInstance(httpClientBuilder, executorService);
    }
}
