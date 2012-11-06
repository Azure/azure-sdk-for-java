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

package com.microsoft.windowsazure.services.media.implementation;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaEntityContract;
import com.microsoft.windowsazure.services.media.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.entities.EntityOperation;
import com.microsoft.windowsazure.services.media.entities.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * 
 *
 */
public class MediaEntityRestProxy implements MediaEntityContract {
    /** The channel. */
    private Client channel;

    /** The log. */
    static Log log = LogFactory.getLog(MediaContract.class);
    /** The filters. */
    ServiceFilter[] filters;

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param authFilter
     *            the auth filter
     * @param redirectFilter
     *            the redirect filter
     * @param versionHeadersFilter
     *            the version headers filter
     */
    @Inject
    public MediaEntityRestProxy(Client channel, OAuthFilter authFilter, RedirectFilter redirectFilter,
            VersionHeadersFilter versionHeadersFilter) {
        this.channel = channel;
        this.filters = new ServiceFilter[0];

        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
        channel.addFilter(versionHeadersFilter);
    }

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param filters
     *            the filters
     */
    public MediaEntityRestProxy(Client channel, ServiceFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public MediaEntityContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new MediaEntityRestProxy(channel, newFilters);
    }

    /**
     * Gets the channel.
     * 
     * @return the channel
     */
    public Client getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     * 
     * @param channel
     *            the new channel
     */
    public void setChannel(Client channel) {
        this.channel = channel;
    }

    /**
     * Gets the resource.
     * 
     * @param entityName
     *            the entity name
     * @return the resource
     */
    private WebResource getResource(String entityName) {
        WebResource resource = getChannel().resource(entityName);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    private Builder getResource(EntityOperation operation) {
        return getResource(operation.getUri()).type(operation.getContentType()).accept(operation.getAcceptType());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#create(com.microsoft.windowsazure.services.media.entities.EntityCreationOperation)
     */
    @Override
    public <T> T create(EntityCreationOperation<T> creator) throws ServiceException {
        return getResource(creator).post(creator.getResponseClass(), creator.getRequestContents());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#get(com.microsoft.windowsazure.services.media.entities.EntityGetOperation)
     */
    @Override
    public <T> T get(EntityGetOperation<T> getter) throws ServiceException {
        return getResource(getter).get(getter.getResponseClass());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#list(com.microsoft.windowsazure.services.media.entities.EntityListOperation)
     */
    @Override
    public <T> ListResult<T> list(EntityListOperation<T> lister) throws ServiceException {
        WebResource resource = getResource(lister.getUri());

        return resource.queryParams(lister.getQueryParameters()).type(lister.getContentType())
                .accept(lister.getAcceptType()).get(lister.getResponseGenericType());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#update(com.microsoft.windowsazure.services.media.entities.EntityUpdateOperation)
     */
    @Override
    public void update(EntityUpdateOperation updater) throws ServiceException {
        ClientResponse response = getResource(updater).header("X-HTTP-METHOD", "MERGE").post(ClientResponse.class,
                updater.getRequestContents());

        PipelineHelpers.ThrowIfNotSuccess(response);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#delete(com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation)
     */
    @Override
    public void delete(EntityDeleteOperation deleter) throws ServiceException {
        getResource(deleter.getUri()).delete();
    }

}
