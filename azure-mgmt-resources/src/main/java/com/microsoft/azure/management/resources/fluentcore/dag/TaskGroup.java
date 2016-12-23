/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;
import rx.functions.Func0;
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
     * @param rootTaskEntry the entry holding root task
     */
    public TaskGroup(TaskGroupEntry<ResultT, TaskT> rootTaskEntry) {
        super(rootTaskEntry);
    }

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskItemId the id of the root task in the group
     * @param rootTaskItem the root task
     */
    public TaskGroup(String rootTaskItemId, TaskT rootTaskItem) {
        this(new TaskGroupEntry<ResultT, TaskT>(rootTaskItemId, rootTaskItem));
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

    /**
     * Prepares the tasks stored in the group entries, preparation allows tasks to define additional
     * task dependencies.
     */
    private void prepareTaskItems() {
        boolean isPreparePending;
        HashSet<String> preparedTasksKeys = new HashSet<>();
        // Invokes 'prepare' on a subset of non-prepared tasks in the group. Initially preparation
        // is pending on all task items.
        //
        do {
            isPreparePending = false;
            super.prepare();
            TaskGroupEntry<ResultT, TaskT> entry = super.getNext();
            // Enumerate group entries, an entry holds one task item, in topological sorted order
            //
            // A. For each non-prepared task item -
            //     1. Invoke 'prepare'
            //     2. If new task dependencies added in 'prepare' then set a flag (isPreparePending)
            //        which indicates another pass is required to 'prepare' new task items
            //     3. Add the prepared task item to the set to skip it in the next pass
            //
            // B. For each prepared & non-prepared task item -
            //     1. Refresh the dependent underlying collection with any newly added dependencies
            //
            while (entry != null) {
                if (!preparedTasksKeys.contains(entry.key())) {
                    int dependencyCountBefore = entry.dependencyKeys().size();
                    entry.data().prepare();
                    int dependencyCountAfter = entry.dependencyKeys().size();
                    if ((dependencyCountAfter - dependencyCountBefore) > 0) {
                        isPreparePending = true;
                    }
                    preparedTasksKeys.add(entry.key());
                }
                for (String parentKey : entry.dependentKeys()) {
                    super.mergeChildToParent(parentKey, entry);
                }
                super.reportCompletion(entry);
                entry = super.getNext();
            }
        } while (isPreparePending); // Run another pass if new dependencies were added in this pass
        super.prepare();
    }

    /**
     * Executes the tasks in the group in the topological order of dependencies.
     *
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    private Observable<ResultT> executeInternAsync() {
        TaskGroupEntry<ResultT, TaskT> entry = super.getNext();
        final List<Observable<ResultT>> observables = new ArrayList<>();
        while (entry != null) {
            final TaskGroupEntry<ResultT, TaskT> currentEntry = entry;
            Observable<ResultT> currentTaskObservable = this.executeTaskAsync(currentEntry);
            Func1<ResultT, Observable<ResultT>> onNext = new Func1<ResultT, Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call(ResultT taskResult) {
                    return Observable.just(taskResult);
                }
            };
            Func1<Throwable, Observable<ResultT>> onError = new Func1<Throwable, Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call(Throwable throwable) {
                    reportError(currentEntry, throwable);
                    boolean isDependencyFaulted = throwable instanceof FaultedDependencyException;
                    if (isRootEntry(currentEntry)) {
                        if (isDependencyFaulted) {
                            return Observable.empty();
                        } else {
                            return toErrorObservable(throwable);
                        }
                    } else {
                        if (isDependencyFaulted) {
                            return executeInternAsync();
                        } else {
                            return Observable.concatDelayError(executeInternAsync(), toErrorObservable(throwable));
                        }
                    }
                }
            };
            Func0<Observable<ResultT>> onComplete = new Func0<Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call() {
                    reportCompletion(currentEntry);
                    if (isRootEntry(currentEntry)) {
                        return Observable.empty();
                    } else {
                        return executeInternAsync();
                    }
                }
            };
            observables.add(currentTaskObservable.flatMap(onNext, onError, onComplete));
            entry = super.getNext();
        }
        return Observable.mergeDelayError(observables);
    }

    /**
     * Executes the task item stored in the given entry.
     * <p>
     * if there is a cached result then it will be returned without executing the task, but the root
     *  task always executed even though result is cached.
     *
     * @param entry the entry holding task item
     * @return an observable that emits the result of task.
     */
    private Observable<ResultT> executeTaskAsync(final TaskGroupEntry<ResultT, TaskT> entry) {
        if (entry.hasFaultedDescentDependencyTask()) {
            return toErrorObservable(new FaultedDependencyException());
        }
        if (entry.hasTaskResult() && !isRootEntry(entry)) {
            return Observable.just(entry.taskResult());
        } else {
            return entry.executeTaskAsync();
        }
    }

    /**
     * Checks the given entry is a root entry in this group.
     *
     * @param taskGroupEntry the entry
     * @return true, if the entry is root entry in the group, false otherwise.
     */
    private boolean isRootEntry(TaskGroupEntry<ResultT, TaskT> taskGroupEntry) {
        return isRootNode(taskGroupEntry);
    }

    /**
     * Gets the given throwable as observable.
     *
     * @param throwable the throwable to wrap
     * @return observable with throwable wrapped
     */
    private Observable<ResultT> toErrorObservable(Throwable throwable) {
        return Observable.error(throwable);
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
