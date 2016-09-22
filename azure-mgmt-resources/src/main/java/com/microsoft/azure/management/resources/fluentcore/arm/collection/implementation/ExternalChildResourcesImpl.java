package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
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
 * (Internal use only)
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourcesImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT, ParentT>,
        FluentModelT extends ExternalChildResource<FluentModelT, ParentT>,
        InnerModelT,
        ParentImplT extends ParentT,
        ParentT> {
    /**
     * The parent resource of this collection of child resources.
     */
    private final ParentImplT parent;
    /**
     * Used to construct error string, this is user friendly name of the child resource (e.g. Subnet, Extension).
     */
    private final String childResourceName;
    /**
     * The child resource instances that this collection contains.
     */
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
    }

    /**
     * Refresh the collection.
     */
    public void refresh() {
        initializeCollection();
    }

    /**
     * Commits the changes in the external child resource collection.
     * <p/>
     * This method returns an observable stream, its observer's onNext will be called for each successfully
     * committed resource followed by 'one call to onCompleted' or one call to onError with a
     * {@link CompositeException } containing the list of exceptions where each exception describes the reason
     * for failure of a resource commit.
     *
     * @return the observable stream
     */
    public Observable<FluentModelTImpl> commitAsync() {
        final ExternalChildResourcesImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> self = this;
        List<FluentModelTImpl> items = new ArrayList<>();
        for (FluentModelTImpl item : this.collection.values()) {
            items.add(item);
        }

        final List<Throwable> exceptionsList = Collections.synchronizedList(new ArrayList<Throwable>());
        Observable<FluentModelTImpl> deleteStream = Observable.from(items)
                .filter(new Func1<FluentModelTImpl, Boolean>() {
                    @Override
                    public Boolean call(FluentModelTImpl childResource) {
                        return childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeRemoved;
                    }
                }).flatMap(new Func1<FluentModelTImpl, Observable<FluentModelTImpl>>() {
                    @Override
                    public Observable<FluentModelTImpl> call(final FluentModelTImpl childResource) {
                        return childResource.deleteAsync()
                                .map(new Func1<Void, FluentModelTImpl>() {
                                    @Override
                                    public FluentModelTImpl call(Void response) {
                                        return childResource;
                                    }
                                }).doOnNext(new Action1<FluentModelTImpl>() {
                                    @Override
                                    public void call(FluentModelTImpl childResource) {
                                        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None);
                                        self.collection.remove(childResource.name());
                                    }
                                })
                                .onErrorResumeNext(new Func1<Throwable, Observable<FluentModelTImpl>>() {
                                    @Override
                                    public Observable<FluentModelTImpl> call(Throwable throwable) {
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
                        return childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated;
                    }
                }).flatMap(new Func1<FluentModelTImpl, Observable<FluentModelTImpl>>() {
                    @Override
                    public Observable<FluentModelTImpl> call(final FluentModelTImpl childResource) {
                        return childResource.createAsync()
                                .map(new Func1<FluentModelT, FluentModelTImpl>() {
                                    @Override
                                    public FluentModelTImpl call(FluentModelT fluentModelT) {
                                        return childResource;
                                    }
                                })
                                .doOnNext(new Action1<FluentModelTImpl>() {
                                    @Override
                                    public void call(FluentModelTImpl fluentModelT) {
                                        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None);
                                    }
                                })
                                .onErrorResumeNext(new Func1<Throwable, Observable<? extends FluentModelTImpl>>() {
                                    @Override
                                    public Observable<FluentModelTImpl> call(Throwable throwable) {
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
                        return childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeUpdated;
                    }
                }).flatMap(new Func1<FluentModelTImpl, Observable<FluentModelTImpl>>() {
                    @Override
                    public Observable<FluentModelTImpl> call(final FluentModelTImpl childResource) {
                        return childResource.updateAsync()
                                .map(new Func1<FluentModelT, FluentModelTImpl>() {
                                    @Override
                                    public FluentModelTImpl call(FluentModelT e) {
                                        return childResource;
                                    }
                                })
                                .doOnNext(new Action1<FluentModelTImpl>() {
                                    @Override
                                    public void call(FluentModelTImpl childResource) {
                                        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None);
                                    }
                                })
                                .onErrorResumeNext(new Func1<Throwable, Observable<? extends FluentModelTImpl>>() {
                                    @Override
                                    public Observable<FluentModelTImpl> call(Throwable throwable) {
                                        exceptionsList.add(throwable);
                                        return Observable.empty();
                                    }
                                });
                    }
                });

        final PublishSubject<FluentModelTImpl> aggregatedErrorStream = PublishSubject.create();
        Observable<FluentModelTImpl> operationsStream = Observable.merge(deleteStream,
                createStream,
                updateStream).doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                    if (exceptionsList.isEmpty()) {
                        aggregatedErrorStream.onCompleted();
                    } else {
                        aggregatedErrorStream.onError(new CompositeException(exceptionsList));
                    }
                }
            });

        Observable<FluentModelTImpl> stream = Observable.concat(operationsStream, aggregatedErrorStream);
        return stream;
    }

    /**
     * Commits the changes in the external child resource collection.
     * <p/>
     * This method returns a observable stream, either its observer's onError will be called with
     * {@link CompositeException} if some resources failed to commit or onNext will be called if all resources
     * committed successfully.
     *
     * @return the observable stream
     */
    public Observable<List<FluentModelTImpl>> commitAndGetAllAsync() {
        return commitAsync().collect(
                new Func0<List<FluentModelTImpl>>() {
                        public List<FluentModelTImpl> call() {
                            return new ArrayList<>();
                        }
                },
                new Action2<List<FluentModelTImpl>, FluentModelTImpl>() {
                    public void call(List<FluentModelTImpl> state, FluentModelTImpl item) {
                        state.add(item);
                    }
                });
    }

    /**
     * @return the parent Azure resource of the external child resource
     */
    protected ParentImplT parent() {
        return parent;
    }

    /**
     * @return the collection of external child resources.
     */
    protected Map<String, FluentModelTImpl> collection() {
        return this.collection;
    }

    /**
     * Prepare for definition of a new external child resource.
     *
     * @param name the name for the new external child resource
     * @return the child resource
     */
    protected FluentModelTImpl prepareDefine(String name) {
        if (find(name) != null) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' already exists");
        }
        FluentModelTImpl childResource = newChildResource(name);
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return childResource;
    }

    /**
     * Prepare for an external child resource update.
     *
     * @param name the name of the external child resource
     * @return the external child resource to be updated
     */
    protected FluentModelTImpl prepareUpdate(String name) {
        FluentModelTImpl childResource = find(name);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' not found");
        }
        if (childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeRemoved) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' is marked for deletion");
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        return childResource;
    }

    /**
     * Mark an external child resource with given name as to be removed.
     *
     * @param name the name of the external child resource
     */
    protected void prepareRemove(String name) {
        FluentModelTImpl childResource = find(name);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name  '" + name + "' not found");
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
    }

    /**
     * Adds an external child resource to the collection.
     *
     * @param childResource the external child resource
     */
    protected void addChildResource(FluentModelTImpl childResource) {
        this.collection.put(childResource.name(), childResource);
    }

    /**
     * Initializes the external child resource collection.
     */
    protected void initializeCollection() {
        this.collection.clear();
        for (FluentModelTImpl childResource : this.listChildResources()) {
            this.collection.put(childResource.name(), childResource);
        }
    }

    /**
     * Gets the list of external child resources.
     *
     * @return the list of external child resources
     */
    protected abstract List<FluentModelTImpl> listChildResources();

    /**
     * Gets a new external child resource model instance.
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
}
