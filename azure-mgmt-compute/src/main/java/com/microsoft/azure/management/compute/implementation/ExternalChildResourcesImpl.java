package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.ExternalChildResource;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Externalized child resource collection abstract implementation.
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the type of the external child resource
 * @param <InnerModelT> the type of the external child resource inner
 * @param <ParentImplT> the type of parent of the external child resources
 */
abstract class ExternalChildResourcesImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT>,
        FluentModelT extends ExternalChildResource,
        InnerModelT,
        ParentImplT> {
    private final ParentImplT parent;
    private final String childResourceName;
    private ConcurrentMap<String, FluentModelTImpl> collection = new ConcurrentHashMap<>();

    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesImpl(ParentImplT parent, String childResourceName) {
        this.parent = parent;
        this.childResourceName = childResourceName;
        this.initializeCollection();
    }

    /**
     * Refresh the collection from the parent.
     */
    public void refresh() {
        initializeCollection();
    }

    /**
     * Commits the changes in the external child resource collection.
     *
     * @return the stream of committed resources
     */
    public Observable<FluentModelTImpl> commitAsync() {
        final ExternalChildResourcesImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT> self = this;
        List<FluentModelTImpl> items = new ArrayList<>();
        for (FluentModelTImpl extension : this.collection.values()) {
            items.add(extension);
        }

        final List<Throwable> exceptionsList = Collections.synchronizedList(new ArrayList<Throwable>());
        final PublishSubject<FluentModelTImpl> exceptionSubject = PublishSubject.create();

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
                                }).onErrorResumeNext(new Func1<Throwable, Observable<FluentModelTImpl>>() {
                                    @Override
                                    public Observable<FluentModelTImpl> call(Throwable throwable) {
                                        childResource.setState(ExternalChildResourceImpl.State.None);
                                        exceptionsList.add(throwable);
                                        return Observable.empty();
                                    }
                                });
                    }
                });

        Observable<FluentModelTImpl> createStream = Observable.from(items)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.state() == ExternalChildResourceImpl.State.ToBeCreated;
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
                                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends FluentModelTImpl>>() {
                                    @Override
                                    public Observable<? extends FluentModelTImpl> call(Throwable throwable) {
                                        self.collection.remove(childResource.name());
                                        exceptionsList.add(throwable);
                                        return Observable.empty();
                                    }
                                });
                    }
                });

        Observable<FluentModelTImpl> updateStream = Observable.from(items)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.state() == ExternalChildResourceImpl.State.ToBeUpdated;
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
                                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends FluentModelTImpl>>() {
                                    @Override
                                    public Observable<? extends FluentModelTImpl> call(Throwable throwable) {
                                        exceptionsList.add(throwable);
                                        return Observable.empty();
                                    }
                                });
                    }
                });

        Observable<FluentModelTImpl> operationsStream = Observable.merge(deleteStream, createStream, updateStream);
        operationsStream.doOnTerminate(new Action0() {
            @Override
            public void call() {
                if (exceptionsList.isEmpty()) {
                    exceptionSubject.onCompleted();
                } else {
                    exceptionSubject.onError(new CompositeException(exceptionsList));
                }
            }
        });

        Observable<FluentModelTImpl> mergedStream = Observable.mergeDelayError(operationsStream, exceptionSubject);
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
     * @return the list of external child resources
     */
    protected abstract List<FluentModelTImpl> listChildResources();

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
     */
    private void initializeCollection() {
        this.collection.clear();
        for (FluentModelTImpl childResource : this.listChildResources()) {
            this.collection.put(childResource.name(), childResource);
        }
    }
}
