/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import org.apache.commons.lang3.tuple.MutablePair;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base implementation of TaskGroup interface.
 *
 * @param <T> the result type of the tasks in the group
 * @param <U> the task item
 */
public abstract class TaskGroupBase<T, U extends TaskItem<T>>
    implements TaskGroup<T, U> {
    private DAGraph<U, DAGNode<U>> dag;

    /**
     * Creates TaskGroupBase.
     *
     * @param rootTaskItemId the id of the root task in this task group
     * @param rootTaskItem the root task
     */
    public TaskGroupBase(String rootTaskItemId, U rootTaskItem) {
        this.dag = new DAGraph<>(new DAGNode<>(rootTaskItemId, rootTaskItem));
    }

    @Override
    public DAGraph<U, DAGNode<U>> dag() {
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
            dag.prepare();
        }
    }

    @Override
    public void execute() throws Exception {
        DAGNode<U> nextNode = dag.getNext();
        while (nextNode != null) {
            nextNode.data().execute();
            this.dag().reportedCompleted(nextNode);
            nextNode = dag.getNext();
        }
    }

    @Override
    public Observable<T> executeAsync() {
        return executeReadyTasksAsync();
    }

    @Override
    public T taskResult(String taskId) {
        return dag.getNodeData(taskId).result();
    }

    /**
     * Executes all runnable tasks, a task is runnable when all the tasks its depends
     * on are finished running.
     */
    private Observable<T> executeReadyTasksAsync() {
        DAGNode<U> nextNode = dag.getNext();
        Observable<T> rootObservable = null;
        final List<Observable<T>> observables = new ArrayList<>();
        while (nextNode != null) {
            final DAGNode<U> thisNode = nextNode;
            if (dag().isRootNode(nextNode)) {
                rootObservable = nextNode.data().executeAsync()
                        .doOnNext(new Action1<T>() {
                            @Override
                            public void call(T t) {
                                dag().reportedCompleted(thisNode);
                            }
                        });
            } else {
                Observable<T> nextNodeObservable = nextNode.data().executeAsync()
                        .flatMap(new Func1<T, Observable<T>>() {
                            @Override
                            public Observable<T> call(T t) {
                                dag().reportedCompleted(thisNode);
                                return executeReadyTasksAsync();
                            }
                        });
                observables.add(nextNodeObservable);
            }
            nextNode = dag.getNext();
        }
        if (rootObservable != null) {
            return rootObservable;
        }
        else {
            return Observable.merge(observables).last();
        }
    }
}
