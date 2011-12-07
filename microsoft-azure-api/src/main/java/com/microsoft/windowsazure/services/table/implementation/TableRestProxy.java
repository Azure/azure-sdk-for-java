/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.implementation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableServiceOptions;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class TableRestProxy implements TableContract {
    private static final String API_VERSION = "2011-08-18";
    private final HttpURLConnectionClient channel;
    private final String accountName;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ServiceFilter[] filters;
    private final SharedKeyFilter filter;

    @Inject
    public TableRestProxy(HttpURLConnectionClient channel, @Named(TableConfiguration.ACCOUNT_NAME) String accountName,
            @Named(TableConfiguration.URI) String url, SharedKeyFilter filter) {

        this.channel = channel;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.filters = new ServiceFilter[0];
        channel.addFilter(filter);
    }

    public TableRestProxy(HttpURLConnectionClient channel, ServiceFilter[] filters, String accountName, String url,
            SharedKeyFilter filter, RFC1123DateConverter dateMapper) {

        this.channel = channel;
        this.filters = filters;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = dateMapper;
    }

    @Override
    public TableContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new TableRestProxy(this.channel, newFilters, this.accountName, this.url, this.filter, this.dateMapper);
    }

    private void ThrowIfError(ClientResponse r) {
        PipelineHelpers.ThrowIfError(r);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value, defaultValue);
    }

    private Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        return PipelineHelpers.addOptionalMetadataHeader(builder, metadata);
    }

    private HashMap<String, String> getMetadataFromHeaders(ClientResponse response) {
        return PipelineHelpers.getMetadataFromHeaders(response);
    }

    private WebResource getResource(TableServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout", options.getTimeout());
        for (ServiceFilter filter : filters) {
            webResource.addFilter(new ClientFilterAdapter(filter));
        }

        return webResource;
    }

    @Override
    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return getServiceProperties(new TableServiceOptions());
    }

    @Override
    public GetServicePropertiesResult getServiceProperties(TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        GetServicePropertiesResult result = new GetServicePropertiesResult();
        result.setValue(builder.get(ServiceProperties.class));
        return result;
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        setServiceProperties(serviceProperties, new TableServiceOptions());
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties, TableServiceOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.put(serviceProperties);
    }
}
