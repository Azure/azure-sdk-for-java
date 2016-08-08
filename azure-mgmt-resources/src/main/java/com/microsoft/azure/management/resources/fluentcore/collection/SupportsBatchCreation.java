package com.microsoft.azure.management.resources.fluentcore.collection;


import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.BatchCreateOperationResult;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * Providing access to creating a batch of Azure top level resources of same type.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 * @param <ResourceT> the top level Azure resource type
 */
public interface SupportsBatchCreation<ResourceT extends Resource> {
    /**
     * Executes the create requests on a collection of resources.
     *
     * @param creatable  the first creatable in the batch
     * @param creatables the rest of the creatables in the batch
     * @return the batch resource from which created resources in this batch can be accessed.
     * @throws Exception exceptions from Azure
     */
    BatchCreateOperationResult<ResourceT> create(Creatable<ResourceT> creatable, Creatable<ResourceT>... creatables) throws Exception;

    /**
     * Puts the request into the queue and allow the HTTP client to execute it when system resources are available.
     *
     * @param callback the callback to handle success and failure
     * @param creatable the first creatable in the batch
     * @param creatables the rest of the creatables in the batch
     * @return a handle to cancel the request
     */
    ServiceCall createAsync(ServiceCallback<BatchCreateOperationResult<ResourceT>> callback, Creatable<ResourceT> creatable, Creatable<ResourceT>... creatables);
}