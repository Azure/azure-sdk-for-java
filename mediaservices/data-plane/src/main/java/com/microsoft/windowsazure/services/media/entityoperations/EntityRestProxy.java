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

package com.microsoft.windowsazure.services.media.entityoperations;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * The Class EntityRestProxy.
 */
public abstract class EntityRestProxy implements EntityContract {

    /** The executor service. */
    private final ExecutorService executorService;
    /** The channel. */
    private final Client channel;
    /** The filters. */
    private final ClientFilter[] filters;

    /**
     * Instantiates a new entity rest proxy.
     * 
     * @param channel
     *            the channel
     * @param filters
     *            the filters
     */
    public EntityRestProxy(Client channel, ClientFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Gets the channel.
     * 
     * @return the channel
     */
    protected Client getChannel() {
        return channel;
    }

    /**
     * Gets the executor service.
     * 
     * @return the executor service
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Gets the filters.
     * 
     * @return the filters
     */
    protected ClientFilter[] getFilters() {
        return filters;
    }

    /**
     * Get the proxy data to pass to operations.
     * 
     * @return The proxy data.
     */
    protected abstract EntityProxyData createProxyData();

    /**
     * Gets the resource.
     * 
     * @param entityName
     *            the entity name
     * @return the resource
     */
    private WebResource getResource(String entityName) {
        WebResource resource = channel.resource(entityName);
        for (ClientFilter filter : filters) {
            resource.addFilter(filter);
        }
        return resource;
    }

    /**
     * Gets the resource.
     * 
     * @param operation
     *            the operation
     * @return the resource
     * @throws ServiceException
     *             the service exception
     */
    private Builder getResource(EntityOperation operation)
            throws ServiceException {
        return getResource(operation.getUri()).type(operation.getContentType())
                .accept(operation.getAcceptType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #create(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityCreateOperation)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(EntityCreateOperation<T> creator)
            throws ServiceException {
        creator.setProxyData(createProxyData());
        Object rawResponse = getResource(creator).post(
                creator.getResponseClass(), creator.getRequestContents());
        Object processedResponse = creator.processResponse(rawResponse);
        return (T) processedResponse;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #get(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityGetOperation)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(EntityGetOperation<T> getter) throws ServiceException {
        getter.setProxyData(createProxyData());
        Object rawResponse = getResource(getter).get(getter.getResponseClass());
        Object processedResponse = getter.processResponse(rawResponse);
        return (T) processedResponse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #list(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityListOperation)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ListResult<T> list(EntityListOperation<T> lister)
            throws ServiceException {
        lister.setProxyData(createProxyData());
        Object rawResponse = getResource(lister.getUri())
                .queryParams(lister.getQueryParameters())
                .type(lister.getContentType()).accept(lister.getAcceptType())
                .get(lister.getResponseGenericType());
        Object processedResponse = lister.processResponse(rawResponse);
        return (ListResult<T>) processedResponse;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #update(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityUpdateOperation)
     */
    @Override
    public String update(EntityUpdateOperation updater) throws ServiceException {
        updater.setProxyData(createProxyData());
        ClientResponse clientResponse = getResource(updater).header("X-HTTP-METHOD",
                "MERGE").post(ClientResponse.class,
                updater.getRequestContents());
        PipelineHelpers.throwIfNotSuccess(clientResponse);
        updater.processResponse(clientResponse);
        if (clientResponse.getHeaders().containsKey("operation-id")) {
            List<String> operationIds = clientResponse.getHeaders().get("operation-id");
            if (operationIds.size() >= 0) {
                return operationIds.get(0);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #delete(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityDeleteOperation)
     * @return operation-id if any otherwise null.
     */
    @Override
    public String delete(EntityDeleteOperation deleter) throws ServiceException {
        deleter.setProxyData(createProxyData());
        ClientResponse clientResponse =  getResource(deleter.getUri()).delete(ClientResponse.class);
        PipelineHelpers.throwIfNotSuccess(clientResponse);
        if (clientResponse.getHeaders().containsKey("operation-id")) {
            List<String> operationIds = clientResponse.getHeaders().get("operation-id");
            if (operationIds.size() >= 0) {
                return operationIds.get(0);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #action(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation)
     */
    @Override
    public <T> T action(EntityTypeActionOperation<T> entityTypeActionOperation)
            throws ServiceException {
        entityTypeActionOperation.setProxyData(createProxyData());
        Builder webResource = getResource(entityTypeActionOperation.getUri())
                .queryParams(entityTypeActionOperation.getQueryParameters())
                .accept(entityTypeActionOperation.getAcceptType())
                .accept(MediaType.APPLICATION_XML_TYPE)
                .entity(entityTypeActionOperation.getRequestContents(),
                        MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML);

        ClientResponse clientResponse = webResource.method(
                entityTypeActionOperation.getVerb(), ClientResponse.class);
        return entityTypeActionOperation.processTypeResponse(clientResponse);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityContract
     * #action(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation)
     */
    @Override
    public String action(EntityActionOperation entityActionOperation)
            throws ServiceException {
        ClientResponse clientResponse = getActionClientResponse(entityActionOperation);
        entityActionOperation.processResponse(clientResponse);
        
        //PipelineHelpers.throwIfNotSuccess(clientResponse);
        
        if (clientResponse.getHeaders().containsKey("operation-id")) {
            List<String> operationIds = clientResponse.getHeaders().get("operation-id");
            if (operationIds.size() >= 0) {
                return operationIds.get(0);
            }
        }
        return null;
    }

    /**
     * Gets the action client response.
     * 
     * @param entityActionOperation
     *            the entity action operation
     * @return the action client response
     */
    private ClientResponse getActionClientResponse(
            EntityActionOperation entityActionOperation) {
        entityActionOperation.setProxyData(createProxyData());
        Builder webResource = getResource(entityActionOperation.getUri())
                .queryParams(entityActionOperation.getQueryParameters())
                .accept(entityActionOperation.getAcceptType())
                .accept(MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML_TYPE);
        if (entityActionOperation.getRequestContents() != null) {
            webResource = webResource.entity(
                    entityActionOperation.getRequestContents(),
                    entityActionOperation.getContentType());
        }
        return webResource.method(entityActionOperation.getVerb(),
                ClientResponse.class);
    }
}
