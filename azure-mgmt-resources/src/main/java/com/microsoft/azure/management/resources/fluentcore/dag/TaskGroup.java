/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Type that represents group of {@link TaskGroupEntry} items, each item holds a {@link TaskItem}
 * and associated dependency information.
 *
 * @param <ResultT> type of the result produced by the tasks in the group
 * @param <TaskT> type of the tasks in the group that can return a value
 */
public class TaskGroup<ResultT, TaskT extends TaskItem<ResultT>>
        extends DAGraph<TaskT, TaskGroupEntry<ResultT, TaskT>> {
    /**
     * Creates TaskGroup.
     *
     * @param rootTaskItemId the id of the root task in the group
     * @param rootTaskItem the root task
     */
    public TaskGroup(String rootTaskItemId, TaskT rootTaskItem) {
        super(new TaskGroupEntry<ResultT, TaskT>(rootTaskItemId, rootTaskItem));
    }

    /**
     * Gets the result produced by a task item in the group.
     * <p>
     * this method can null if the task has not yet been executed
     *
     * @param taskId the task item id
     * @return the task result
     */
    public ResultT taskResult(String taskId) {
        TaskGroupEntry<ResultT, TaskT> taskGroupEntry = super.getNode(taskId);
        if (taskGroupEntry == null) {
            throw new IllegalArgumentException("A task with key '" + taskId + "' not found");
        }
        return taskGroupEntry.taskResult();
    }

    /**
     * Merge this group with the given group containing root task depends on this
     * group.
     *
     * @param parentTaskGroup the parent task group
     */
    public void merge(TaskGroup<ResultT, TaskT> parentTaskGroup) {
        super.merge(parentTaskGroup);
    }

    /**
     * Executes tasks in the group.
     *
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    public Observable<ResultT> executeAsync() {
        if (!isPreparer()) {
          return Observable.error(new IllegalStateException("executeAsync can be called "
                  + "only from root TaskGroup"));
        }
        prepareTaskItems();
        return executeInternAsync();
    }

    private boolean isRootTask(TaskGroupEntry<ResultT, TaskT> taskGroupEntry) {
        return isRootNode(taskGroupEntry);
    }

    private Observable<ResultT> executeInternAsync() {
        TaskGroupEntry<ResultT, TaskT> currentEntry = super.getNext();
        final List<Observable<ResultT>> observables = new ArrayList<>();
        while (currentEntry != null) {
            final TaskGroupEntry<ResultT, TaskT> entry = currentEntry;
            Observable<ResultT> thisTaskObservable;
            if (entry.hasTaskResult() && !isRootTask(entry)) {
                thisTaskObservable = Observable.just(entry.taskResult());
            } else {
                thisTaskObservable = entry.executeTaskAsync();
            }
            observables.add(thisTaskObservable.flatMap(new Func1<ResultT, Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call(ResultT taskResult) {
                    reportedCompleted(entry);
                    if (isRootTask(entry)) {
                        return Observable.just(taskResult);
                    } else {
                        return Observable.just(taskResult).concatWith(executeInternAsync());
                    }
                }
            }));
            currentEntry = super.getNext();
        }
        return Observable.merge(observables);
    }

    private void prepareTaskItems() {
        boolean isPreparePending;
        HashSet<String> preparedTasksKeys = new HashSet<>();
        // In each pass of below loop, prepare() will be invoked on a subset of tasks in the
        // group for which preparation is pending. Initially preparation is pending on all tasks.
        //
        do {
            isPreparePending = false;
            super.prepare();
            TaskGroupEntry<ResultT, TaskT> currentEntry = super.getNext();
            // Enumerate through each task item in the group in the order dependency is defined
            // (starting from tasks with 0 dependencies and works backward).
            //
            // For each non-prepared task item -
            // 1. TaskItem.prepare() will be invoked where additional dependencies for the task can be added
            // 2. if any dependencies added then set a flag which indicates another pass is required to
            //    'prepare' newly added task items
            // 3. Add the prepared task item to the set to skip them in the next pass
            //
            // For each task item (prepared/non-prepared)
            // 1. Refresh the dependent underlying collection with any newly added dependencies
            //
            while (currentEntry != null) {
                if (!preparedTasksKeys.contains(currentEntry.key())) {
                    int dependencyCountBefore = currentEntry.dependencyKeys().size();
                    currentEntry.data().prepare();
                    int dependencyCountAfter = currentEntry.dependencyKeys().size();
                    if ((dependencyCountAfter - dependencyCountBefore) > 0) {
                        isPreparePending = true;
                    }
                    preparedTasksKeys.add(currentEntry.key());
                }
                for (String parentKey : currentEntry.dependentKeys()) {
                    super.mergeChildToParent(parentKey, currentEntry);
                }
                super.reportedCompleted(currentEntry);
                currentEntry = super.getNext();
            }
        } while (isPreparePending); // Exit only if no new dependencies were added in this iteration
        super.prepare();
    }

    /**
     * An interface representing a type that is a part of TaskGroup.
     *
     * @param <T> type of the value returned by tasks in the group
     * @param <U> type of the task that can return a value
     */
    public interface HasTaskGroup<T, U extends TaskItem<T>> {
        /**
         * @return Gets the task group.
         */
        TaskGroup<T, U> taskGroup();
    }
}
