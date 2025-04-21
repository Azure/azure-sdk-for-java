// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class DAGErrorTests {
    private static final ClientLogger LOGGER = new ClientLogger(DAGErrorTests.class);

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTerminateOnInProgressTaskCompletion(boolean syncStack) {
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
            seen.add(pancake.name());
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
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };
        if (syncStack) {
            try {
                pancakeFtg.invoke(context);
            } catch (Throwable e) {
                consumeError.accept(e);
            }
        } else {
            IPancake rootPancake = pancakeFtg.invokeAsync(context).map(consumeCake).onErrorResume(throwable -> {
                consumeError.accept(throwable);
                return Mono.empty();
            }).blockLast();
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        if (syncStack) {
            // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
            Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
            RuntimeException cause = (RuntimeException) exceptions.get(0).getCause();
            Assertions.assertTrue(cause.getMessage().equalsIgnoreCase("B"));
        } else {
            RuntimeException runtimeException = (RuntimeException) exceptions.get(0);
            Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("B"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTerminateOnHittingLcaTask(boolean syncStack) {
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
        Function<Indexable, IPasta> consumePasta = indexable -> {
            IPasta pasta = (IPasta) indexable;
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onNext: " + pasta.name());
            seen.add(pasta.name());
            return pasta;
        };
        PastaImpl pastaM = new PastaImplWrapper("M", 250, consumePasta);
        PastaImpl pastaN = new PastaImplWrapper("N", 250, consumePasta);
        PastaImpl pastaK = new PastaImplWrapper("K", 250, consumePasta);
        PastaImpl pastaQ = new PastaImplWrapper("Q", 8000, consumePasta);
        PastaImpl pastaI = new PastaImplWrapper("I", 8000, consumePasta);

        PastaImpl pastaJ = new PastaImplWrapper("J", 250, consumePasta);
        pastaJ.withInstantPasta(pastaM);
        pastaJ.withInstantPasta(pastaN);
        PastaImpl pastaP = new PastaImplWrapper("P", 250, consumePasta);
        pastaP.withDelayedPasta(pastaQ);
        PastaImpl pastaH = new PastaImplWrapper("H", 250, consumePasta);
        pastaH.withInstantPasta(pastaI);

        PastaImpl pastaA = new PastaImplWrapper("A", 250, consumePasta);
        PastaImpl pastaL = new PastaImplWrapper("L", 250, consumePasta);
        pastaL.withInstantPasta(pastaP);

        PastaImpl pastaB = new PastaImplWrapper("B", 4000, true, consumePasta); // Task B wait for 4000 ms then emit error
        pastaB.withInstantPasta(pastaA);
        PastaImpl pastaC = new PastaImplWrapper("C", 250, consumePasta);
        pastaC.withInstantPasta(pastaA);

        PastaImpl pastaD = new PastaImplWrapper("D", 250, consumePasta);
        pastaD.withInstantPasta(pastaB);
        PastaImpl pastaG = new PastaImplWrapper("G", 250, consumePasta);
        pastaG.withInstantPasta(pastaC);
        pastaG.withDelayedPasta(pastaL);

        PastaImpl pastaE = new PastaImplWrapper("E", 250, consumePasta);
        pastaE.withInstantPasta(pastaB);
        pastaE.withInstantPasta(pastaG);

        PastaImpl pastaF = new PastaImplWrapper("F", 250, consumePasta);
        pastaF.withInstantPasta(pastaD);
        pastaF.withInstantPasta(pastaE);
        pastaF.withInstantPasta(pastaH);

        pastaA.withDelayedPasta(pastaJ);
        pastaA.withDelayedPasta(pastaK);

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

        TaskGroup pastaFtg = pastaF.taskGroup();
        TaskGroup.InvocationContext context = pastaFtg.newInvocationContext()
            .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_HITTING_LCA_TASK);

        Consumer<Throwable> consumeError = throwable -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "map.onErrorResumeNext: ", throwable);
            exceptions.add(throwable);
        };

        if (syncStack) {
            try {
                pastaFtg.invoke(context);
            } catch (Throwable e) {
                consumeError.accept(e);
            }
        } else {
            IPasta rootPasta = pastaFtg.invokeAsync(context).map(consumePasta).onErrorResume(throwable -> {
                consumeError.accept(throwable);
                return Mono.empty();
            }).blockLast();
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        RuntimeException runtimeException;
        if (syncStack) {
            // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
            Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
            runtimeException = (RuntimeException) exceptions.get(0).getCause();
        } else {
            runtimeException = (RuntimeException) exceptions.get(0);
        }
        Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("B"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testCompositeError(boolean syncStack) {
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
            seen.add(pancake.name());
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
        if (syncStack) {
            try {
                pancakeFtg.invoke(context);
            } catch (Throwable e) {
                consumeError.accept(e);
            }
        } else {
            IPancake rootPancake = pancakeFtg.invokeAsync(context).map(consumeCake).onErrorResume(throwable -> {
                consumeError.accept(throwable);
                return Mono.empty();
            }).blockLast();
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        if (syncStack) {
            // For CompletableFuture.allOf():
            // If any of the given CompletableFutures complete exceptionally, then the returned
            // CompletableFuture also does so, with a CompletionException holding this exception as its cause.
            Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
            Throwable cause = exceptions.get(0).getCause();
            String message = cause.getMessage();
            Assertions.assertTrue("B".equalsIgnoreCase(message) || "G".equalsIgnoreCase(message));
        } else {
            // Flux.mergeDelayError will merge exceptions into one CompositeException with all the exceptions
            // as its suppressed.
            RuntimeException compositeException = (RuntimeException) exceptions.get(0);
            Assertions.assertEquals(compositeException.getSuppressed().length, 2);
            for (Throwable throwable : compositeException.getSuppressed()) {
                String message = throwable.getMessage();
                Assertions.assertTrue("B".equalsIgnoreCase(message) || "G".equalsIgnoreCase(message));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testErrorOnRoot(boolean syncStack) {
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
            seen.add(pancake.name());
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
        if (syncStack) {
            try {
                pancakeFtg.invoke(context);
            } catch (Throwable e) {
                consumeError.accept(e);
            }
        } else {
            IPancake rootPancake = pancakeFtg.invokeAsync(context).map(consumeCake).onErrorResume(throwable -> {
                consumeError.accept(throwable);
                return Mono.empty();
            }).blockLast();
        }

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertInstanceOf(RuntimeException.class, exceptions.get(0));
        RuntimeException runtimeException;
        if (syncStack) {
            // CompletableFuture will wrap exception into CompletionException and the actual exception as its cause.
            Assertions.assertInstanceOf(CompletionException.class, exceptions.get(0));
            runtimeException = (RuntimeException) exceptions.get(0).getCause();
        } else {
            runtimeException = (RuntimeException) exceptions.get(0);
        }
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
        public IPancake createResource() {
            IPancake cake = super.createResource();
            if (this.postSyncInvoke != null) {
                return this.postSyncInvoke.apply(cake);
            }
            return cake;
        }
    }

    /*
     * A wrapper around PastaImpl for sync-stack tests to record which PastaImpls have been created.
     */
    private static class PastaImplWrapper extends PastaImpl {
        private Function<Indexable, IPasta> postSyncInvoke;

        PastaImplWrapper(String name, long eventDelayInMilliseconds, Function<Indexable, IPasta> postSyncInvoke) {
            super(name, eventDelayInMilliseconds);
            this.postSyncInvoke = postSyncInvoke;
        }

        PastaImplWrapper(String name, long eventDelayInMilliseconds, boolean fault,
            Function<Indexable, IPasta> postSyncInvoke) {
            super(name, eventDelayInMilliseconds, fault);
            this.postSyncInvoke = postSyncInvoke;
        }

        @Override
        public IPasta createResource() {
            IPasta pasta = super.createResource();
            if (this.postSyncInvoke != null) {
                return this.postSyncInvoke.apply(pasta);
            }
            return pasta;
        }
    }
}
