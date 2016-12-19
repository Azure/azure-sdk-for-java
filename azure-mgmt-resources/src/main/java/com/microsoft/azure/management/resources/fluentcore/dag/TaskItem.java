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
     * method that gets called before start executing all the tasks in the task group {@link TaskGroup}.
     */
    void prepare();

    /**
     * Executes the task asynchronously.
     * <p>
     * once executed the result will be available through result getter
     *
     * @return the handle of the REST call
     */
    Observable<T> executeAsync();
}
