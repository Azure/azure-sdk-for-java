/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Type representing a group of task entries with dependencies between them. Initially a task
 * group will have only one task entry known as root task entry, then more entries can be
 * added by taking dependency on multiple task group.
 *
 * The method {@link TaskGroup#invokeAsync(InvocationContext)} ()} kick-off invocation of tasks
 * in the group, task are invoked in topological sorted order.
 *
 * {@link TaskGroup#addDependencyTaskGroup(TaskGroup)}: A task group "A" can take dependency on
 * another task group "B" through this method e.g. `A.addDependencyTaskGroup(B)` indicates that
 * completion of tasks in the dependency task group "B" is required before the invocation of root
 * task in group "A". A.invokeAsync() will ensure this order.
 *
 * {@link TaskGroup#addDependentTaskGroup(TaskGroup)}: Through this method it is possible to
 * express that a task group "M" has a dependent task group "N" e.g. `N.addDependentTaskGroup(M)`
 * In this case M.invokeAsync() will not invocation tasks in the group "N". on the other hand
 * N.invokeAsync() will group "M" followed by "N".
 *
 * {@link TaskGroup#addPostRunDependentTaskGroup(TaskGroup)}: there are scenarios where a subset
 * of dependent task groups say "H", "I" may required to run after the invocation of a task group
 * "K" when K.invokeAsync() is called. Such special dependents can be added via
 * K.addPostRunDependentTaskGroup(H) and K.addPostRunDependentTaskGroup(I).
 *
 * @param <ResultT> type of the result returned by the tasks in the group
 * @param <TaskT> type of the tasks in the group that can return a value
 */
public class TaskGroup<ResultT, TaskT extends TaskItem<ResultT>>
        extends DAGraph<TaskT, TaskGroupEntry<ResultT, TaskT>> {
    /**
     * The root task in this task group.
     */
    private final TaskGroupEntry<ResultT, TaskT> rootTaskEntry;

    /**
     * Task group termination strategy to be used once any task in the group error-ed.
     */
    private final TaskGroupTerminateOnErrorStrategy taskGroupTerminateOnErrorStrategy;
    /**
     * Flag indicating whether this group is marked as cancelled or not. This flag will be used only
     * when group's terminate on error strategy is set as
     * {@link TaskGroupTerminateOnErrorStrategy#TERMINATE_ON_INPROGRESS_TASKS_COMPLETION}.
     * Effect of setting this flag can be think as broadcasting a cancellation signal to tasks those
     * are yet to invoke.
     */
    private boolean isGroupCancelled;
    /**
     * The shared exception object used to indicate that a task is not invoked since the group
     * is marked as cancelled i.e. {@link this#isGroupCancelled} is set.
     */
    private final TaskCancelledException taskCancelledException = new TaskCancelledException();
    /**
     * The helper to operate on proxy TaskGroup of this TaskGroup for supporting dependents marked
     * for post run.
     */
    protected ProxyTaskGroupWrapper<ResultT> proxyTaskGroupWrapper;

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskEntry the entry holding root task
     * @param taskGroupTerminateOnErrorStrategy termination strategy to be used on error
     */
    private TaskGroup(TaskGroupEntry<ResultT, TaskT> rootTaskEntry,
                      TaskGroupTerminateOnErrorStrategy taskGroupTerminateOnErrorStrategy) {
        super(rootTaskEntry);
        this.rootTaskEntry = rootTaskEntry;
        this.taskGroupTerminateOnErrorStrategy = taskGroupTerminateOnErrorStrategy;
        this.proxyTaskGroupWrapper = new ProxyTaskGroupWrapper<>(this.that(), taskGroupTerminateOnErrorStrategy);
    }

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskItemId the id of the root task in the group
     * @param rootTaskItem the root task
     * @param taskGroupTerminateOnErrorStrategy group termination strategy to be used on error
     */
    public TaskGroup(String rootTaskItemId,
                     TaskT rootTaskItem,
                     TaskGroupTerminateOnErrorStrategy taskGroupTerminateOnErrorStrategy) {
        this(new TaskGroupEntry<ResultT, TaskT>(rootTaskItemId, rootTaskItem), taskGroupTerminateOnErrorStrategy);
    }

    /**
     * Gets the result produced by a task in the group.
     *
     * @param taskId the task item id
     * @return the task result, null will be returned if task has not yet been invoked
     */
    public ResultT taskResult(String taskId) {
        TaskGroupEntry<ResultT, TaskT> taskGroupEntry = super.getNode(taskId);
        if (taskGroupEntry == null) {
            throw new IllegalArgumentException("A task with id '" + taskId + "' is not found");
        }
        return taskGroupEntry.taskResult();
    }

    /**
     * @return the root task entry in the group.
     */
    private TaskGroupEntry<ResultT, TaskT> root() {
        return this.rootTaskEntry;
    }


    /**
     * Mark root of this task task group depends on the given task group's root.
     * This ensure this task group's root get picked for execution only after the completion
     * of all tasks in the given group.
     *
     * @param dependencyTaskGroup the task group that this task group depends on
     */
    public void addDependencyTaskGroup(TaskGroup<ResultT, TaskT> dependencyTaskGroup) {
        if (dependencyTaskGroup.proxyTaskGroupWrapper.isActive()) {
            dependencyTaskGroup.proxyTaskGroupWrapper.addDependentTaskGroup(this.that());
        } else {
            DAGraph<TaskT, TaskGroupEntry<ResultT, TaskT>> dependencyGraph = dependencyTaskGroup;
            super.addDependencyGraph(dependencyGraph);
        }
    }

    /**
     * Mark root of the given task group depends on this task group's root.
     * This ensure given task group's root get picked for be invoked only after the completion
     * of all tasks in this group. Invoking {@link this#invokeAsync(InvocationContext)} ()}
     * will not invoke the tasks in the given dependent task group.
     *
     * @param dependentTaskGroup the task group depends on this task group
     */
    public void addDependentTaskGroup(TaskGroup<ResultT, TaskT> dependentTaskGroup) {
        DAGraph<TaskT, TaskGroupEntry<ResultT, TaskT>> dependentGraph = dependentTaskGroup;
        super.addDependentGraph(dependentGraph);
    }

    /**
     * Mark root of the given task group depends on this task group's root.
     * This ensure given task group's root get picked for invocation only after the completion
     * of all tasks in this group. Invoking {@link this#invokeAsync(InvocationContext)} ()}
     * will run the tasks in the given dependent task group as well.
     *
     * @param dependentTaskGroup the task group depends on this task group
     */
    public void addPostRunDependentTaskGroup(TaskGroup<ResultT, TaskT> dependentTaskGroup) {
        DAGraph<TaskT, TaskGroupEntry<ResultT, TaskT>> dependentGraph = dependentTaskGroup;
        super.addDependentGraph(dependentGraph);
        this.proxyTaskGroupWrapper.addDependencyTaskGroup(dependentTaskGroup.that());
    }

    /**
     * Invokes tasks in the group.
     *
     * @param context group level shared context that need be passed to {@link TaskItem#invokeAsync(InvocationContext)}
     *                method of each task item in the group when it is selected for invocation.
     *
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    public Observable<ResultT> invokeAsync(final InvocationContext context) {
        if (this.proxyTaskGroupWrapper.isActive()) {
            return this.proxyTaskGroupWrapper.invokeAsync(context);
        } else {
            if (!isPreparer()) {
                return Observable.error(new IllegalStateException("invokeAsync(cxt) can be called only from root TaskGroup"));
            }
            return Observable.defer(new Func0<Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call() {
                    isGroupCancelled = false;
                    // Prepare tasks and queue the ready tasks (terminal tasks with no dependencies)
                    //
                    prepareTasks();
                    // Runs the ready tasks concurrently
                    //
                    return invokeReadyTasksAsync(context);
                }
            });
        }
    }

    /**
     * Run the prepare stage of the tasks in the group, preparation allows tasks to define additional
     * task dependencies.
     */
    private void prepareTasks() {
        boolean isPreparePending;
        HashSet<String> preparedTasksKeys = new HashSet<>();
        // Invokes 'prepare' on a subset of non-prepared tasks in the group. Initially preparation
        // is pending on all task items.
        List<TaskGroupEntry<ResultT, TaskT>> entries = this.entriesSnapshot();
        do {
            isPreparePending = false;
            for (TaskGroupEntry<ResultT, TaskT> entry : entries) {
                if (!preparedTasksKeys.contains(entry.key())) {
                    entry.data().prepare();
                    preparedTasksKeys.add(entry.key());
                }
            }
            int prevSize = entries.size();
            entries = this.entriesSnapshot();
            if (entries.size() > prevSize) {
                // If new task dependencies/dependents added in 'prepare' then set the
                // flag which indicates another pass is required to 'prepare' new task
                // items
                isPreparePending = true;
            }
        } while (isPreparePending);  // Run another pass if new dependencies were added in this pass
        super.prepareForEnumeration();
    }

    /**
     * @return list with nodes in the graph.
     */
    private List<TaskGroupEntry<ResultT, TaskT>> entriesSnapshot() {
        List<TaskGroupEntry<ResultT, TaskT>> entries = new ArrayList<>();
        super.prepareForEnumeration();
        for (TaskGroupEntry<ResultT, TaskT> current = super.getNext(); current != null; current = super.getNext()) {
            entries.add(current);
            super.reportCompletion(current);
        }
        return entries;
    }

    /**
     * Invokes the ready tasks.
     *
     * @param context group level shared context that need be passed to
     *                {@link TaskGroupEntry#invokeTaskAsync(boolean, InvocationContext)}
     *                method of each entry in the group when it is selected for execution
     *
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    private Observable<ResultT> invokeReadyTasksAsync(final InvocationContext context) {
        TaskGroupEntry<ResultT, TaskT> entry = super.getNext();
        final List<Observable<ResultT>> observables = new ArrayList<>();
        // Enumerate the ready tasks (those with dependencies resolved) and kickoff them concurrently
        //
        while (entry != null) {
            final TaskGroupEntry<ResultT, TaskT> currentEntry = entry;
            Observable<ResultT> currentTaskObservable = invokeTaskAsync(currentEntry, context);
            Func1<ResultT, Observable<ResultT>> onNext = new Func1<ResultT, Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call(ResultT taskResult) {
                    return Observable.just(taskResult);
                }
            };
            Func1<Throwable, Observable<ResultT>> onError = new Func1<Throwable, Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call(Throwable throwable) {
                    // Append next observable on error terminate event of this observable
                    return processFaultedTaskAsync(currentEntry, throwable, context);
                }
            };
            Func0<Observable<ResultT>> onComplete = new Func0<Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call() {
                    // Append next observable on successful terminate event of this observable
                    return processCompletedTaskAsync(currentEntry, context);
                }
            };
            observables.add(currentTaskObservable.flatMap(onNext, onError, onComplete));
            entry = super.getNext();
        }
        return Observable.mergeDelayError(observables, 1);
    }

    /**
     * Invokes the task stored in the given entry.
     * <p>
     * if the task cannot be invoked because the group marked as cancelled then an observable
     * that emit {@link TaskCancelledException} will be returned.
     *
     * @param entry the entry holding task
     * @param context a group level shared context that is passed to {@link TaskItem#invokeAsync(InvocationContext)}
     *                method of the task item this entry wraps.
     *
     * @return an observable represents result of task in the given entry.
     */
    private Observable<ResultT> invokeTaskAsync(final TaskGroupEntry<ResultT, TaskT> entry, final InvocationContext context) {
        if (this.isGroupCancelled) {
            return toErrorObservable(taskCancelledException);
        }
        return entry.invokeTaskAsync(isRootEntry(entry), context);
    }

    /**
     * Handles successful completion of a task.
     * <p>
     * If the task is not root (terminal) task then this kickoff execution of next set of ready tasks
     *
     * @param completedEntry the entry holding completed task
     * @param context the context object shared across all the task entries in this group during execution
     *
     * @return an observable represents asynchronous operation in the next stage
     */
    private Observable<ResultT> processCompletedTaskAsync(final TaskGroupEntry<ResultT, TaskT> completedEntry,
                                                          final InvocationContext context) {
        reportCompletion(completedEntry);
        if (isRootEntry(completedEntry)) {
            return Observable.empty();
        }
        return invokeReadyTasksAsync(context);
    }

    /**
     * Handles a faulted task.
     *
     * @param faultedEntry the entry holding faulted task
     * @param throwable the reason for fault
     * @param context the context object shared across all the task entries in this group during execution
     *
     * @return an observable represents asynchronous operation in the next stage
     */
    private Observable<ResultT> processFaultedTaskAsync(final TaskGroupEntry<ResultT, TaskT> faultedEntry,
                                                        final Throwable throwable,
                                                        final InvocationContext context) {
        this.isGroupCancelled = this.taskGroupTerminateOnErrorStrategy
                == TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_INPROGRESS_TASKS_COMPLETION;
        reportError(faultedEntry, throwable);
        if (isRootEntry(faultedEntry)) {
            if (shouldPropagateException(throwable)) {
                return toErrorObservable(throwable);
            }
            return Observable.empty();
        }
        if (shouldPropagateException(throwable)) {
            return Observable.concatDelayError(invokeReadyTasksAsync(context), toErrorObservable(throwable));
        }
        return invokeReadyTasksAsync(context);
    }

    /**
     * Check that given entry is the root entry in this group.
     *
     * @param taskGroupEntry the entry
     * @return true if the entry is root entry in the group, false otherwise.
     */
    private boolean isRootEntry(TaskGroupEntry<ResultT, TaskT> taskGroupEntry) {
        return isRootNode(taskGroupEntry);
    }

    /**
     * Checks the given throwable needs to be propagated to final stream returned by
     * {@link this#invokeAsync(InvocationContext)} ()} method.
     *
     * @param throwable the exception to check
     * @return true if the throwable needs to be included in the {@link rx.exceptions.CompositeException}
     * emitted by the final stream.
     */
    private static boolean shouldPropagateException(Throwable throwable) {
        return (!(throwable instanceof ErroredDependencyTaskException)
                && !(throwable instanceof TaskCancelledException));
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
     * @return this instance with TaskItem param down casted.
     */
    @SuppressWarnings("unchecked")
    private TaskGroup<ResultT, TaskItem<ResultT>> that() {
        // TaskGroup<ResultT, TaskT extends TaskItem<ResultT>> --> TaskGroup<ResultT, TaskItem<ResultT>>
        //
        return (TaskGroup<ResultT, TaskItem<ResultT>>) this;
    }

    /**
     * @return a new clean context instance.
     */
    public InvocationContext newInvocationContext() {
        return new InvocationContext(this);
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

    /**
     * A mutable type that can be used to pass data around task items during the invocation
     * of the TaskGroup.
     */
    public static final class InvocationContext {
        private final Map<String, Object> properties;
        private final TaskGroup<?, ?> taskGroup;

        /**
         * Creates InvocationContext instance.
         *
         * @param taskGroup the task group that uses this context instance.
         */
        private InvocationContext(final TaskGroup<?, ?> taskGroup) {
            this.properties = new ConcurrentHashMap<>();
            this.taskGroup = taskGroup;
        }

        /**
         * Put a key-value in the context.
         *
         * @param key the key
         * @param value the value
         */
        public void put(String key, Object value) {
            this.properties.put(key, value);
        }

        /**
         * Get a value in the context with the given key.
         *
         * @param key the key
         * @return value with the given key if exists, null otherwise.
         */
        public Object get(String key) {
            return this.properties.get(key);
        }

        /**
         * Check existence of a key in the context.
         *
         * @param key the key
         * @return true if the key exists, false otherwise.
         */
        public boolean hasKey(String key) {
            return this.get(key) != null;
        }
    }

    /**
     * Wrapper type to simplify operations on proxy TaskGroup.
     * <p>
     * A proxy TaskGroup will be activated for a TaskGroup as soon as a "post-run" dependent
     * added to the actual TaskGroup via {@link TaskGroup#addPostRunDependentTaskGroup(TaskGroup)}.
     * "post run" dependents are those TaskGroup which need to be invoked as part of invocation
     * of actual TaskGroup.
     *
     * @param <R> type of the result returned by the tasks in the proxy TaskGroup.
     */
    static final class ProxyTaskGroupWrapper<R> {
        // The proxy TaskGroup
        private TaskGroup<R, TaskItem<R>> proxyTaskGroup;
        // The actual TaskGroup for which above TaskGroup act as proxy
        private final TaskGroup<R, TaskItem<R>> actualTaskGroup;
        // The actual TaskGroup's termination strategy
        private final TaskGroupTerminateOnErrorStrategy terminationStrategy;

        /**
         * Creates ProxyTaskGroupWrapper.
         *
         * @param actualTaskGroup the actual TaskGroup for which proxy TaskGroup will be enabled
         * @param terminationStrategy the actual TaskGroup's termination strategy
         */
        ProxyTaskGroupWrapper(TaskGroup<R, TaskItem<R>> actualTaskGroup,
                              TaskGroupTerminateOnErrorStrategy terminationStrategy) {
            this.actualTaskGroup = actualTaskGroup;
            this.terminationStrategy = terminationStrategy;
        }

        /**
         * @return true if the proxy TaskGroup is enabled for original TaskGroup.
         */
        boolean isActive() {
            return this.proxyTaskGroup != null;
        }

        /**
         * @return the wrapped proxy task group.
         */
        TaskGroup<R, TaskItem<R>> proxyTaskGroup() {
            return this.proxyTaskGroup;
        }

        /**
         * Add a dependency for the proxy TaskGroup.
         *
         * @param dependencyTaskGroup the dependency TaskGroup.
         */
        void addDependencyTaskGroup(TaskGroup<R, TaskItem<R>> dependencyTaskGroup) {
            if (this.proxyTaskGroup == null) {
                ProxyTaskItem<R> proxyTaskItem = new ProxyTaskItem<>(this.actualTaskGroup.root().data());
                this.proxyTaskGroup = new TaskGroup<R, TaskItem<R>>("proxy-" + this.actualTaskGroup.root().key(),
                        proxyTaskItem,
                        this.terminationStrategy);
                this.proxyTaskGroup.addDependencyGraph(this.actualTaskGroup);
            }
            this.proxyTaskGroup.addDependencyGraph(dependencyTaskGroup);
        }

        /**
         * Add a dependent for the proxy TaskGroup.
         *
         * @param dependentTaskGroup the dependent TaskGroup
         */
        void addDependentTaskGroup(TaskGroup<R, TaskItem<R>> dependentTaskGroup) {
            if (this.proxyTaskGroup == null) {
                throw new IllegalStateException("addDependentTaskGroup() cannot be called in a non-active ProxyTaskGroup");
            }
            dependentTaskGroup.addDependencyGraph(this.proxyTaskGroup);
        }

        /**
         * Invokes the tasks grouped under the proxy TaskGroup.
         *
         * @param context the context shared across the the all task items in the group this task item belongs to.
         * @return an observable that emits the invocation result of tasks in the TaskGroup.
         */
        Observable<R> invokeAsync(InvocationContext context) {
            if (this.proxyTaskGroup == null) {
                throw new IllegalStateException("invokeAsync(cxt) cannot be called in a non-active ProxyTaskGroup");
            }
            return this.proxyTaskGroup.invokeAsync(context);
        }

        /**
         * A {@link TaskItem} type that act as proxy for another {@link TaskItem}.
         *
         * @param <R> the type of the result produced the task this proxy wraps.
         */
        private static final class ProxyTaskItem<R> implements TaskItem<R> {
            private final TaskItem<R> taskItem;

            private ProxyTaskItem(final TaskItem<R> taskItem) {
                this.taskItem = taskItem;
            }

            @Override
            public R result() {
                return taskItem.result();
            }

            @Override
            public void prepare() {
                // NOP
            }

            @Override
            public boolean isHot() {
                return taskItem.isHot();
            }

            @Override
            public Observable<R> invokeAsync(InvocationContext context) {
                return Observable.just(taskItem.result());
            }
        }
    }
}
