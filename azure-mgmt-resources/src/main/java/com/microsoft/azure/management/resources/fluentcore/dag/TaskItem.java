/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;

/**
 * Type representing a task in a task group {@link TaskGroup}.
 *
 * @param <T> the task result type
 */
public interface TaskItem<T> {
    /**
     * @return the result of the task execution
     */
    T result();

    /**
     * method that gets called before start executing all the tasks in the task group
     * {@link TaskGroup} in which this task belongs to.
     */
    void prepare();

    /**
     * @return true if the observable returned by executeAsync is a hot observable,
     * false if its a cold observable.
     */
    boolean isHot();

    /**
     * Executes the task asynchronously.
     *
     * @return the handle of the asynchronous operation
     */
    Observable<T> executeAsync();
}
