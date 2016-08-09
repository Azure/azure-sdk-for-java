package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
    public CreatedResources<T> create(Creatable<T> ... creatables) throws Exception {
        ServiceCall<CreatedResources<T>> serviceCall = createAsync(null, creatables);
        serviceCall.wait();
        return serviceCall.get().getBody();
    }

    @Override
    public ServiceCall<CreatedResources<T>> createAsync(final ServiceCallback<CreatedResources<T>> callback, Creatable<T> ... creatables) {
        rootResource = new BatchRootResourceImpl<>(); // Imp: Creates a new BatchRootResourceImpl for each batch
        rootResource.addCreatableDependencies(creatables);

        final BatchServiceCall batchServiceCall = new BatchServiceCall();
        ServiceCall<BatchRootResource<T>> serviceCall = rootResource.createAsync(batchServiceCall.wrapCallback(callback));
        batchServiceCall.setInnerServiceCall(serviceCall);
        return batchServiceCall;
    }

    /**
     * Implements {@link CreatedResources}.
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatedResourcesImpl<ResourceT extends Resource> implements CreatedResources<ResourceT> {
        private BatchRootResource<ResourceT> batchRootResource;

        private final List<ResourceT> list;

        CreatedResourcesImpl(BatchRootResource<ResourceT> batchRootResource) {
            this.batchRootResource = batchRootResource;
            this.list = this.batchRootResource.createdTopLevelResources();
        }

        @Override
        public Resource createdRelatedResource(String key) {
            return this.batchRootResource.createdRelatedResource(key);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<ResourceT> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean add(ResourceT resourceT) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends ResourceT> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends ResourceT> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceT get(int index) {
            return this.list.get(index);
        }

        @Override
        public ResourceT set(int index, ResourceT element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, ResourceT element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceT remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            return this.list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return this.list.lastIndexOf(o);
        }

        @Override
        public ListIterator<ResourceT> listIterator() {
            return this.list.listIterator();
        }

        @Override
        public ListIterator<ResourceT> listIterator(int index) {
            return this.list.listIterator(index);
        }

        @Override
        public List<ResourceT> subList(int fromIndex, int toIndex) {
            return this.list.subList(fromIndex, toIndex);
        }
    }

    /**
     *  The local dummy root resource for the batch of creatable resources in a batch.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    interface BatchRootResource<ResourceT extends Resource> extends Resource {
        List<ResourceT> createdTopLevelResources();
        Resource createdRelatedResource(String key);
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
        public List<ResourceT> createdTopLevelResources() {
            List<ResourceT> resources = new ArrayList<>();
            for (String resourceKey : keys) {
                resources.add((ResourceT) creatorTaskGroup().createdResource(resourceKey));
            }
            return Collections.unmodifiableList(resources);
        }

        @Override
        public Resource createdRelatedResource(String key) {
            return (Resource) creatorTaskGroup().createdResource(key);
        }

        void addCreatableDependencies(Creatable<T> ... creatables) {
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

    /**
     * Represents a collection of in-progress service calls in a batch.
     */
    private class BatchServiceCall extends ServiceCall<CreatedResources<T>> {
        private ServiceCall<BatchRootResource<T>> innerServiceCall;

        /**
         * Creates BatchServiceCall.
         */
        BatchServiceCall() {
            super(null);
        }

        /**
         * Sets the inner service call.
         *
         * @param inner the service call to wrap
         */
        public void setInnerServiceCall(ServiceCall<BatchRootResource<T>> inner) {
            this.innerServiceCall = inner;
        }

        /**
         * Cancels all the service calls currently executing.
         */
        public void cancel() {
            this.innerServiceCall.cancel();
        }

        /**
         * @return true if the call has been canceled; false otherwise.
         */
        public boolean isCancelled() {
            return this.innerServiceCall.isCancelled();
        }

        ServiceCallback<BatchRootResource<T>> wrapCallback(final ServiceCallback<CreatedResources<T>> innerCallback) {
            final BatchServiceCall self = this;
            return new ServiceCallback<BatchRootResource<T>>() {
                @Override
                public void failure(Throwable t) {
                    self.failure(t);
                    if (innerCallback != null) {
                        innerCallback.failure((t));
                    }
                }

                @Override
                public void success(ServiceResponse<BatchRootResource<T>> result) {
                    self.success(new ServiceResponse<CreatedResources<T>>(new CreatedResourcesImpl<>(result.getBody()), null));
                    if (innerCallback != null) {
                        innerCallback.success(new ServiceResponse<CreatedResources<T>>(new CreatedResourcesImpl<>(result.getBody()), null));
                    }
                }
            };
        }
    }
}