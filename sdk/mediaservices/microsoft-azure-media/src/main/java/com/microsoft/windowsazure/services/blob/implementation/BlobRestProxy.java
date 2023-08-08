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
package com.microsoft.windowsazure.services.blob.implementation;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.windowsazure.core.RFC1123DateConverter;
import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterResponseAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.HttpURLConnectionClient;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.services.blob.BlobConfiguration;
import com.microsoft.windowsazure.services.blob.BlobContract;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;

public class BlobRestProxy extends BlobOperationRestProxy implements
        BlobContract {
    private final SharedKeyFilter sharedKeyFilter;

    @Inject
    public BlobRestProxy(HttpURLConnectionClient channel,
            @Named(BlobConfiguration.ACCOUNT_NAME) String accountName,
            @Named(BlobConfiguration.URI) String url,
            SharedKeyFilter sharedKeyFilter, UserAgentFilter userAgentFilter) {
        super(channel, accountName, url);

        this.sharedKeyFilter = sharedKeyFilter;

        channel.addFilter(sharedKeyFilter);
        channel.addFilter(new ClientFilterRequestAdapter(userAgentFilter));
    }

    public BlobRestProxy(Client client, ClientFilter[] filters,
            String accountName, String url, SharedKeyFilter sharedKeyFilter,
            RFC1123DateConverter dateMapper) {
        super(client, filters, accountName, url, dateMapper);

        this.sharedKeyFilter = sharedKeyFilter;
    }

    @Override
    public BlobContract withFilter(ServiceFilter filter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterAdapter(filter);
        return new BlobRestProxy(getChannel(), newFilters, getAccountName(),
                getUrl(), this.sharedKeyFilter, getDateMapper());
    }

    @Override
    public BlobContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterRequestAdapter(serviceRequestFilter);
        return new BlobRestProxy(getChannel(), newFilters, getAccountName(),
                getUrl(), this.sharedKeyFilter, getDateMapper());
    }

    @Override
    public BlobContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterRequestAdapter(
                serviceRequestFilter);
        return new BlobRestProxy(getChannel(), newFilters, getAccountName(),
                getUrl(), this.sharedKeyFilter, getDateMapper());
    }

    @Override
    public BlobContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterResponseAdapter(serviceResponseFilter);
        return new BlobRestProxy(getChannel(), newFilters, getAccountName(),
                getUrl(), this.sharedKeyFilter, getDateMapper());
    }

    @Override
    public BlobContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterResponseAdapter(
                serviceResponseFilter);
        return new BlobRestProxy(getChannel(), newFilters, getAccountName(),
                getUrl(), this.sharedKeyFilter, getDateMapper());
    }

}
