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
 * The base implementation of TaskGroup interface.
 *
 * @param <T> the result type of the tasks in the group
 * @param <U> the task item
 */
public abstract class TaskGroupBase<T, U extends TaskItem<T>>
    implements TaskGroup<T, U> {
    /**
     * Stores the tasks in this group and their dependency information.
     * <p>
     * This 'DAGraph' holds collection of {@link TaskItemHolder} where each 'TaskItemHolder' is identified
     * using a key (e.g. uuid) and stores following information -
     *  1. The task to execute, a task implements {@link TaskItem}.
     *  2. A list of keys {@link TaskItemHolder#dependencyKeys()} of other TaskItemHolder that holds the task that #1 task depends on
     *  3. A list of keys {@link TaskItemHolder#dependentKeys()} of other TaskItemHolder holding the task depends on the #1 task
     *     The dependent keys gets populated only after invoking {@link DAGraph#prepare()}
     */
    private DAGraph<U, TaskItemHolder<T, U>> dag;

    /**
     * Creates TaskGroupBase.
     *
     * @param rootTaskItemId the id of the root task in this task group
     * @param rootTaskItem the root task
     */
    public TaskGroupBase(String rootTaskItemId, U rootTaskItem) {
        this.dag = new DAGraph<U, TaskItemHolder<T, U>>(new TaskItemHolder<T, U>(rootTaskItemId, rootTaskItem));
    }

    @Override
    public DAGraph<U, TaskItemHolder<T, U>> dag() {
        return dag;
    }

    @Override
    public boolean isPreparer() {
        return dag.isPreparer();
    }

    @Override
    public void merge(TaskGroup<T, U> parentTaskGroup) {
        dag.merge(parentTaskGroup.dag());
    }

    @Override
    public void prepare() {
        if (isPreparer()) {
            boolean isPreparePending;
            HashSet<String> preparedTasksKeys = new HashSet<>();
            // In each iteration of below loop, prepare() will be invoked on a subset of nodes
            // in the graph. The subset that contains the nodes for which preparation is pending.
            //
            // Initially preparation is pending on all nodes, at the end of each iteration,
            // 'preparedTasksKeys' will contains keys of the nodes which are prepared.
            //
            do {
                isPreparePending = false;
                dag.prepare();
                TaskItemHolder<T, U> nextNode = dag.getNext();
                // Below loop enumerate through each node in the graph in the order dependency
                // is defined (starts with nodes of 0 dependencies and works backward).
                //
                // For each non-prepared node -
                // 1. TaskItem.prepare() will be invoked, which is the opportunity to add
                // additional dependencies for task in the node
                // 2. if any dependencies added then set a flag which indicates another pass is
                //    required to prepare newly added nodes
                // 3. Mark the node as prepared which ensures prepare() is not getting called them
                //    in the next pass
                // For each node (prepared/non-prepared)
                // 1. Refresh the dependent underlying graph with any newly added dependencies
                //
                while (nextNode != null) {
                    if (!preparedTasksKeys.contains(nextNode.key())) {
                        int dependencyCountBefore = nextNode.dependencyKeys().size();
                        nextNode.data().prepare();
                        int dependencyCountAfter = nextNode.dependencyKeys().size();
                        if ((dependencyCountAfter - dependencyCountBefore) > 0) {
                            isPreparePending = true;
                        }
                        preparedTasksKeys.add(nextNode.key());
                    }
                    for (String parentKey : nextNode.dependentKeys()) {
                        dag.mergeChildToParent(parentKey, nextNode);
                    }

                    dag.reportedCompleted(nextNode);
                    nextNode = dag.getNext();
                }
            } while (isPreparePending); // Exit only if no new dependencies were added in this iteration
            dag.prepare();
        }
    }

    @Override
    public Observable<T> executeAsync() {
        TaskItemHolder<T, U> nextNode = dag.getNext();
        final List<Observable<T>> observables = new ArrayList<>();
        while (nextNode != null) {
            final TaskItemHolder<T, U> thisNode = nextNode;
            T cachedResult = nextNode.data().result();
            if (cachedResult != null && !this.dag().isRootNode(nextNode)) {
                observables.add(Observable.just(cachedResult)
                        .flatMap(new Func1<T, Observable<T>>() {
                            @Override
                            public Observable<T> call(T t) {
                                dag().reportedCompleted(thisNode);
                                return Observable.just(t).concatWith(executeAsync());
                            }
                        })
                );
            } else {
                observables.add(nextNode.data().executeAsync()
                        .flatMap(new Func1<T, Observable<T>>() {
                            @Override
                            public Observable<T> call(T t) {
                                dag().reportedCompleted(thisNode);
                                if (dag().isRootNode(thisNode)) {
                                    return Observable.just(t);
                                } else {
                                    return Observable.just(t).concatWith(executeAsync());
                                }
                            }
                        }));
            }
            nextNode = dag.getNext();
        }
        return Observable.merge(observables);
    }

    @Override
    public T taskResult(String taskId) {
        return dag.getNodeData(taskId).result();
    }
}
