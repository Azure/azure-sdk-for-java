// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Type representing a group of task entries with dependencies between them. Initially a task
 * group will have only one task entry known as root task entry, then more entries can be
 * added by taking dependency on other task groups or adding "post-run" task group dependents.
 * <p>
 * The method {@link TaskGroup#invokeAsync(InvocationContext)} ()} kick-off invocation of tasks
 * in the group, task are invoked in topological sorted order.
 * <p>
 * {@link TaskGroup#addDependencyTaskGroup(TaskGroup)}: A task group "A" can take dependency on
 * another task group "B" through this method e.g. `A.addDependencyTaskGroup(B)` indicates that
 * completion of tasks in the dependency task group "B" is required before the invocation of root
 * task in group "A". A.invokeAsync(cxt) will ensure this order.
 * <p>
 * {@link TaskGroup#addPostRunDependentTaskGroup(TaskGroup)}: there are scenarios where a subset
 * of dependent task groups say "H", "I" may required to run after the invocation of a task group
 * "K" when K.invokeAsync(cxt) is called. Such special dependents can be added via
 * K.addPostRunDependentTaskGroup(H) and K.addPostRunDependentTaskGroup(I).
 * <p>
 * The result produced by the tasks in the group are of type {@link Indexable}.
 */
public class TaskGroup
        extends DAGraph<TaskItem, TaskGroupEntry<TaskItem>>
        implements Indexable {
    /**
     * The root task in this task group.
     */
    private final TaskGroupEntry<TaskItem> rootTaskEntry;
    /**
     * Task group termination strategy to be used once any task in the group error-ed.
     */
    private TaskGroupTerminateOnErrorStrategy taskGroupTerminateOnErrorStrategy;
    /**
     * Flag indicating whether this group is marked as cancelled or not. This flag will be used only
     * when group's terminate on error strategy is set as
     * {@link TaskGroupTerminateOnErrorStrategy#TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION}.
     * Effect of setting this flag can be think as broadcasting a cancellation signal to tasks those
     * are yet to invoke.
     */
    private final AtomicBoolean isGroupCancelled;
    /**
     * The shared exception object used to indicate that a task is not invoked since the group
     * is marked as cancelled i.e. {@link this#isGroupCancelled} is set.
     */
    private final TaskCancelledException taskCancelledException = new TaskCancelledException();
    /**
     * The helper to operate on proxy TaskGroup of this TaskGroup for supporting dependents marked
     * for post run.
     */
    protected ProxyTaskGroupWrapper proxyTaskGroupWrapper;

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskEntry the entry holding root task
     */
    private TaskGroup(TaskGroupEntry<TaskItem> rootTaskEntry) {
        super(rootTaskEntry);
        this.isGroupCancelled = new AtomicBoolean(false);
        this.rootTaskEntry = rootTaskEntry;
        this.proxyTaskGroupWrapper = new ProxyTaskGroupWrapper(this);
    }

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskItemId the id of the root task in the group
     * @param rootTaskItem the root task
     */
    public TaskGroup(String rootTaskItemId,
                     TaskItem rootTaskItem) {
        this(new TaskGroupEntry<TaskItem>(rootTaskItemId, rootTaskItem));
    }

    /**
     * Creates TaskGroup.
     *
     * @param rootTaskItem the root task
     */
    public TaskGroup(IndexableTaskItem rootTaskItem) {
        this(new TaskGroupEntry<TaskItem>(rootTaskItem.key(), rootTaskItem));
    }

    /**
     * @return the key of this task group, which is same as key of the root entry in the group
     */
    @Override
    public String key() {
        return this.rootTaskEntry.key();
    }

    /**
     * Retrieve the result produced by a task with the given id in the group.
     * <p>
     * This method can be used to retrieve the result of invocation of both dependency
     * and "post-run" dependent tasks. If task with the given id does not exists then
     * IllegalArgumentException exception will be thrown.
     *
     * @param taskId the task item id
     * @return the task result, null will be returned if task has not yet been invoked
     */
    public Indexable taskResult(String taskId) {
        TaskGroupEntry<TaskItem> taskGroupEntry = super.getNode(taskId);
        if (taskGroupEntry != null) {
            return taskGroupEntry.taskResult();
        }
        if (!this.proxyTaskGroupWrapper.isActive()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("A dependency task with id '" + taskId + "' is not found"));
        }
        taskGroupEntry = this.proxyTaskGroupWrapper.proxyTaskGroup.getNode(taskId);
        if (taskGroupEntry != null) {
            return taskGroupEntry.taskResult();
        }
        throw logger.logExceptionAsError(new IllegalArgumentException(
            "A dependency task or 'post-run' dependent task with with id '" + taskId + "' not found"));
    }

    /**
     * Checks this TaskGroup depends on the given TaskGroup.
     *
     * @param taskGroup the TaskGroup to check
     * @return true if TaskGroup is depends on the given TaskGroup
     */
    public boolean dependsOn(TaskGroup taskGroup) {
        return this.nodeTable.containsKey(taskGroup.root().key());
    }

    /**
     * @return the root task entry in the group.
     */
    protected TaskGroupEntry<TaskItem> root() {
        return this.rootTaskEntry;
    }

    /**
     * Mark root of this task task group depends on the given TaskItem.
     * This ensure this task group's root get picked for execution only after the completion
     * of invocation of provided TaskItem.
     *
     * @param dependencyTaskItem the task item that this task group depends on
     * @return the key of the dependency
     */
    public String addDependency(FunctionalTaskItem dependencyTaskItem) {
        IndexableTaskItem dependency = IndexableTaskItem.create(dependencyTaskItem);
        this.addDependency(dependency);
        return dependency.key();
    }

    /**
     * Mark root of this task task group depends on the given item's taskGroup.
     * This ensure this task group's root get picked for execution only after the completion
     * of invocation of provided TaskItem.
     *
     * @param hasTaskGroup an item with taskGroup that this task group depends on
     */
    public void addDependency(TaskGroup.HasTaskGroup hasTaskGroup) {
        this.addDependencyTaskGroup(hasTaskGroup.taskGroup());
    }

    /**
     * Mark root of this task task group depends on the given task group's root.
     * This ensure this task group's root get picked for execution only after the completion
     * of all tasks in the given group.
     *
     * @param dependencyTaskGroup the task group that this task group depends on
     */
    public void addDependencyTaskGroup(TaskGroup dependencyTaskGroup) {
        if (dependencyTaskGroup.proxyTaskGroupWrapper.isActive()) {
            dependencyTaskGroup.proxyTaskGroupWrapper.addDependentTaskGroup(this);
        } else {
            DAGraph<TaskItem, TaskGroupEntry<TaskItem>> dependencyGraph = dependencyTaskGroup;
            super.addDependencyGraph(dependencyGraph);
        }
    }

    /**
     * Mark the given TaskItem depends on this taskGroup.
     *
     * @param dependentTaskItem the task item that depends on this task group
     * @return key to be used as parameter to taskResult(string) method to retrieve result of
     * invocation of given task item.
     */
    public String addPostRunDependent(FunctionalTaskItem dependentTaskItem) {
        IndexableTaskItem taskItem = IndexableTaskItem.create(dependentTaskItem);
        this.addPostRunDependent(taskItem);
        return taskItem.key();
    }

    /**
     * Mark the given TaskItem depends on this taskGroup.
     *
     * @param dependentTaskItem the task item that depends on this task group
     * @param internalContext the internal runtime context
     * @return key to be used as parameter to taskResult(string) method to retrieve result of
     * invocation of given task item.
     */
    public String addPostRunDependent(
        FunctionalTaskItem dependentTaskItem, ResourceManagerUtils.InternalRuntimeContext internalContext) {
        IndexableTaskItem taskItem = IndexableTaskItem.create(dependentTaskItem, internalContext);
        this.addPostRunDependent(taskItem);
        return taskItem.key();
    }

    /**
     * Mark the given item with taskGroup depends on this taskGroup.
     *
     * @param hasTaskGroup an item with as task group that depends on this task group
     */
    public void addPostRunDependent(TaskGroup.HasTaskGroup hasTaskGroup) {
        this.addPostRunDependentTaskGroup(hasTaskGroup.taskGroup());
    }

    /**
     * Mark root of the given task group depends on this task group's root.
     * This ensure given task group's root get picked for invocation only after the completion
     * of all tasks in this group. Calling invokeAsync(cxt) will run the tasks in the given
     * dependent task group as well.
     *
     * @param dependentTaskGroup the task group depends on this task group
     */
    public void addPostRunDependentTaskGroup(TaskGroup dependentTaskGroup) {
        this.proxyTaskGroupWrapper.addPostRunTaskGroupForActualTaskGroup(dependentTaskGroup);
    }

    /**
     * Invokes tasks in the group.
     * It is not guaranteed to return indexable in topological order.
     *
     * @param context group level shared context that need be passed to invokeAsync(cxt)
     *                method of each task item in the group when it is selected for invocation.
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    public Flux<Indexable> invokeAsync(final InvocationContext context) {
        return Flux.defer(() -> {
            if (proxyTaskGroupWrapper.isActive()) {
                return proxyTaskGroupWrapper.taskGroup().invokeInternAsync(context, true, null);
            } else {
                Set<String> processedKeys = runBeforeGroupInvoke(null);
                if (proxyTaskGroupWrapper.isActive()) {
                    // If proxy got activated after 'runBeforeGroupInvoke()' stage due to the addition of direct
                    // 'postRunDependent's then delegate group invocation to proxy group.
                    //
                    return proxyTaskGroupWrapper.taskGroup().invokeInternAsync(context, true, processedKeys);
                } else {
                    return invokeInternAsync(context, false, null);
                }
            }
        });
    }

    /**
     * Invokes tasks in the group.
     *
     * @return the root result of task group.
     */
    public Mono<Indexable> invokeAsync() {
        return invokeAsync(this.newInvocationContext())
            .then(Mono.defer(() -> {
                if (proxyTaskGroupWrapper.isActive()) {
                    return Mono.just(proxyTaskGroupWrapper.taskGroup().root().taskResult());
                }
                return Mono.just(root().taskResult());
            }));
    }

    /**
     * Invokes dependency tasks in the group, but not.
     *
     * @param context group level shared context that need be passed to invokeAsync(cxt)
     *                method of each task item in the group when it is selected for invocation.
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    public Flux<Indexable> invokeDependencyAsync(final InvocationContext context) {
        context.put(TaskGroup.InvocationContext.KEY_SKIP_TASKS, Collections.singleton(this.key()));
        return Flux.defer(() -> {
            if (proxyTaskGroupWrapper.isActive()) {
                return Flux.error(new IllegalStateException("postRunDependent is not supported"));
            } else {
                Set<String> processedKeys = runBeforeGroupInvoke(null);
                if (proxyTaskGroupWrapper.isActive()) {
                    return Flux.error(new IllegalStateException("postRunDependent is not supported"));
                } else {
                    return invokeInternAsync(context, false, null);
                }
            }
        });
    }

    /**
     * Invokes tasks in the group.
     *
     * @param context group level shared context that need be passed to invokeAsync(cxt)
     *                                   method of each task item in the group when it is selected for invocation.
     * @param shouldRunBeforeGroupInvoke indicate whether to run the 'beforeGroupInvoke' method
     *                                   of each tasks before invoking them
     * @param skipBeforeGroupInvoke the tasks keys for which 'beforeGroupInvoke' should not be called
     *                                   before invoking them
     * @return an observable that emits the result of tasks in the order they finishes.
     */
    private Flux<Indexable> invokeInternAsync(final InvocationContext context,
                                              final boolean shouldRunBeforeGroupInvoke,
                                              final Set<String> skipBeforeGroupInvoke) {
        if (!isPreparer()) {
            return Flux.error(new IllegalStateException(
                "invokeInternAsync(cxt) can be called only from root TaskGroup"));
        }
        this.taskGroupTerminateOnErrorStrategy = context.terminateOnErrorStrategy();
        if (shouldRunBeforeGroupInvoke) {
            // Prepare tasks and queue the ready tasks (terminal tasks with no dependencies)
            //
            this.runBeforeGroupInvoke(skipBeforeGroupInvoke);
        }
        // Runs the ready tasks concurrently
        //
        return this.invokeReadyTasksAsync(context);
    }

    /**
     * Run 'beforeGroupInvoke' method of the tasks in this group. The tasks can use beforeGroupInvoke()
     * method to add additional dependencies or dependents.
     *
     * @param skip the keys of the tasks that are previously processed hence they must be skipped
     * @return the keys of all the tasks those are processed (including previously processed items in skip param)
     */
    private Set<String> runBeforeGroupInvoke(final Set<String> skip) {
        HashSet<String> processedEntryKeys = new HashSet<>();
        if (skip != null) {
            processedEntryKeys.addAll(skip);
        }
        List<TaskGroupEntry<TaskItem>> entries = this.entriesSnapshot();
        boolean hasMoreToProcess;
        // Invokes 'beforeGroupInvoke' on a subset of non-processed tasks in the group.
        // Initially processing is pending on all task items.
        do {
            hasMoreToProcess = false;
            for (TaskGroupEntry<TaskItem> entry : entries) {
                if (!processedEntryKeys.contains(entry.key())) {
                    entry.data().beforeGroupInvoke();
                    processedEntryKeys.add(entry.key());
                }
            }
            int prevSize = entries.size();
            entries = this.entriesSnapshot();
            if (entries.size() > prevSize) {
                // If new task dependencies/dependents added in 'beforeGroupInvoke' then
                // set the flag which indicates another pass is required to 'prepare' new
                // task items
                hasMoreToProcess = true;
            }
        } while (hasMoreToProcess);  // Run another pass if new dependencies/dependents were added in this pass
        super.prepareForEnumeration();
        return processedEntryKeys;
    }

    /**
     * @return list with current task entries in this task group
     */
    private List<TaskGroupEntry<TaskItem>> entriesSnapshot() {
        List<TaskGroupEntry<TaskItem>> entries = new ArrayList<>();
        super.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> current = super.getNext(); current != null; current = super.getNext()) {
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
     * @return a {@link Flux} that emits the result of tasks in the order they finishes.
     */
    // Due to it takes approximate 3ms in flux for returning, it cannot be guaranteed to return in topological order.
    // One simply fix for guaranteeing the last element could be https://github.com/Azure/azure-sdk-for-java/pull/15074
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Flux<Indexable> invokeReadyTasksAsync(final InvocationContext context) {
        TaskGroupEntry<TaskItem> readyTaskEntry = super.getNext();
        final List<Flux<Indexable>> observables = new ArrayList<>();
        // Enumerate the ready tasks (those with dependencies resolved) and kickoff them concurrently
        //
        while (readyTaskEntry != null) {
            final TaskGroupEntry<TaskItem> currentEntry = readyTaskEntry;
            final TaskItem currentTaskItem = currentEntry.data();
            if (currentTaskItem instanceof ProxyTaskItem) {
                observables.add(invokeAfterPostRunAsync(currentEntry, context));
            } else {
                observables.add(invokeTaskAsync(currentEntry, context));
            }
            readyTaskEntry = super.getNext();
        }
        return Flux.mergeDelayError(32, observables.toArray(new Flux[0]));
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
     * @return an observable that emits result of task in the given entry and result of subset of tasks which gets
     * scheduled after this task.
     */
    private Flux<Indexable> invokeTaskAsync(final TaskGroupEntry<TaskItem> entry, final InvocationContext context) {
        return Flux.defer(() -> {
            if (isGroupCancelled.get()) {
                // One or more tasks are in faulted state, though this task MAYBE invoked if it does not
                // have faulted tasks as transitive dependencies, we won't do it since group is cancelled
                // due to termination strategy TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION.
                //
                return processFaultedTaskAsync(entry, taskCancelledException, context);
            } else {
                // Any cached result will be ignored for root resource
                //
                boolean ignoreCachedResult = isRootEntry(entry)
                    || (entry.proxy() != null && isRootEntry(entry.proxy()));

                Mono<Indexable> taskObservable;
                Object skipTasks = context.get(InvocationContext.KEY_SKIP_TASKS);
                if (skipTasks instanceof Set && ((Set) skipTasks).contains(entry.key())) {
                    taskObservable = Mono.just(new VoidIndexable(entry.key()));
                } else {
                    taskObservable = entry.invokeTaskAsync(ignoreCachedResult, context);
                }
                return taskObservable.flatMapMany((indexable) -> Flux.just(indexable),
                    (throwable) -> processFaultedTaskAsync(entry, throwable, context),
                    () -> processCompletedTaskAsync(entry, context));
            }
        });
    }

    /**
     * Invokes the {@link TaskItem#invokeAfterPostRunAsync(boolean)} method of an actual TaskItem
     * if the given entry holds a ProxyTaskItem.
     *
     * @param entry the entry holding a ProxyTaskItem
     * @param context a group level shared context
     * @return An Observable that represents asynchronous work started by
     * {@link TaskItem#invokeAfterPostRunAsync(boolean)} method of actual TaskItem and result of subset
     * of tasks which gets scheduled after proxy task. If group was not in faulted state and
     * {@link TaskItem#invokeAfterPostRunAsync(boolean)} emits no error then stream also includes
     * result produced by actual TaskItem.
     */
    private Flux<Indexable> invokeAfterPostRunAsync(final TaskGroupEntry<TaskItem> entry,
                                                    final InvocationContext context) {
        return Flux.defer(() -> {
            final ProxyTaskItem proxyTaskItem = (ProxyTaskItem) entry.data();
            if (proxyTaskItem == null) {
                return Flux.empty();
            }
            final boolean isFaulted = entry.hasFaultedDescentDependencyTasks() || isGroupCancelled.get();

            return proxyTaskItem.invokeAfterPostRunAsync(isFaulted)
                    .flatMapMany(indexable -> Flux.error(
                        new IllegalStateException("This onNext should never be called")),
                        (error) -> processFaultedTaskAsync(entry, error, context),
                        () -> {
                            if (isFaulted) {
                                if (entry.hasFaultedDescentDependencyTasks()) {
                                    return processFaultedTaskAsync(entry,
                                        new ErroredDependencyTaskException(), context);
                                } else {
                                    return processFaultedTaskAsync(entry, taskCancelledException, context);
                                }
                            } else {
                                return Flux.concat(Flux.just(proxyTaskItem.result()),
                                        processCompletedTaskAsync(entry, context));
                            }
                        });

        });
    }

    /**
     * Handles successful completion of a task.
     * <p>
     * If the task is not root (terminal) task then this kickoff execution of next set of ready tasks
     *
     * @param completedEntry the entry holding completed task
     * @param context the context object shared across all the task entries in this group during execution
     * @return an observable represents asynchronous operation in the next stage
     */
    private Flux<Indexable> processCompletedTaskAsync(final TaskGroupEntry<TaskItem> completedEntry,
                                                      final InvocationContext context) {
        reportCompletion(completedEntry);
        if (isRootEntry(completedEntry)) {
            return Flux.empty();
        } else {
            return invokeReadyTasksAsync(context);
        }
    }

    /**
     * Handles a faulted task.
     *
     * @param faultedEntry the entry holding faulted task
     * @param throwable the reason for fault
     * @param context the context object shared across all the task entries in this group during execution
     * @return an observable represents asynchronous operation in the next stage
     */
    private Flux<Indexable> processFaultedTaskAsync(final TaskGroupEntry<TaskItem> faultedEntry,
                                                    final Throwable throwable,
                                                    final InvocationContext context) {
        markGroupAsCancelledIfTerminationStrategyIsIPTC();
        reportError(faultedEntry, throwable);
        if (isRootEntry(faultedEntry)) {
            if (shouldPropagateException(throwable)) {
                return toErrorObservable(throwable);
            }
            return Flux.empty();
        } else if (shouldPropagateException(throwable)) {
            return Flux.concatDelayError(invokeReadyTasksAsync(context), toErrorObservable(throwable));
        } else {
            return invokeReadyTasksAsync(context);
        }
    }

    /**
     * Mark this TaskGroup as cancelled if the termination strategy associated with the group
     * is {@link TaskGroupTerminateOnErrorStrategy#TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION}.
     */
    private void markGroupAsCancelledIfTerminationStrategyIsIPTC() {
        this.isGroupCancelled.set(this.taskGroupTerminateOnErrorStrategy
            == TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);
    }

    /**
     * Check that given entry is the root entry in this group.
     *
     * @param taskGroupEntry the entry
     * @return true if the entry is root entry in the group, false otherwise.
     */
    private boolean isRootEntry(TaskGroupEntry<TaskItem> taskGroupEntry) {
        return isRootNode(taskGroupEntry);
    }

    /**
     * Checks the given throwable needs to be propagated to final stream returned by
     * {@link this#invokeAsync(InvocationContext)} ()} method.
     *
     * @param throwable the exception to check
     * @return true if the throwable needs to be included in the {@link RuntimeException}
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
    private Flux<Indexable> toErrorObservable(Throwable throwable) {
        return Flux.error(throwable);
    }

    /**
     * @return a new clean context instance.
     */
    public InvocationContext newInvocationContext() {
        return new InvocationContext(this);
    }

    /**
     * An interface representing a type composes a TaskGroup.
     */
    public interface HasTaskGroup {
        /**
         * @return Gets the task group.
         */
        TaskGroup taskGroup();
    }

    /**
     * A mutable type that can be used to pass data around task items during the invocation
     * of the TaskGroup.
     */
    public static final class InvocationContext {
        public static final String KEY_SKIP_TASKS = "SKIP_TASKS";

        private final Map<String, Object> properties;
        private final TaskGroup taskGroup;
        private TaskGroupTerminateOnErrorStrategy terminateOnErrorStrategy;
        private final ClientLogger logger = new ClientLogger(this.getClass());

        /**
         * Creates InvocationContext instance.
         *
         * @param taskGroup the task group that uses this context instance.
         */
        private InvocationContext(final TaskGroup taskGroup) {
            this.properties = new ConcurrentHashMap<>();
            this.taskGroup = taskGroup;
        }

        /**
         * @return the TaskGroup this invocation context associated with.
         */
        public TaskGroup taskGroup() {
            return this.taskGroup;
        }

        /**
         * Sets the group termination strategy to use on error.
         *
         * @param strategy the strategy
         * @return the context
         */
        public InvocationContext withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy strategy) {
            if (this.terminateOnErrorStrategy != null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Termination strategy is already set, it is immutable for a specific context"));
            }
            this.terminateOnErrorStrategy = strategy;
            return this;
        }

        /**
         * @return the termination strategy to use upon error during the current invocation of the TaskGroup.
         */
        public TaskGroupTerminateOnErrorStrategy terminateOnErrorStrategy() {
            if (this.terminateOnErrorStrategy == null) {
                return TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_HITTING_LCA_TASK;
            }
            return this.terminateOnErrorStrategy;
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
     */
    protected static final class ProxyTaskGroupWrapper {
        // The "proxy TaskGroup"
        private TaskGroup proxyTaskGroup;
        // The "actual TaskGroup" for which above TaskGroup act as proxy
        private final TaskGroup actualTaskGroup;

        private final ClientLogger logger = new ClientLogger(this.getClass());

        /**
         * Creates ProxyTaskGroupWrapper.
         *
         * @param actualTaskGroup the actual TaskGroup for which proxy TaskGroup will be enabled
         */
        ProxyTaskGroupWrapper(TaskGroup actualTaskGroup) {
            this.actualTaskGroup = actualTaskGroup;
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
        TaskGroup taskGroup() {
            return this.proxyTaskGroup;
        }

        /**
         * Add "post-run TaskGroup" for the "actual TaskGroup".
         *
         * @param postRunTaskGroup the dependency TaskGroup.
         */
        void addPostRunTaskGroupForActualTaskGroup(TaskGroup postRunTaskGroup) {
            if (this.proxyTaskGroup == null) {
                this.initProxyTaskGroup();
            }
            postRunTaskGroup.addDependencyGraph(this.actualTaskGroup);
            if (postRunTaskGroup.proxyTaskGroupWrapper.isActive()) {
                this.proxyTaskGroup.addDependencyGraph(postRunTaskGroup.proxyTaskGroupWrapper.proxyTaskGroup);
            } else {
                this.proxyTaskGroup.addDependencyGraph(postRunTaskGroup);
            }
        }

        /**
         * Add a dependent for the proxy TaskGroup.
         *
         * @param dependentTaskGroup the dependent TaskGroup
         */
        void addDependentTaskGroup(TaskGroup dependentTaskGroup) {
            if (this.proxyTaskGroup == null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "addDependentTaskGroup() cannot be called in a non-active ProxyTaskGroup"));
            }
            dependentTaskGroup.addDependencyGraph(this.proxyTaskGroup);
        }

        /**
         * Initialize the proxy TaskGroup if not initialized yet.
         */
        private void initProxyTaskGroup() {
            if (this.proxyTaskGroup == null) {
                // Creates proxy TaskGroup with an instance of ProxyTaskItem as root TaskItem which delegates actions on
                // it to "actual TaskGroup"'s root.
                //
                ProxyTaskItem proxyTaskItem = new ProxyTaskItem(this.actualTaskGroup.root().data());
                this.proxyTaskGroup = new TaskGroup("proxy-" + this.actualTaskGroup.root().key(),
                        proxyTaskItem);

                if (this.actualTaskGroup.hasParents()) {
                    // Once "proxy TaskGroup" is enabled, all existing TaskGroups depends on "actual TaskGroup" should
                    // take dependency on "proxy TaskGroup".
                    //
                    String atgRootKey = this.actualTaskGroup.root().key();
                    for (DAGraph<TaskItem, TaskGroupEntry<TaskItem>> parentDAG : this.actualTaskGroup.parentDAGs) {
                        parentDAG.root().removeDependency(atgRootKey);
                        parentDAG.addDependencyGraph(this.proxyTaskGroup);
                    }
                    // re-assigned actual's parents as proxy's parents, so clear actual's parent collection.
                    //
                    this.actualTaskGroup.parentDAGs.clear();
                }
                // "Proxy TaskGroup" takes dependency on "actual TaskGroup"
                //
                this.proxyTaskGroup.addDependencyGraph(this.actualTaskGroup);
                // Add a back reference to "proxy" in actual
                //
                this.actualTaskGroup.rootTaskEntry.setProxy(this.proxyTaskGroup.rootTaskEntry);
            }
        }
    }

    /**
     * A {@link TaskItem} type that act as proxy for another {@link TaskItem}.
     */
    private static final class ProxyTaskItem implements TaskItem {
        private final TaskItem actualTaskItem;

        private ProxyTaskItem(final TaskItem actualTaskItem) {
            this.actualTaskItem = actualTaskItem;
        }

        @Override
        public Indexable result() {
            return actualTaskItem.result();
        }


        @Override
        public void beforeGroupInvoke() {
            // NOP
        }

        @Override
        public boolean isHot() {
            return actualTaskItem.isHot();
        }

        @Override
        public Mono<Indexable> invokeAsync(InvocationContext context) {
            return Mono.just(actualTaskItem.result());
        }

        @Override
        public Mono<Void> invokeAfterPostRunAsync(final boolean isGroupFaulted) {
            if (actualTaskItem.isHot()) {
                return Mono.defer(() ->
                    actualTaskItem.invokeAfterPostRunAsync(isGroupFaulted).subscribeOn(Schedulers.immediate()));
            } else {
                return this.actualTaskItem.invokeAfterPostRunAsync(isGroupFaulted)
                        .subscribeOn(Schedulers.immediate());
            }
        }
    }
}
