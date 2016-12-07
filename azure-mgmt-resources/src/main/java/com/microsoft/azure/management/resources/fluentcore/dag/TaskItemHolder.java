package com.microsoft.azure.management.resources.fluentcore.dag;

/**
 * Type that hold one {@link TaskItem} and associated information which includes:
 * 1. references to other {@link TaskItemHolder} holding {@link TaskItem} this {@link TaskItem} depends on
 * 2. references to the other {@link TaskItemHolder} holding {@link TaskItem} depends on this {@link TaskItem}
 *
 * @param <T> the task result type
 * @param <U> the task item type
 */
public class TaskItemHolder<T, U extends TaskItem<T>> extends DAGNode<U, TaskItemHolder<T, U>> {
    /**
     * Creates TaskItemHolder.
     *
     * @param taskId the unique id of the task
     * @param taskItem the task
     */
    public TaskItemHolder(String taskId, U taskItem) {
        super(taskId, taskItem);
    }
}