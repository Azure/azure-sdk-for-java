// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sync-stack variant of {@link DAGErrorTests}.
 */
public class SyncDAGErrorTests {
    private static final ClientLogger LOGGER = new ClientLogger(SyncDAGErrorTests.class);

    @Test
    public void testTerminateOnInProgressTaskCompletion() {
        // Terminate on error strategy used in this task group is
        // TaskGroupTerminateOnErrorStrategy::TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION

        // The task B start and asynchronously wait for 4000 ms then emit an error.
        // The tasks Q and I start and asynchronous wait for 8000 ms and then report
        // completion. The more delay in Q and I ensure that B emits error before Q and
        // I finishes.
        //
        // In this setup,
        //    D and E cannot be executed since their dependency B is faulted.
        //    P, H, L and G cannot be executed since fault in B cause group cancellation
        //
        /**
         *                                                                        |--------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)----->[N](0)
         *                                 X                     |
         *   |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *   |                             ^                     ^
         *   |                             |                     |
         *  [F](6)---->[E](5)--------------|                     |
         *   |          |                                        |
         *   |          |------->[G](4)-->[C](3)------------------
         *   |                    |
         *   |                    |============================>[L](2)---------->[P](1)=====>[Q](0)
         *   |
         *   |------------------------------------------------------------------>[H](1)----->[I](0)
         */
        final Set<String> seen = new HashSet<>();
        Function<Indexable, IPancake> consumeCake = indexable -> {
            IPancake pancake = (IPancake) indexable;
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onNext: " + pancake.name());
            synchronized (seen) {
                seen.add(pancake.name());
            }
            return pancake;
        };
        PancakeImpl pancakeM = new PancakeImplWrapper("M", 250, consumeCake);
        PancakeImpl pancakeN = new PancakeImplWrapper("N", 250, consumeCake);
        PancakeImpl pancakeK = new PancakeImplWrapper("K", 250, consumeCake);
        PancakeImpl pancakeQ = new PancakeImplWrapper("Q", 8000, consumeCake);
        PancakeImpl pancakeI = new PancakeImplWrapper("I", 8000, consumeCake);

        PancakeImpl pancakeJ = new PancakeImplWrapper("J", 250, consumeCake);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImplWrapper("P", 250, consumeCake);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImplWrapper("H", 250, consumeCake);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImplWrapper("A", 250, consumeCake);
        PancakeImpl pancakeL = new PancakeImplWrapper("L", 250, consumeCake);
        pancakeL.withInstantPancake(pancakeP);

        PancakeImpl pancakeB = new PancakeImplWrapper("B", 4000, true, consumeCake); // Task B wait for 4000 ms then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImplWrapper("C", 250, consumeCake);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImplWrapper("D", 250, consumeCake);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImplWrapper("G", 250, consumeCake);
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImplWrapper("E", 250, consumeCake);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImplWrapper("F", 250, consumeCake);
        pancakeF.withInstantPancake(pancakeD);
        pancakeF.withInstantPancake(pancakeE);
        pancakeF.withInstantPancake(pancakeH);

        pancakeA.withDelayedPancake(pancakeJ);
        pancakeA.withDelayedPancake(pancakeK);

        final Set<String> expectedToSee = new HashSet<>();
        expectedToSee.add("M");
        expectedToSee.add("N");
        expectedToSee.add("K");
        expectedToSee.add("Q");
        expectedToSee.add("I");

        expectedToSee.add("J");

        expectedToSee.add("A");

        expectedToSee.add("C");

        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
            // Ensure the thread pool size > 1, to avoid C runs after B throws exception.
            //
            // In test pipeline, sometimes agent has only 1 cpu core, making the default ForkJoinPool having 1 available
            // thread, resulting in task executing sequentially. Since  C is at the
            // same execution level as B, C could execute after B throws exception.
            // This will mark the group as canceled, and C will not execute.
            .withSyncTaskExecutor(Executors.newFixedThreadPool(5))
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };
        try {
            pancakeFtg.invoke(context);
        } catch (Throwable e) {
            consumeError.accept(e);
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
        Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
        RuntimeException cause = (RuntimeException) exceptions.get(0).getCause();
        Assertions.assertTrue(cause.getMessage().equalsIgnoreCase("B"));
    }

    @Test
    public void testTerminateOnHittingLcaTask() {
        // Terminate on error strategy used in this task group is
        // TaskGroupTerminateOnErrorStrategy::TERMINATE_ON_HITTING_LCA_TASK

        // The task B start and asynchronously wait for 4000 ms then emit an error.
        // The tasks Q and I start and asynchronous wait for 8000 ms and then report
        // completion. The more delay in Q and I ensure that B emits error before Q and
        // I finishes.
        //
        // In this setup,
        //    D and E cannot be executed since their dependency B is faulted.
        //    Q, I, P, H, L and G will be executed since they are not directly or indirectly depends on B
        //
        //    Here the LCA task is E, because there is no way we can make a progress beyond that
        //

        /**
         *                                                                        |--------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)----->[N](0)
         *                                 X                     |
         *   |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *   |                             ^                     ^
         *   |                             |                     |
         *  [F](6)---->[E](5)--------------|                     |
         *   |          |                                        |
         *   |          |------->[G](4)-->[C](3)------------------
         *   |                    |
         *   |                    |============================>[L](2)---------->[P](1)=====>[Q](0)
         *   |
         *   |------------------------------------------------------------------>[H](1)----->[I](0)
         */

        final Set<String> seen = new HashSet<>();
        Function<Indexable, IPancake> consumePancake = indexable -> {
            IPancake pancake = (IPancake) indexable;
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onNext: " + pancake.name());
            synchronized (seen) {
                seen.add(pancake.name());
            }
            return pancake;
        };
        PancakeImpl pancakeM = new PancakeImplWrapper("M", 250, consumePancake);
        PancakeImpl pancakeN = new PancakeImplWrapper("N", 250, consumePancake);
        PancakeImpl pancakeK = new PancakeImplWrapper("K", 250, consumePancake);
        PancakeImpl pancakeQ = new PancakeImplWrapper("Q", 8000, consumePancake);
        PancakeImpl pancakeI = new PancakeImplWrapper("I", 8000, consumePancake);

        PancakeImpl pancakeJ = new PancakeImplWrapper("J", 250, consumePancake);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImplWrapper("P", 250, consumePancake);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImplWrapper("H", 250, consumePancake);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImplWrapper("A", 250, consumePancake);
        PancakeImpl pancakeL = new PancakeImplWrapper("L", 250, consumePancake);
        pancakeL.withInstantPancake(pancakeP);

        PancakeImpl pancakeB = new PancakeImplWrapper("B", 4000, true, consumePancake); // Task B wait for 4000 ms then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImplWrapper("C", 250, consumePancake);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImplWrapper("D", 250, consumePancake);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImplWrapper("G", 250, consumePancake);
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImplWrapper("E", 250, consumePancake);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImplWrapper("F", 250, consumePancake);
        pancakeF.withInstantPancake(pancakeD);
        pancakeF.withInstantPancake(pancakeE);
        pancakeF.withInstantPancake(pancakeH);

        pancakeA.withInstantPancake(pancakeJ);
        pancakeA.withInstantPancake(pancakeK);

        final Set<String> expectedToSee = new HashSet<>();
        expectedToSee.add("M");
        expectedToSee.add("N");
        expectedToSee.add("K");
        expectedToSee.add("Q");
        expectedToSee.add("I");

        expectedToSee.add("J");
        expectedToSee.add("P");
        expectedToSee.add("H");

        expectedToSee.add("A");
        expectedToSee.add("L");

        expectedToSee.add("C");

        expectedToSee.add("G");

        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_HITTING_LCA_TASK);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };

        try {
            pancakeFtg.invoke(context);
        } catch (Throwable e) {
            consumeError.accept(e);
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
        Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0).getCause();
        Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("B"));
    }

    @Test
    public void testCompositeError() {
        // Terminate on error strategy used in this task group is
        // TaskGroupTerminateOnErrorStrategy::TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION

        // Tasks marked X (B & G) will fault. B and G are not depends on each other.
        // If B start at time 't0'th ms then G starts ~'t1 = (t0 + 250)'th ms.
        // After B start, it asynchronously wait and emit an error at time '(t0 + 3500)' ms.
        // In this setup, G gets ~3250 ms to start before B emits error. Eventually G also
        // emit error.
        // The final stream, emits result of all tasks that B and G directly or indirectly
        // depends on and terminate with composite exception (that composes exception from
        // B and G).
        /**
         *                                                                        |--------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)----->[N](0)
         *                                 X                     |
         *   |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *   |                             ^                     ^
         *   |                             |                     |
         *  [F](6)---->[E](5)--------------|                     |
         *   |          |         X                              |
         *   |          |------->[G](4)-->[C](3)------------------
         *   |                    |
         *   |                    |============================>[L](2)---------->[P](1)=====>[Q](0)
         *   |
         *   |------------------------------------------------------------------>[H](1)----->[I](0)
         */
        final Set<String> seen = new TreeSet<>();
        Function<Indexable, IPancake> consumeCake = indexable -> {
            IPancake pancake = (IPancake) indexable;
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onNext: " + pancake.name());
            synchronized (seen) {
                seen.add(pancake.name());
            }
            return pancake;
        };
        PancakeImpl pancakeM = new PancakeImplWrapper("M", 250, consumeCake);
        PancakeImpl pancakeN = new PancakeImplWrapper("N", 250, consumeCake);
        PancakeImpl pancakeK = new PancakeImplWrapper("K", 250, consumeCake);
        PancakeImpl pancakeQ = new PancakeImplWrapper("Q", 250, consumeCake);
        PancakeImpl pancakeI = new PancakeImplWrapper("I", 250, consumeCake);

        PancakeImpl pancakeJ = new PancakeImplWrapper("J", 250, consumeCake);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImplWrapper("P", 250, consumeCake);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImplWrapper("H", 250, consumeCake);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImplWrapper("A", 250, consumeCake);
        PancakeImpl pancakeL = new PancakeImplWrapper("L", 250, consumeCake);
        pancakeL.withInstantPancake(pancakeP);

        PancakeImpl pancakeB = new PancakeImplWrapper("B", 3500, true, consumeCake); // Task B wait for 3500 ms then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImplWrapper("C", 250, consumeCake);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImplWrapper("D", 250, consumeCake);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImplWrapper("G", 250, true, consumeCake); // Task G wait for 250 ms then emit error
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImplWrapper("E", 250, consumeCake);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImplWrapper("F", 250, consumeCake);
        pancakeF.withInstantPancake(pancakeD);
        pancakeF.withInstantPancake(pancakeE);
        pancakeF.withInstantPancake(pancakeH);

        pancakeA.withDelayedPancake(pancakeJ);
        pancakeA.withDelayedPancake(pancakeK);

        final Set<String> expectedToSee = new TreeSet<>();
        expectedToSee.add("M");
        expectedToSee.add("N");
        expectedToSee.add("K");
        expectedToSee.add("Q");
        expectedToSee.add("I");

        expectedToSee.add("J");
        expectedToSee.add("P");
        expectedToSee.add("H");

        expectedToSee.add("A");
        expectedToSee.add("L");

        expectedToSee.add("C");

        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };
        try {
            pancakeFtg.invoke(context.withSyncTaskExecutor(
                // Ensure the thread pool size > 1, to avoid G runs after B throws exception.
                //
                // In test pipeline, agent has only 1 cpu core, making the default ForkJoinPool having 1 available
                // thread, resulting in task executing sequentially. Since G takes dependency of C, and C is at the
                // same execution level as B, G will not execute until B throws exception.
                // This will mark the group as canceled, and G will not execute.
                Executors.newFixedThreadPool(5)));
        } catch (Throwable e) {
            consumeError.accept(e);
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));

        // For CompletableFuture.allOf():
        // If any of the given CompletableFutures complete exceptionally, then the returned
        // CompletableFuture also does so, with a CompletionException holding this exception as its cause.
        Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
        Throwable cause = exceptions.get(0).getCause();
        String message = cause.getMessage();
        Assertions.assertTrue("B".equalsIgnoreCase(message) || "G".equalsIgnoreCase(message));
    }

    @Test
    public void testErrorOnRoot() {
        // Terminate on error strategy used in this task group is
        // TaskGroupTerminateOnErrorStrategy::TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION

        // In this setup only the root task F fault. The final stream will emit result from
        // all tasks expect F and terminate with exception from F.
        //
        /**
         *                                                                        |---------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)------>[N](0)
         *                                                       |
         *    |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *    |                             ^                     ^
         *    |                             |                     |
         *  X[F](6)---->[E](5)--------------|                     |
         *    |          |                                        |
         *    |          |------->[G](4)-->[C](3)------------------
         *    |                    |
         *    |                    |============================>[L](2)--------->[P](1)======>[Q](0)
         *    |
         *    |----------------------------------------------------------------->[H](1)------>[I](0)
         */

        final Set<String> seen = new TreeSet<>();
        Function<Indexable, IPancake> consumeCake = indexable -> {
            IPancake pancake = (IPancake) indexable;
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onNext: " + pancake.name());
            synchronized (seen) {
                seen.add(pancake.name());
            }
            return pancake;
        };
        PancakeImpl pancakeM = new PancakeImplWrapper("M", 250, consumeCake);
        PancakeImpl pancakeN = new PancakeImplWrapper("N", 250, consumeCake);
        PancakeImpl pancakeK = new PancakeImplWrapper("K", 250, consumeCake);
        PancakeImpl pancakeQ = new PancakeImplWrapper("Q", 250, consumeCake);
        PancakeImpl pancakeI = new PancakeImplWrapper("I", 250, consumeCake);

        PancakeImpl pancakeJ = new PancakeImplWrapper("J", 250, consumeCake);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImplWrapper("P", 250, consumeCake);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImplWrapper("H", 250, consumeCake);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImplWrapper("A", 250, consumeCake);
        PancakeImpl pancakeL = new PancakeImplWrapper("L", 250, consumeCake);
        pancakeL.withInstantPancake(pancakeP);

        PancakeImpl pancakeB = new PancakeImplWrapper("B", 250, consumeCake);
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImplWrapper("C", 250, consumeCake);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImplWrapper("D", 250, consumeCake);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImplWrapper("G", 250, consumeCake);
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImplWrapper("E", 250, consumeCake);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImplWrapper("F", 250, true, consumeCake); // Emit error on root
        pancakeF.withInstantPancake(pancakeD);
        pancakeF.withInstantPancake(pancakeE);
        pancakeF.withInstantPancake(pancakeH);

        pancakeA.withDelayedPancake(pancakeJ);
        pancakeA.withDelayedPancake(pancakeK);

        final Set<String> expectedToSee = new HashSet<>();
        expectedToSee.add("M");
        expectedToSee.add("N");
        expectedToSee.add("K");
        expectedToSee.add("Q");
        expectedToSee.add("I");

        expectedToSee.add("J");
        expectedToSee.add("P");
        expectedToSee.add("H");

        expectedToSee.add("A");
        expectedToSee.add("L");

        expectedToSee.add("B");
        expectedToSee.add("C");

        expectedToSee.add("D");
        expectedToSee.add("G");

        expectedToSee.add("E");
        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };
        try {
            pancakeFtg.invoke(context);
        } catch (Throwable e) {
            consumeError.accept(e);
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
        Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0).getCause();
        Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("F"));
    }

    /*
     * A wrapper around PancakeImpl for sync-stack tests to record which PancakeImpls have been created.
     */
    private static class PancakeImplWrapper extends PancakeImpl {
        private Function<Indexable, IPancake> postSyncInvoke;

        PancakeImplWrapper(String name, long eventDelayInMilliseconds, Function<Indexable, IPancake> postSyncInvoke) {
            super(name, eventDelayInMilliseconds);
            this.postSyncInvoke = postSyncInvoke;
        }

        PancakeImplWrapper(String name, long eventDelayInMilliseconds, boolean fault,
            Function<Indexable, IPancake> postSyncInvoke) {
            super(name, eventDelayInMilliseconds, fault);
            this.postSyncInvoke = postSyncInvoke;
        }

        @Override
        public IPancake create() {
            IPancake cake = super.create();
            if (this.postSyncInvoke != null) {
                return this.postSyncInvoke.apply(cake);
            }
            return cake;
        }
    }

    // Temporary implementation when sync interfaces are not supported in CreateUpdateTask.
    private static class PancakeImpl implements TaskGroup.HasTaskGroup, IPancake, TaskItem {
        private final String name;
        final List<Creatable<IPancake>> delayedPancakes;
        final long eventDelayInMilliseconds;
        final Throwable errorToThrow;
        private final TaskGroup taskGroup;
        private IPancake result;

        PancakeImpl(String name, long eventDelayInMilliseconds) {
            this(name, eventDelayInMilliseconds, false);
        }

        PancakeImpl(String name, long eventDelayInMilliseconds, boolean fault) {
            this.name = name;
            this.taskGroup = new TaskGroup(this.name, this);
            this.eventDelayInMilliseconds = eventDelayInMilliseconds;
            if (fault) {
                this.errorToThrow = new RuntimeException(name);
            } else {
                this.errorToThrow = null;
            }
            delayedPancakes = new ArrayList<>();
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public TaskGroup taskGroup() {
            return this.taskGroup;
        }

        @Override
        public Indexable invoke(TaskGroup.InvocationContext context) {
            return this.create();
        }

        @Override
        public IPancake create() {
            if (this.errorToThrow == null) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Pancake(" + this.name() + ")::createResource() 'onNext()'");
                try {
                    Thread.sleep(this.eventDelayInMilliseconds);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.result = this;
            } else {
                LOGGER.log(LogLevel.VERBOSE, () -> "Pancake(" + this.name() + ")::createResource() 'onError()'");
                try {
                    Thread.sleep(this.eventDelayInMilliseconds);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                throw (RuntimeException) errorToThrow;
            }
            return this;
        }

        @Override
        public String key() {
            return this.name;
        }

        @Override
        public Indexable result() {
            return result;
        }

        @Override
        public void beforeGroupInvoke() {
            for (Creatable<IPancake> pancake : this.delayedPancakes) {
                this.taskGroup.addDependency((TaskGroup.HasTaskGroup) pancake);
            }
        }

        @Override
        public IPancake withInstantPancake(Creatable<IPancake> anotherPancake) {
            this.taskGroup.addDependency((TaskGroup.HasTaskGroup) anotherPancake);
            return this;
        }

        @Override
        public IPancake withDelayedPancake(Creatable<IPancake> anotherPancake) {
            this.delayedPancakes.add(anotherPancake);
            return this;
        }

        // Below are not used in tests
        ////////////////////////////////

        @Override
        public Mono<IPancake> createAsync() {
            throw new UnsupportedOperationException(
                "method [createAsync] not implemented in class [com.azure.resourcemanager.resources.fluentcore.dag.SyncDAGErrorTests.PancakeImpl]");
        }

        @Override
        public IPancake create(Context context) {
            throw new UnsupportedOperationException(
                "method [create] not implemented in class [com.azure.resourcemanager.resources.fluentcore.dag.SyncDAGErrorTests.PancakeImpl]");
        }

        @Override
        public Mono<IPancake> createAsync(Context context) {
            throw new UnsupportedOperationException(
                "method [createAsync] not implemented in class [com.azure.resourcemanager.resources.fluentcore.dag.SyncDAGErrorTests.PancakeImpl]");
        }

        @Override
        public boolean isHot() {
            return false;
        }

        @Override
        public Mono<Indexable> invokeAsync(TaskGroup.InvocationContext context) {
            throw new UnsupportedOperationException(
                "method [invokeAsync] not implemented in class [com.azure.resourcemanager.resources.fluentcore.dag.SyncDAGErrorTests.PancakeImpl]");
        }

        @Override
        public Mono<Void> invokeAfterPostRunAsync(boolean isGroupFaulted) {
            throw new UnsupportedOperationException(
                "method [invokeAfterPostRunAsync] not implemented in class [com.azure.resourcemanager.resources.fluentcore.dag.SyncDAGErrorTests.PancakeImpl]");
        }
    }
}
