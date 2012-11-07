package com.microsoft.windowsazure.services.media.implementation.entities;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class EntityRestProxy implements EntityContract {

    /** The channel. */
    private final Client channel;
    /** The filters. */
    private final ServiceFilter[] filters;

    public EntityRestProxy(Client channel, ServiceFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
    }

    protected Client getChannel() {
        return channel;
    }

    protected ServiceFilter[] getFilters() {
        return filters;
    }

    /**
     * Gets the resource.
     * 
     * @param entityName
     *            the entity name
     * @return the resource
     */
    private WebResource getResource(String entityName) {
        WebResource resource = channel.resource(entityName);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    private Builder getResource(EntityOperation operation) {
        return getResource(operation.getUri()).type(operation.getContentType()).accept(operation.getAcceptType());
    }

    @Override
    public <T> T create(EntityCreationOperation<T> creator) throws ServiceException {
        return getResource(creator).post(creator.getResponseClass(), creator.getRequestContents());
    }

    @Override
    public <T> T get(EntityGetOperation<T> getter) throws ServiceException {
        return getResource(getter).get(getter.getResponseClass());
    }

    @Override
    public <T> ListResult<T> list(EntityListOperation<T> lister) throws ServiceException {
        return getResource(lister.getUri()).queryParams(lister.getQueryParameters()).type(lister.getContentType())
                .accept(lister.getAcceptType()).get(lister.getResponseGenericType());
    }

    @Override
    public void update(EntityUpdateOperation updater) throws ServiceException {
        ClientResponse response = getResource(updater).header("X-HTTP-METHOD", "MERGE").post(ClientResponse.class,
                updater.getRequestContents());

        PipelineHelpers.ThrowIfNotSuccess(response);
    }

    @Override
    public void delete(EntityDeleteOperation deleter) throws ServiceException {
        getResource(deleter.getUri()).delete();
    }

}