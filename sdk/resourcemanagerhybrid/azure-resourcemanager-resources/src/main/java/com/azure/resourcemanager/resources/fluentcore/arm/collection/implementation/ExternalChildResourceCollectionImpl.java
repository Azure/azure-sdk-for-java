// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Base class for cached {@link ExternalChildResourcesCachedImpl}
 * and non-cached {@link ExternalChildResourcesNonCachedImpl}
 * externalized child resource collection.
 * (Internal use only)
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
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
     * the TaskGroup of parent resource. This is used to schedule the "post run" works
     * when post run is enabled via {@link this#enablePostRunMode()}
     */
    private final TaskGroup parentTaskGroup;
    /**
     * The child resource instances that this collection contains.
     */
    protected ConcurrentMap<String, FluentModelTImpl> childCollection = new ConcurrentSkipListMap<>();
    /**
     * Indicates how the pending operations on the child resources are performed, true
     * if operations are performed through "post run" task, false if operations are
     * performed via explicit call to {@link this#commitAsync()}.
     */
    private boolean isPostRunMode;

    /**
     * Used to construct error string, this is user friendly name of the child resource (e.g. Subnet, Extension).
     */
    protected final String childResourceName;

    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param parentTaskGroup the TaskGroup the parent Azure resource belongs to
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourceCollectionImpl(ParentImplT parent,
                                                  TaskGroup parentTaskGroup, String childResourceName) {
        this.parent = parent;
        this.parentTaskGroup = parentTaskGroup;
        this.childResourceName = childResourceName;
        this.isPostRunMode = true;
    }

    /**
     * Indicates that the pending operations on the resources are performed as "post run" tasks.
     */
    public void enablePostRunMode() {
        this.isPostRunMode = true;
    }

    /**
     * Indicates that the pending operations on the resources are performed via explicit call to
     * {@link this#commitAsync()}.
     */
    public void enableCommitMode() {
        this.isPostRunMode = false;
    }

    /**
     * Clear the child collection.
     */
    public void clear() {
        for (FluentModelTImpl child : childCollection.values()) {
            child.clear();
        }
        this.childCollection.clear();
    }

    /**
     * Mark the given child resource as the post run dependent of the parent of this collection.
     *
     * @param childResource the child resource
     */
    protected FluentModelTImpl prepareForFutureCommitOrPostRun(FluentModelTImpl childResource) {
        if (this.isPostRunMode) {
            if (!childResource.taskGroup().dependsOn(this.parentTaskGroup)) {
                this.parentTaskGroup.addPostRunDependentTaskGroup(childResource.taskGroup());
            }
        }
        return childResource;
    }

    /**
     * Commits the changes in the external child resource childCollection.
     * <p/>
     * This method returns a Flux stream, its Flux's onNext will be called for each successfully
     * committed resource followed by one call to 'onCompleted' or one call to 'onError' with a
     * {@link RuntimeException} containing the list of exceptions where each exception describes the reason
     * for failure of a resource commit.
     *
     * @return the Flux stream
     */
    public Flux<FluentModelTImpl> commitAsync() {
        if (this.isPostRunMode) {
            return Flux.error(
                new IllegalStateException("commitAsync() cannot be invoked when 'post run' mode is enabled"));
        }

        final ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT>
            self = this;
        List<FluentModelTImpl> items = new ArrayList<>();
        for (FluentModelTImpl item : this.childCollection.values()) {
            items.add(item);
        }

        final List<Throwable> exceptionsList = Collections.synchronizedList(new ArrayList<>());
        final List<FluentModelTImpl> successfullyRemoved = new ArrayList<>();

        ReplayProcessor<FluentModelTImpl> aggregatedErrorStream = ReplayProcessor.create();
        Flux<FluentModelTImpl> deleteStream = Flux.fromIterable(items)
                .filter(childResource ->
                    childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeRemoved)
                .flatMap(childResource -> childResource.deleteResourceAsync()
                        .map(response -> childResource)
                        .doOnSuccess(fluentModelT -> {
                            childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None);
                            self.childCollection.remove(childResource.name());
                            successfullyRemoved.add(childResource);
                        })
                        .onErrorResume(throwable -> {
                            exceptionsList.add(throwable);
                            return Mono.empty();
                        }));

        Flux<FluentModelTImpl> createStream = Flux.fromIterable(items)
                .filter(childResource ->
                    childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated)
                .flatMap(childResource -> childResource.createResourceAsync()
                        .map(fluentModelT -> childResource)
                        .doOnNext(fluentModelT ->
                            childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None))
                        .onErrorResume(throwable -> {
                            self.childCollection.remove(childResource.name());
                            exceptionsList.add(throwable);
                            return Mono.empty();
                        }));

        Flux<FluentModelTImpl> updateStream = Flux.fromIterable(items)
                .filter(childResource ->
                    childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeUpdated)
                .flatMap(childResource -> childResource.updateResourceAsync()
                        .map(e -> childResource)
                        .doOnNext(resource ->
                            resource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.None))
                        .onErrorResume(throwable -> {
                            exceptionsList.add(throwable);
                            return Mono.empty();
                        }));

        Flux<FluentModelTImpl> operationsStream = Flux.merge(deleteStream, createStream, updateStream)
                .doOnTerminate(() -> {
                    if (clearAfterCommit()) {
                        self.childCollection.clear();
                    }
                    if (successfullyRemoved.size() > 0) {
                        for (FluentModelTImpl removed : successfullyRemoved) {
                            aggregatedErrorStream.sink().next(removed);
                        }
                    }
                    if (!exceptionsList.isEmpty()) {
                        aggregatedErrorStream.sink().error(Exceptions.multiple(exceptionsList));
                    } else {
                        aggregatedErrorStream.sink().complete();
                    }
                });
        return Flux.concat(operationsStream, aggregatedErrorStream);
    }

    /**
     * Commits the changes in the external child resource childCollection.
     * <p/>
     * This method returns a observable stream, either its observer's onError will be called with
     * {@link RuntimeException} if some resources failed to commit or onNext will be called if all resources
     * committed successfully.
     *
     * @return the Mono stream
     */
    public Mono<List<FluentModelTImpl>> commitAndGetAllAsync() {
        return commitAsync().collect(() -> new ArrayList<FluentModelTImpl>(),
            (state, item) -> state.add(item));
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
    protected ParentImplT getParent() {
        return parent;
    }

    /**
     * @return true if the child resource collection needs to be cleared after the commit.
     */
    protected abstract boolean clearAfterCommit();
}
