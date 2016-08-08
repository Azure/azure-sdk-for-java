package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.model.BatchCreateOperationResult;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for creatable resource collection, i.e. those where the member of the collection is of Resource
 * type {@link com.microsoft.azure.Resource } and are creatable.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class CreatableResourcesImpl<T extends Resource, ImplT extends T, InnerT>
        extends CreatableWrappersImpl<T, ImplT, InnerT>
        implements SupportsBatchCreation<T> {

    /**
     * the root resource that groups batch of creatable resources in the collection.
     */
    private BatchRootResourceImpl<T> rootResource;

    protected CreatableResourcesImpl() {
    }

    @Override
    public BatchCreateOperationResult<T> create(Creatable<T> creatable, Creatable<T> ... creatables) throws Exception {
        rootResource = new BatchRootResourceImpl<>(); // Creates a new batchResource each batch
        rootResource.addCreatableDependencies(creatable, creatables);
        rootResource.create();
        return new BatchCreateOperationResultImpl<>(rootResource);
    }

    @Override
    public ServiceCall createAsync(final ServiceCallback<BatchCreateOperationResult<T>> callback, Creatable<T> creatable, Creatable<T> ... creatables) {
        rootResource = new BatchRootResourceImpl<>(); // Creates a new batchResource each batch
        rootResource.addCreatableDependencies(creatable, creatables);
        return rootResource.createAsync(new ServiceCallback<BatchRootResource<T>>() {
            @Override
            public void failure(Throwable t) {
                callback.failure((t));
            }

            @Override
            public void success(ServiceResponse<BatchRootResource<T>> result) {
                callback.success(new ServiceResponse<BatchCreateOperationResult<T>>(new BatchCreateOperationResultImpl<>(result.getBody()), null));
            }
        });
    }

    /**
     * Implements {@link BatchCreateOperationResult}.
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class BatchCreateOperationResultImpl<ResourceT extends Resource> implements BatchCreateOperationResult<ResourceT> {
        private BatchRootResource<ResourceT> batchRootResource;

        BatchCreateOperationResultImpl(BatchRootResource<ResourceT> localResource) {
            this.batchRootResource = localResource;
        }

        @Override
        public List<ResourceT> resources() {
            return batchRootResource.resources();
        }
    }

    /**
     *  The local dummy root resource for the batch of creatable resources in a batch.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    interface BatchRootResource<ResourceT extends Resource> extends Resource {
        List<ResourceT> resources();
    }

    /**
     * Implementation of {@link BatchRootResource}.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class BatchRootResourceImpl<ResourceT extends Resource>
            extends CreatableImpl<BatchRootResource<ResourceT>, Object, BatchRootResourceImpl, Resource>
            implements BatchRootResource<ResourceT> {
        /**
         * Collection of keys of top level resources in this batch.
         */
        private List<String> keys;

        BatchRootResourceImpl() {
            super("BatchRootResource", null);
            this.keys = new ArrayList<>();
        }

        @Override
        public List<ResourceT> resources() {
            List<ResourceT> resources = new ArrayList<>();
            for (String resourceKey : keys) {
                resources.add((ResourceT) creatorTaskGroup().createdResource(resourceKey));
            }
            return Collections.unmodifiableList(resources);
        }

        void addCreatableDependencies(Creatable<T> creatable, Creatable<T> ... creatables) {
            this.keys.add(creatable.key());
            this.addCreatableDependency((creatable));
            for (Creatable<T> item : creatables) {
                this.keys.add(item.key());
                this.addCreatableDependency((item));
            }
        }

        @Override
        public ServiceCall createResourceAsync(ServiceCallback<Resource> serviceCallback) {
            serviceCallback.success(new ServiceResponse<Resource>(this, null));
            return null;
        }

        @Override
        public Resource createResource() throws Exception {
            return this;
        }

        @Override
        public BatchRootResource refresh() throws Exception {
            return null;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public String type() {
            return null;
        }

        @Override
        public String regionName() {
            return null;
        }

        @Override
        public Region region() {
            return null;
        }

        @Override
        public Map<String, String> tags() {
            return null;
        }
    }
}