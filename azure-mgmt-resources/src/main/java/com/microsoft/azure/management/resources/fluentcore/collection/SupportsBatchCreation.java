package com.microsoft.azure.management.resources.fluentcore.collection;


import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * Providing access to creating a batch of Azure top level resources of same type.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 * @param <ResourceT> the top level Azure resource type
 */
public interface SupportsBatchCreation<ResourceT extends Resource> {
    /**
     * Executes the create requests on a collection (batch) of resources.
     *
     * @param creatables the creatables in the batch
     * @return the batch operation result from which created resources in this batch can be accessed.
     * @throws Exception exceptions from Azure
     */
    CreatedResources<ResourceT> create(Creatable<ResourceT>... creatables) throws Exception;

    /**
     * Puts the requests to create a batch of resources into the queue and allow the HTTP client to execute it when
     * system resources are available.
     *
     * @param creatables the creatables in the batch
     * @return a handle to cancel the request
     */
    Observable<CreatedResources<ResourceT>> createAsync(Creatable<ResourceT>... creatables);
}