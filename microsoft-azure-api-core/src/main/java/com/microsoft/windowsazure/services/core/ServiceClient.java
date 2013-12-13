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
package com.microsoft.windowsazure.services.core;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.impl.client.CloseableHttpClient;

public class ServiceClient<TClient> implements FilterableService<TClient> {
    /** The filters. */
    private ServiceFilter[] filters;
    
    private ExecutorService executorService;
    
    public ExecutorService getExecutorService() { return this.executorService; }

    protected CloseableHttpClient httpClient;
    
    public CloseableHttpClient getHttpClient() { return this.httpClient; }
    
    public ServiceClient()
    {
        this.executorService = Executors.newCachedThreadPool();
    }
    
    @Override
    public TClient withFilter(ServiceFilter filter)
    {
        ServiceFilter[] newFilters = Arrays.copyOf(this.filters, this.filters.length + 1);
        newFilters[this.filters.length] = filter;
        
        //return new ManagementRestProxy(channel, newFilters, uri, subscriptionId);
        return null;
    }
}
