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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaEntityContract;
import com.microsoft.windowsazure.services.media.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.models.ListOptions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

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

    private WebResource getResource(String entityName, ListOptions options) {
        WebResource resource = getResource(entityName);
        if (options != null) {
            resource = resource.queryParams(options.getQueryParameters());
        }
        return resource;
    }

    /**
     * Gets the resource.
     * 
     * @param entityType
     *            the entity type
     * @param entityId
     *            the entity id
     * @return the resource
     * @throws ServiceException
     *             the service exception
     */
    private WebResource getResource(String entityType, String entityId) throws ServiceException {
        String escapedEntityId = null;
        try {
            escapedEntityId = URLEncoder.encode(entityId, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new ServiceException(e);
        }
        String entityPath = String.format("%s(\'%s\')", entityType, escapedEntityId);

        return getResource(entityPath);
    }

    /**
     * Merge request.
     * 
     * @param <T>
     *            the generic type
     * @param entityType
     *            the entity type
     * @param entityId
     *            the entity id
     * @param c
     *            the c
     * @param requestEntity
     *            the request entity
     * @return the t
     * @throws ServiceException
     *             the service exception
     */
    private <T> T mergeRequest(String entityType, String entityId, java.lang.Class<T> c, java.lang.Object requestEntity)
            throws ServiceException {
        WebResource resource = getResource(entityType, entityId);
        WebResource.Builder builder = resource.getRequestBuilder();
        builder = builder.type(MediaType.APPLICATION_ATOM_XML).accept(MediaType.APPLICATION_ATOM_XML)
                .header("X-HTTP-Method", "MERGE");
        return builder.post(c, requestEntity);
    }

    /**
     * 
     */
    public MediaEntityRestProxy() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#create(com.microsoft.windowsazure.services.media.entities.EntityCreationOperation)
     */
    @Override
    public <T> T create(EntityCreationOperation<T> creator) throws ServiceException {
        WebResource resource = getResource(creator.getUri());

        return resource.type(creator.getContentType()).accept(creator.getAcceptType())
                .post(creator.getResponseClass(), creator.getRequestContents());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#get(com.microsoft.windowsazure.services.media.entities.EntityGetOperation)
     */
    @Override
    public <T> T get(EntityGetOperation<T> getter) throws ServiceException {
        WebResource resource = getResource(getter.getUri());

        return resource.type(getter.getContentType()).accept(getter.getAcceptType()).get(getter.getResponseClass());
    }
}
