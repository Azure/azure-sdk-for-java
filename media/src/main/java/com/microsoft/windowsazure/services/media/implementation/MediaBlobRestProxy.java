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

import java.util.Arrays;

import com.microsoft.windowsazure.core.RFC1123DateConverter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterResponseAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.services.blob.BlobContract;
import com.microsoft.windowsazure.services.blob.implementation.BlobOperationRestProxy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Rest proxy for blob operations that's specialized for working with the blobs
 * created by and for Media Services storage.
 * 
 */
class MediaBlobRestProxy extends BlobOperationRestProxy {
    private final SASTokenFilter tokenFilter;

    /**
     * Construct instance of MediaBlobRestProxy with given parameters.
     * 
     * @param channel
     *            Jersey Client object used to communicate with blob service
     * @param accountName
     *            Account name for blob storage
     * @param url
     *            URL for blob storage
     * @param tokenFilter
     *            filter used to add SAS tokens to requests.
     */
    public MediaBlobRestProxy(Client channel, String accountName, String url,
            SASTokenFilter tokenFilter) {
        super(channel, accountName, url);

        this.tokenFilter = tokenFilter;
        channel.addFilter(tokenFilter);
    }

    /**
     * Construct instance of MediaBlobRestProxy with given parameters.
     * 
     * @param channel
     *            Jersey Client object used to communicate with blob service
     * @param filters
     *            Additional ServiceFilters to manipulate requests and responses
     * @param accountName
     *            Account name for blob storage
     * @param url
     *            URL for blob storage
     * @param dateMapper
     *            date conversion helper object
     */
    public MediaBlobRestProxy(Client channel, ClientFilter[] filters,
            String accountName, String url, SASTokenFilter tokenFilter,
            RFC1123DateConverter dateMapper) {
        super(channel, filters, accountName, url, dateMapper);

        this.tokenFilter = tokenFilter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.blob.implementation.
     * BlobOperationRestProxy
     * #withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public BlobContract withFilter(ServiceFilter filter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterAdapter(filter);
        return new MediaBlobRestProxy(getChannel(), newFilters,
                getAccountName(), getUrl(), this.tokenFilter, getDateMapper());
    }

    @Override
    public BlobContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterRequestAdapter(serviceRequestFilter);
        return new MediaBlobRestProxy(getChannel(), newFilters,
                getAccountName(), getUrl(), this.tokenFilter, getDateMapper());
    }

    @Override
    public BlobContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterRequestAdapter(
                serviceRequestFilter);
        return new MediaBlobRestProxy(getChannel(), newFilters,
                getAccountName(), getUrl(), this.tokenFilter, getDateMapper());
    }

    @Override
    public BlobContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterResponseAdapter(serviceResponseFilter);
        return new MediaBlobRestProxy(getChannel(), newFilters,
                getAccountName(), getUrl(), this.tokenFilter, getDateMapper());
    }

    @Override
    public BlobContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterResponseAdapter(
                serviceResponseFilter);
        return new MediaBlobRestProxy(getChannel(), newFilters,
                getAccountName(), getUrl(), this.tokenFilter, getDateMapper());
    }

}
