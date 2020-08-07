// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Type representing a unit of work, upon invocation produces result of {@link Indexable} type.
 * <p>
 * The {@link TaskGroup} holds a group of these TaskItems those depends on each other.
 */
public interface TaskItem {
    /**
     * @return the result of the task invocation
     */
    Indexable result();

    /**
     * The method that gets called before invoking all the tasks in the {@link TaskGroup}
     * this task belongs to.
     */
    void beforeGroupInvoke();

    /**
     * @return true if the publisher returned by invokeAsync(cxt) is a hot observable,
     * false if its a cold publisher.
     */
    boolean isHot();

    /**
     * The method that gets called to perform the unit of work asynchronously.
     *
     * @param context the context shared across the the all task items in the group
     *                this task item belongs to.
     * @return a {@link Mono} upon subscription does the unit of work and produces
     * result of type {@link Indexable}
     */
    Mono<Indexable> invokeAsync(TaskGroup.InvocationContext context);

    /**
     * The method that gets called after invocation of "post run" task items depends on
     * this TaskItem.
     * <p>
     * This method will be invoked only if this TaskItem had "post run" dependents.
     *
     * @param isGroupFaulted true if one or more tasks in the group this TaskItem belongs
     *                       to are in faulted state.
     * @return a {@link Flux} representing any asynchronous work initiated
     */
    Mono<Void> invokeAfterPostRunAsync(boolean isGroupFaulted);
}
