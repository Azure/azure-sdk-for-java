/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.dag.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Action1;

/**
 * Represents a task that produces a result when executed.
 *
 * @param <ResultT> the type of the resource that execution of this task produces
 */
public class ExecuteTask<ResultT> implements TaskItem<ResultT> {
    /**
     * the underlying instance that can execute the task.
     */
    private ExecuteTask.Executor<ResultT> executor;
    /**
     * result of execution.
     */
    private ResultT result;

    /**
     * Creates ExecuteTask.
     *
     * @param executor the executor
     */
    public ExecuteTask(ExecuteTask.Executor<ResultT> executor) {
        this.executor = executor;
    }

    @Override
    public ResultT result() {
        return this.result;
    }

    @Override
    public void prepare() {
        executor.prepare();

    }

    @Override
    public boolean isHot() {
        return executor.isHot();
    }

    @Override
    public Observable<ResultT> executeAsync() {
        return this.executor.executeWorkAsync()
                .subscribeOn(SdkContext.getRxScheduler())
                .doOnNext(new Action1<ResultT>() {
                    @Override
                    public void call(ResultT value) {
                        result = value;
                    }
                });
    }

    /**
     * Represents a type that produces a value of type {@link ResultT} when executed.
     *
     * @param <ResultT> the resource type
     */
    public interface Executor<ResultT> {
        /**
         * prepare for execute.
         */
        void prepare();

        /**
         * @return true if the observable returned by {@link this#executeAsync()} and
         * {@link this#executeAsync()} are hot observables, false if they are cold observables.
         */
        boolean isHot();

        /**
         * Execute the work asynchronously.
         *
         * @return the observable reference
         */
        Observable<ResultT> executeWorkAsync();
    }
}