// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for creatable resource collection,
 * i.e. those where the member of the collection is of <code>Resource</code> type and are creatable.
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
    public final CreatedResources<T> create(Creatable<T>... creatables) {
        return createAsyncNonStream(creatables)
                .block();
    }

    @Override
    public final CreatedResources<T> create(List<Creatable<T>> creatables) {
        return createAsyncNonStream(creatables)
                .block();
    }

    @Override
    @SafeVarargs
    public final Flux<Indexable> createAsync(Creatable<T>... creatables) {
        CreatableUpdatableResourcesRootImpl<T> rootResource = new CreatableUpdatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);
        return rootResource.createAsync();
    }

    @Override
    public final Flux<Indexable> createAsync(List<Creatable<T>> creatables) {
        CreatableUpdatableResourcesRootImpl<T> rootResource = new CreatableUpdatableResourcesRootImpl<>();
        rootResource.addCreatableDependencies(creatables);
        return rootResource.createAsync();
    }

    private Mono<CreatedResources<T>> createAsyncNonStream(List<Creatable<T>> creatables) {
        return Utils.<CreatableUpdatableResourcesRoot<T>>rootResource(this.createAsync(creatables).last())
                .map(tCreatableUpdatableResourcesRoot -> new CreatedResourcesImpl<>(tCreatableUpdatableResourcesRoot));
    }

    @SuppressWarnings("unchecked")
    private Mono<CreatedResources<T>> createAsyncNonStream(Creatable<T>... creatables) {
        return Utils.<CreatableUpdatableResourcesRoot<T>>rootResource(this.createAsync(creatables).last())
                .map(tCreatableUpdatableResourcesRoot -> new CreatedResourcesImpl<>(tCreatableUpdatableResourcesRoot));
    }

    /**
     * Implements CreatedResources.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatedResourcesImpl<ResourceT extends Indexable>
            implements CreatedResources<ResourceT> {
        private final ClientLogger logger = new ClientLogger(this.getClass());
        private CreatableUpdatableResourcesRoot<ResourceT> creatableUpdatableResourcesRoot;
        private Map<String, ResourceT> resources = new HashMap<>();

        CreatedResourcesImpl(CreatableUpdatableResourcesRoot<ResourceT> creatableUpdatableResourcesRoot) {
            this.creatableUpdatableResourcesRoot = creatableUpdatableResourcesRoot;
            for (ResourceT resource : this.creatableUpdatableResourcesRoot.createdTopLevelResources()) {
                resources.put(resource.key(), resource);
            }
        }

        @Override
        public Indexable createdRelatedResource(String key) {
            return this.creatableUpdatableResourcesRoot.createdRelatedResource(key);
        }

        @Override
        public void clear() {
            throw logger.logExceptionAsError(new UnsupportedOperationException());
        }

        @Override
        public Set<String> keySet() {
            return resources.keySet();
        }

        @Override
        public Collection<ResourceT> values() {
            return resources.values();
        }

        @Override
        public Set<Entry<String, ResourceT>> entrySet() {
            return resources.entrySet();
        }

        @Override
        public int size() {
            return resources.size();
        }

        @Override
        public boolean isEmpty() {
            return resources.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return resources.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return resources.containsValue(value);
        }

        @Override
        public ResourceT get(Object key) {
            return resources.get(key);
        }

        @Override
        public ResourceT put(String key, ResourceT value) {
            throw logger.logExceptionAsError(new UnsupportedOperationException());
        }

        @Override
        public ResourceT remove(Object key) {
            throw logger.logExceptionAsError(new UnsupportedOperationException());
        }

        @Override
        public void putAll(Map<? extends String, ? extends ResourceT> m) {
            throw logger.logExceptionAsError(new UnsupportedOperationException());
        }
    }

    /**
     * The local root resource that is used as dummy parent resource for the batch creatable resources
     * added via <code>SupportsBatchCreation.create()</code> or <code>CreatableResourcesImpl#createAsync</code>.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    interface CreatableUpdatableResourcesRoot<ResourceT extends Indexable> extends Indexable {
        List<ResourceT> createdTopLevelResources();

        Indexable createdRelatedResource(String key);
    }

    /**
     * Implementation of CreatableUpdatableResourcesRoot.
     *
     * @param <ResourceT> the type of the resources in the batch.
     */
    private class CreatableUpdatableResourcesRootImpl<ResourceT extends Indexable>
            extends CreatableUpdatableImpl<CreatableUpdatableResourcesRoot<ResourceT>,
                                           Object,
                                           CreatableUpdatableResourcesRootImpl<ResourceT>>
            implements CreatableUpdatableResourcesRoot<ResourceT> {
        /**
         * Collection of keys of top level resources in this batch.
         */
        private List<String> keys;

        CreatableUpdatableResourcesRootImpl() {
            super("CreatableUpdatableResourcesRoot", null);
            this.keys = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<ResourceT> createdTopLevelResources() {
            List<ResourceT> resources = new ArrayList<>();
            for (String resourceKey : keys) {
                resources.add(this.<ResourceT>taskResult(resourceKey));
            }
            return Collections.unmodifiableList(resources);
        }

        @Override
        public Indexable createdRelatedResource(String key) {
            return this.<Indexable>taskResult(key);
        }

        @SuppressWarnings("unchecked")
        void addCreatableDependencies(Creatable<T>... creatables) {
            for (Creatable<T> item : creatables) {
                this.keys.add(this.addDependency(item));
            }
        }

        void addCreatableDependencies(List<Creatable<T>> creatables) {
            for (Creatable<T> item : creatables) {
                this.keys.add(this.addDependency(item));
            }
        }

        @Override
        public Mono<CreatableUpdatableResourcesRoot<ResourceT>> createResourceAsync() {
            return Mono.just((CreatableUpdatableResourcesRoot<ResourceT>) this);
        }

        @Override
        public Mono<CreatableUpdatableResourcesRoot<ResourceT>> updateResourceAsync() {
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
        protected Mono<Object> getInnerAsync() {
            return null;
        }
    }
}
