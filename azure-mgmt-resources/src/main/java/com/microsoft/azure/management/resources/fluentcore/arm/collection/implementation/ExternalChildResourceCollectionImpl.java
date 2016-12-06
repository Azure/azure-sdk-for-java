/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
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
 * Base class for cached {@link ExternalChildResourcesCachedImpl} and non-cached {@link ExternalChildResourcesNonCachedImpl}
 * externalized child resource collection.
 * (Internal use only)
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourceCollectionImpl<
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
     * The child resource instances that this collection contains.
     */
    protected ConcurrentMap<String, FluentModelTImpl> childCollection = new ConcurrentHashMap<>();

    /**
     * Used to construct error string, this is user friendly name of the child resource (e.g. Subnet, Extension).
     */
    protected final String childResourceName;

    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourceCollectionImpl(ParentImplT parent, String childResourceName) {
        this.parent = parent;
        this.childResourceName = childResourceName;
    }

    /**
     * Commits the changes in the external child resource childCollection.
     * <p/>
     * This method returns an observable stream, its observer's onNext will be called for each successfully
     * committed resource followed by one call to 'onCompleted' or one call to 'onError' with a
     * {@link CompositeException } containing the list of exceptions where each exception describes the reason
     * for failure of a resource commit.
     *
     * @return the observable stream
     */
    public Observable<FluentModelTImpl> commitAsync() {
        final ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> self = this;
        List<FluentModelTImpl> items = new ArrayList<>();
        for (FluentModelTImpl item : this.childCollection.values()) {
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
                                        self.childCollection.remove(childResource.name());
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
                                        self.childCollection.remove(childResource.name());
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
                if (clearAfterCommit()) {
                    self.childCollection.clear();
                }
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
     * Commits the changes in the external child resource childCollection.
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
     * Finds a child resource with the given key.
     *
     * @param key the child resource key
     * @return null if no child resource exists with the given name else the child resource
     */
    protected FluentModelTImpl find(String key) {
        for (Map.Entry<String, FluentModelTImpl> entry : this.childCollection.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * @return the parent Azure resource of the external child resource
     */
    protected ParentImplT parent() {
        return parent;
    }

    /**
     * @return true if the child resource collection needs to be cleared after the commit.
     */
    protected abstract boolean clearAfterCommit();
}
