package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.ExternalChildResource;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Externalized child resource collection abstract implementation.
 *
 * @param <FluentModelTImpl> the type of the external child resource implementation
 * @param <FluentModelT> the type of the external child resource
 * @param <InnerModelT> the type of the external child resource inner model
 * @param <ParentImplT> the type of parent of the external child resource collection
 */
abstract class ExternalChildResourcesImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT>,
        FluentModelT extends ExternalChildResource,
        InnerModelT,
        ParentImplT> {
    private final ParentImplT parent;
    private final String parentResourceName;
    private final String childResourceName;
    private boolean requireRefresh = false;
    private ConcurrentMap<String, FluentModelTImpl> collection = new ConcurrentHashMap<>();

    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param parentResourceName the parent resource name
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesImpl(ParentImplT parent, String parentResourceName, String childResourceName) {
        this.parent = parent;
        this.parentResourceName = parentResourceName;
        this.childResourceName = childResourceName;
        this.initializeCollection(false);
    }

    /**
     * Refresh the collection from the parent.
     */
    public void refresh() {
        initializeCollection(false);
    }

    /**
     * Commits the changes in the external child resource collection.
     *
     * @return the stream of updated extensions
     */
    public Observable<FluentModelTImpl> commitAsync() {
        final ExternalChildResourcesImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT> self = this;
        List<FluentModelTImpl> items = new ArrayList<>();
        for (FluentModelTImpl extension : this.collection.values()) {
            items.add(extension);
        }

        Observable<FluentModelTImpl> deleteStream = Observable.from(items)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.state() == ExternalChildResourceImpl.State.ToBeRemoved;
                    }
                }).flatMap(new Func1<FluentModelTImpl, Observable<FluentModelTImpl>>() {
                    @Override
                    public Observable<FluentModelTImpl> call(final FluentModelTImpl childResource) {
                        return childResource.deleteAsync()
                                .map(new Func1<Void, FluentModelTImpl>() {
                                    @Override
                                    public FluentModelTImpl call(Void response) {
                                        self.collection.remove(childResource.name());
                                        return childResource;
                                    }
                                });
                    }
                });

        Observable<FluentModelTImpl> setStream = Observable.from(items)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.state() == ExternalChildResourceImpl.State.ToBeUpdated
                                || childResource.state() == ExternalChildResourceImpl.State.ToBeCreated;
                    }
                }).flatMap(new Func1<FluentModelTImpl, Observable<FluentModelTImpl>>() {
                    @Override
                    public Observable<FluentModelTImpl> call(final FluentModelTImpl childResource) {
                        return childResource.setAsync()
                                .map(new Func1<FluentModelT, FluentModelTImpl>() {
                                    @Override
                                    public FluentModelTImpl call(FluentModelT e) {
                                        childResource.setState(ExternalChildResourceImpl.State.None);
                                        return childResource;
                                    }
                                });
                    }
                });

        Observable<FluentModelTImpl> mergedStream = deleteStream.mergeWith(setStream)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.state() == ExternalChildResourceImpl.State.None;
                    }
                }).doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        initializeCollection(true);
                    }
                });

        return mergedStream;
    }

    /**
     * @return the parent Azure resource of the external child resource
     */
    protected ParentImplT parent() {
        return parent;
    }

    /**
     * @return the collection
     */
    protected Map<String, FluentModelTImpl> collection() {
        return this.collection;
    }

    /**
     * Prepare for definition of a new child resource.
     *
     * @param name the name for the new child resource
     * @return the child resource
     */
    protected FluentModelTImpl prepareDefine(String name) {
        this.checkRefreshRequired();
        if (find(name) != null) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' already exists");
        }
        FluentModelTImpl childResource = newChildResource(name);
        childResource.setState(VirtualMachineExtensionImpl.State.ToBeCreated);
        return childResource;
    }

    /**
     * Prepare for a child resource update.
     *
     * @param name the name of the child resource
     * @return the child resource
     */
    protected FluentModelTImpl prepareUpdate(String name) {
        this.checkRefreshRequired();
        FluentModelTImpl childResource = find(name);
        if (childResource == null
                || childResource.state() == ExternalChildResourceImpl.State.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' not found");
        }
        if (childResource.state() == VirtualMachineExtensionImpl.State.ToBeRemoved) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' is marked for deletion");
        }
        childResource.setState(VirtualMachineExtensionImpl.State.ToBeUpdated);
        return childResource;
    }

    /**
     * Mark the child resource with given name as to be removed.
     *
     * @param name the name of the child resource
     */
    protected void prepareRemove(String name) {
        this.checkRefreshRequired();
        FluentModelTImpl childResource = find(name);
        if (childResource == null
                || childResource.state() == ExternalChildResourceImpl.State.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' not found");
        }
        childResource.setState(VirtualMachineExtensionImpl.State.ToBeRemoved);
    }

    /**
     * Adds a child resource to the collection.
     *
     * @param childResource the child resource
     */
    protected void addChildResource(FluentModelTImpl childResource) {
        this.collection.put(childResource.name(), childResource);
    }

    /**
     * Gets the list of external child resources.
     *
     * @param requireRefresh true if the child resource collection needs to be refreshed
     * @return the list of external child resources
     */
    protected abstract Observable<FluentModelTImpl> listChildResourcesAsync(boolean requireRefresh);

    /**
     * Creates a new external child resource.
     *
     * @param name the name for the new child resource
     * @return the new child resource
     */
    protected abstract FluentModelTImpl newChildResource(String name);

    /**
     * Finds a child resource with the given name.
     *
     * @param name the child resource name
     * @return null if no child resource exists with the given name else the child resource
     */
    private FluentModelTImpl find(String name) {
        for (Map.Entry<String, FluentModelTImpl> entry : this.collection.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Initializes the child resource collection.
     *
     * @param doRefresh true if inner collection required to be refreshed
     */
    private void initializeCollection(boolean doRefresh) {
        this.collection.clear();
        final ExternalChildResourcesImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT> self = this;
        this.listChildResourcesAsync(doRefresh)
                .toBlocking()
                .subscribe(new Observer<FluentModelTImpl>() {
                    @Override
                    public void onCompleted() {
                        self.requireRefresh = false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        self.requireRefresh = true;
                    }

                    @Override
                    public void onNext(FluentModelTImpl childResource) {
                        self.collection.put(childResource.name(), childResource);
                    }
                });
    }

    /**
     * Checks collection refresh is required if yes throw exception.
     */
    private void checkRefreshRequired() {
        if (this.requireRefresh) {
            throw new RuntimeException("The parent '" + parentResourceName + "' needs to be refreshed before adding '" + childResourceName + "'");
        }
    }
}
