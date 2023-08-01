// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Simplified functional interface equivalent to abstract class {@link IndexableTaskItem}.
 */
public interface FunctionalTaskItem
        extends Function<FunctionalTaskItem.Context, Mono<Indexable>> {
    /**
     * Type representing context of an {@link FunctionalTaskItem}.
     */
    final class Context implements HasInnerModel<TaskGroup.InvocationContext>, Indexable {
        private final IndexableTaskItem wrapperTaskItem;
        private TaskGroup.InvocationContext innerContext;

        /**
         * Creates Context.
         *
         * @param taskItem the IndexableTaskItem that wraps this task item.
         */
        Context(IndexableTaskItem taskItem) {
            this.wrapperTaskItem = taskItem;
        }

        /**
         * Set the inner context.
         *
         * @param innerContext the inner context
         */
        void setInnerContext(TaskGroup.InvocationContext innerContext) {
            this.innerContext = innerContext;
        }

        /**
         * Get result of one of the task that belongs to this task's task group.
         *
         * @param key the task key
         * @param <T> the actual type of the task result
         * @return the task result, null will be returned if task has not produced a result yet
         */
        @SuppressWarnings("unchecked")
        public <T extends Indexable> T taskResult(String key) {
            Indexable result = this.wrapperTaskItem.taskGroup().taskResult(key);
            if (result == null) {
                return null;
            } else {
                T castedResult = (T) result;
                return castedResult;
            }
        }

        /**
         * @return a {@link Mono} upon subscription emits {@link VoidIndexable} with key same as
         * the key of this TaskItem.
         */
        public Mono<Indexable> voidMono() {
            Indexable voidIndexable = new VoidIndexable(this.wrapperTaskItem.key());
            return Mono.just(voidIndexable);
        }

        @Override
        public TaskGroup.InvocationContext innerModel() {
            return this.innerContext;
        }

        @Override
        public String key() {
            return this.wrapperTaskItem.key();
        }
    }
}
