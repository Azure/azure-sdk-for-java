package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

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

    protected CreatableResourcesImpl() {
    }

    @Override
    @SafeVarargs
    public final CreatedResources<T> create(Creatable<T> ... creatables) throws Exception {
        return BlockingObservable.from(createAsync(creatables)).single();
    }

    @Override
    public final CreatedResources<T> create(List<Creatable<T>> creatables) throws Exception {
        return BlockingObservable.from(createAsync(creatables)).single();
    }

    @Override
    @SafeVarargs
    public final Observable<CreatedResources<T>> createAsync(Creatable<T> ... creatables) {
        CreatableResourcesRootImpl<T> rootResource = new CreatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);

        return rootResource.createAsync()
                .map(new Func1<CreatableResourcesRoot<T>, CreatedResources<T>>() {
                    @Override
                    public CreatedResources<T> call(CreatableResourcesRoot<T> tCreatableResourcesRoot) {
                        return new CreatedResourcesImpl<T>(tCreatableResourcesRoot);
                    }
                });
    }

    @Override
    public final Observable<CreatedResources<T>> createAsync(List<Creatable<T>> creatables) {
        CreatableResourcesRootImpl<T> rootResource = new CreatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);

        return rootResource.createAsync()
                .map(new Func1<CreatableResourcesRoot<T>, CreatedResources<T>>() {
                    @Override
                    public CreatedResources<T> call(CreatableResourcesRoot<T> tCreatableResourcesRoot) {
                        return new CreatedResourcesImpl<T>(tCreatableResourcesRoot);
                    }
                });
    }

    @Override
    @SafeVarargs
    public final ServiceCall<CreatedResources<T>> createAsync(final ServiceCallback<CreatedResources<T>> callback, Creatable<T>... creatables) {
        return ServiceCall.create(createAsync(creatables).map(new Func1<CreatedResources<T>, ServiceResponse<CreatedResources<T>>>() {
            @Override
            public ServiceResponse<CreatedResources<T>> call(CreatedResources<T> ts) {
                // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map can be removed
                return new ServiceResponse<>(ts, null);
            }
        }), callback);
    }

    @Override
    public final ServiceCall<CreatedResources<T>> createAsync(final ServiceCallback<CreatedResources<T>> callback, List<Creatable<T>> creatables) {
        return ServiceCall.create(createAsync(creatables).map(new Func1<CreatedResources<T>, ServiceResponse<CreatedResources<T>>>() {
            @Override
            public ServiceResponse<CreatedResources<T>> call(CreatedResources<T> ts) {
                // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map can be removed
                return new ServiceResponse<>(ts, null);
            }
        }), callback);
    }

    /**
     * Implements {@link CreatedResources}.
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatedResourcesImpl<ResourceT extends Resource> implements CreatedResources<ResourceT> {
        private CreatableResourcesRoot<ResourceT> creatableResourcesRoot;
        private final List<ResourceT> list;

        CreatedResourcesImpl(CreatableResourcesRoot<ResourceT> creatableResourcesRoot) {
            this.creatableResourcesRoot = creatableResourcesRoot;
            this.list = this.creatableResourcesRoot.createdTopLevelResources();
        }

        @Override
        public Resource createdRelatedResource(String key) {
            return this.creatableResourcesRoot.createdRelatedResource(key);
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
     * The local root resource that is used as dummy parent resource for the batch creatable resources
     * added via {@link CreatableResourcesImpl#create} or {@link CreatableResourcesImpl#createAsync}.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    interface CreatableResourcesRoot<ResourceT extends Resource> extends Resource {
        List<ResourceT> createdTopLevelResources();
        Resource createdRelatedResource(String key);
    }

    /**
     * Implementation of {@link CreatableResourcesRoot}.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatableResourcesRootImpl<ResourceT extends Resource>
            extends CreatableImpl<CreatableResourcesRoot<ResourceT>, Object, CreatableResourcesRootImpl<ResourceT>>
            implements CreatableResourcesRoot<ResourceT> {
        /**
         * Collection of keys of top level resources in this batch.
         */
        private List<String> keys;

        CreatableResourcesRootImpl() {
            super("CreatableResourcesRoot", null);
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
            return creatorTaskGroup().createdResource(key);
        }

        void addCreatableDependencies(Creatable<T> ... creatables) {
            for (Creatable<T> item : creatables) {
                this.keys.add(item.key());
                this.addCreatableDependency((item));
            }
        }

        void addCreatableDependencies(List<Creatable<T>> creatables) {
            for (Creatable<T> item : creatables) {
                this.keys.add(item.key());
                this.addCreatableDependency((item));
            }
        }

        @Override
        public Observable<CreatableResourcesRoot<ResourceT>> createResourceAsync() {
            return Observable.just((CreatableResourcesRoot<ResourceT>) this);
        }

        @Override
        public CreatableResourcesRoot<ResourceT> createResource() throws Exception {
            return this;
        }

        // Below overrides returns null as this is not a real resource in Azure
        // but a dummy resource representing parent of a batch of creatable Azure
        // resources.

        @Override
        public CreatableResourcesRoot<ResourceT> refresh() throws Exception {
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