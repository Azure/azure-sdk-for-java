package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Base class for creatable resource collection, i.e. those where the member of the collection is of Resource
 * type {@link com.microsoft.azure.Resource } and are creatable.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class CreatableResourcesImpl<T extends Indexable, ImplT extends T, InnerT>
        extends CreatableWrappersImpl<T, ImplT, InnerT>
        implements
            SupportsBatchCreation<T> {

    protected CreatableResourcesImpl() {
    }

    @Override
    @SafeVarargs
    public final CreatedResources<T> create(Creatable<T> ... creatables) {
        return createAsyncNonStream(creatables)
                .toBlocking()
                .single();
    }

    @Override
    public final CreatedResources<T> create(List<Creatable<T>> creatables) {
        return createAsyncNonStream(creatables)
                .toBlocking()
                .single();
    }

    @Override
    @SafeVarargs
    public final Observable<Indexable> createAsync(Creatable<T> ... creatables) {
        CreatableUpdatableResourcesRootImpl<T> rootResource = new CreatableUpdatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);
        return rootResource.createAsync();
    }

    @Override
    public final Observable<Indexable> createAsync(List<Creatable<T>> creatables) {
        CreatableUpdatableResourcesRootImpl<T> rootResource = new CreatableUpdatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);
        return rootResource.createAsync();
    }

    @Override
    @SafeVarargs
    public final ServiceCall<CreatedResources<T>> createAsync(final ServiceCallback<CreatedResources<T>> callback, Creatable<T>... creatables) {
        return ServiceCall.create(createAsyncNonStream(creatables).map(new Func1<CreatedResources<T>, ServiceResponse<CreatedResources<T>>>() {
            @Override
            public ServiceResponse<CreatedResources<T>> call(CreatedResources<T> ts) {
                // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map can be removed
                return new ServiceResponse<>(ts, null);
            }
        }), callback);
    }

    @Override
    public final ServiceCall<CreatedResources<T>> createAsync(final ServiceCallback<CreatedResources<T>> callback, List<Creatable<T>> creatables) {
        return ServiceCall.create(createAsyncNonStream(creatables).map(new Func1<CreatedResources<T>, ServiceResponse<CreatedResources<T>>>() {
            @Override
            public ServiceResponse<CreatedResources<T>> call(CreatedResources<T> ts) {
                // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map can be removed
                return new ServiceResponse<>(ts, null);
            }
        }), callback);
    }


    private Observable<CreatedResources<T>> createAsyncNonStream(List<Creatable<T>> creatables) {
        return Utils.<CreatableUpdatableResourcesRoot<T>>rootResource(this.createAsync(creatables))
                .map(new Func1<CreatableUpdatableResourcesRoot<T>, CreatedResources<T>>() {
                    @Override
                    public CreatedResources<T> call(CreatableUpdatableResourcesRoot<T> tCreatableUpdatableResourcesRoot) {
                        return new CreatedResourcesImpl<>(tCreatableUpdatableResourcesRoot);
                    }
                });
    }

    private Observable<CreatedResources<T>> createAsyncNonStream(Creatable<T>... creatables) {
        return Utils.<CreatableUpdatableResourcesRoot<T>>rootResource(this.createAsync(creatables))
                .map(new Func1<CreatableUpdatableResourcesRoot<T>, CreatedResources<T>>() {
                    @Override
                    public CreatedResources<T> call(CreatableUpdatableResourcesRoot<T> tCreatableUpdatableResourcesRoot) {
                        return new CreatedResourcesImpl<>(tCreatableUpdatableResourcesRoot);
                    }
                });
    }

    /**
     * Implements {@link CreatedResources}.
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatedResourcesImpl<ResourceT extends Indexable> implements CreatedResources<ResourceT> {
        private CreatableUpdatableResourcesRoot<ResourceT> creatableUpdatableResourcesRoot;
        private final List<ResourceT> list;

        CreatedResourcesImpl(CreatableUpdatableResourcesRoot<ResourceT> creatableUpdatableResourcesRoot) {
            this.creatableUpdatableResourcesRoot = creatableUpdatableResourcesRoot;
            this.list = this.creatableUpdatableResourcesRoot.createdTopLevelResources();
        }

        @Override
        public Indexable createdRelatedResource(String key) {
            return this.creatableUpdatableResourcesRoot.createdRelatedResource(key);
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
     * added via {@link SupportsBatchCreation#create} or {@link CreatableResourcesImpl#createAsync}.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    interface CreatableUpdatableResourcesRoot<ResourceT extends Indexable> extends Indexable {
        List<ResourceT> createdTopLevelResources();
         Indexable createdRelatedResource(String key);
    }

    /**
     * Implementation of {@link CreatableUpdatableResourcesRoot}.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatableUpdatableResourcesRootImpl<ResourceT extends Indexable>
            extends CreatableUpdatableImpl<CreatableUpdatableResourcesRoot<ResourceT>, Object, CreatableUpdatableResourcesRootImpl<ResourceT>>
            implements CreatableUpdatableResourcesRoot<ResourceT> {
        /**
         * Collection of keys of top level resources in this batch.
         */
        private List<String> keys;

        CreatableUpdatableResourcesRootImpl() {
            super("CreatableUpdatableResourcesRoot", null);
            this.keys = new ArrayList<>();
        }

        @Override
        public List<ResourceT> createdTopLevelResources() {
            List<ResourceT> resources = new ArrayList<>();
            for (String resourceKey : keys) {
                resources.add((ResourceT) creatorUpdatorTaskGroup().createdResource(resourceKey));
            }
            return Collections.unmodifiableList(resources);
        }

        @Override
        public Indexable createdRelatedResource(String key) {
            return creatorUpdatorTaskGroup().createdResource(key);
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
        public Observable<CreatableUpdatableResourcesRoot<ResourceT>> createResourceAsync() {
            return Observable.just((CreatableUpdatableResourcesRoot<ResourceT>) this);
        }

        @Override
        public Observable<CreatableUpdatableResourcesRoot<ResourceT>> updateResourceAsync() {
            return createResourceAsync();
        }

        @Override
        public boolean isInCreateMode() {
            return true;
        }

        // Below overrides returns null as this is not a real resource in Azure
        // but a dummy resource representing parent of a batch of creatable Azure
        // resources.

        @Override
        public CreatableUpdatableResourcesRoot<ResourceT> refresh() {
            return null;
        }
    }
}