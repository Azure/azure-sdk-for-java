// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DAGErrorTests {
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
        PancakeImpl pancakeM = new PancakeImpl("M", 250);
        PancakeImpl pancakeN = new PancakeImpl("N", 250);
        PancakeImpl pancakeK = new PancakeImpl("K", 250);
        PancakeImpl pancakeQ = new PancakeImpl("Q", 8000);
        PancakeImpl pancakeI = new PancakeImpl("I", 8000);

        PancakeImpl pancakeJ = new PancakeImpl("J", 250);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImpl("P", 250);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImpl("H", 250);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImpl("A", 250);
        PancakeImpl pancakeL = new PancakeImpl("L", 250);
        pancakeL.withInstantPancake(pancakeP);


        PancakeImpl pancakeB = new PancakeImpl("B", 4000, true); // Task B wait for 4000 ms then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImpl("C", 250);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImpl("D", 250);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImpl("G", 250);
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImpl("E", 250);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImpl("F", 250);
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

        final Set<String> seen = new HashSet<>();
        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
                .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);
        IPancake rootPancake = pancakeFtg.invokeAsync(context).map(indexable -> {
            IPancake pancake = (IPancake) indexable;
            System.out.println("map.onNext: " + pancake.name());
            seen.add(pancake.name());
            return pancake;
        })
                .onErrorResume(throwable -> {
                    System.out.println("map.onErrorResumeNext: " + throwable);
                    exceptions.add(throwable);
                    return Mono.empty();
                }).blockLast();

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertTrue(exceptions.get(0) instanceof RuntimeException);
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0);
        Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("B"));
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
        PastaImpl pastaM = new PastaImpl("M", 250);
        PastaImpl pastaN = new PastaImpl("N", 250);
        PastaImpl pastaK = new PastaImpl("K", 250);
        PastaImpl pastaQ = new PastaImpl("Q", 8000);
        PastaImpl pastaI = new PastaImpl("I", 8000);

        PastaImpl pastaJ = new PastaImpl("J", 250);
        pastaJ.withInstantPasta(pastaM);
        pastaJ.withInstantPasta(pastaN);
        PastaImpl pastaP = new PastaImpl("P", 250);
        pastaP.withDelayedPasta(pastaQ);
        PastaImpl pastaH = new PastaImpl("H", 250);
        pastaH.withInstantPasta(pastaI);

        PastaImpl pastaA = new PastaImpl("A", 250);
        PastaImpl pastaL = new PastaImpl("L", 250);
        pastaL.withInstantPasta(pastaP);


        PastaImpl pastaB = new PastaImpl("B", 4000, true); // Task B wait for 4000 ms then emit error
        pastaB.withInstantPasta(pastaA);
        PastaImpl pastaC = new PastaImpl("C", 250);
        pastaC.withInstantPasta(pastaA);

        PastaImpl pastaD = new PastaImpl("D", 250);
        pastaD.withInstantPasta(pastaB);
        PastaImpl pastaG = new PastaImpl("G", 250);
        pastaG.withInstantPasta(pastaC);
        pastaG.withDelayedPasta(pastaL);

        PastaImpl pastaE = new PastaImpl("E", 250);
        pastaE.withInstantPasta(pastaB);
        pastaE.withInstantPasta(pastaG);

        PastaImpl pastaF = new PastaImpl("F", 250);
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

        final Set<String> seen = new HashSet<>();
        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pastaFtg = pastaF.taskGroup();
        TaskGroup.InvocationContext context = pastaFtg.newInvocationContext()
                .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_HITTING_LCA_TASK);

        IPasta rootPasta = pastaFtg.invokeAsync(context).map(indexable -> {
            IPasta pasta = (IPasta) indexable;
            System.out.println("map.onNext: " + pasta.name());
            seen.add(pasta.name());
            return pasta;
        })
                .onErrorResume(throwable -> {
                    System.out.println("map.onErrorResumeNext: " + throwable);
                    exceptions.add(throwable);
                    return Mono.empty();
                }).blockLast();

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertTrue(exceptions.get(0) instanceof RuntimeException);
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0);
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

        PancakeImpl pancakeM = new PancakeImpl("M", 250);
        PancakeImpl pancakeN = new PancakeImpl("N", 250);
        PancakeImpl pancakeK = new PancakeImpl("K", 250);
        PancakeImpl pancakeQ = new PancakeImpl("Q", 250);
        PancakeImpl pancakeI = new PancakeImpl("I", 250);

        PancakeImpl pancakeJ = new PancakeImpl("J", 250);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImpl("P", 250);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImpl("H", 250);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImpl("A", 250);
        PancakeImpl pancakeL = new PancakeImpl("L", 250);
        pancakeL.withInstantPancake(pancakeP);


        PancakeImpl pancakeB = new PancakeImpl("B", 3500, true); // Task B wait for 3500 ms then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImpl("C", 250);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImpl("D", 250);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImpl("G", 250, true); // Task G wait for 250 ms then emit error
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImpl("E", 250);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImpl("F", 250);
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

        final Set<String> seen = new TreeSet<>();
        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
                .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);

        IPancake rootPancake = pancakeFtg.invokeAsync(context).map(indexable -> {
            IPancake pancake = (IPancake) indexable;
            String name = pancake.name();
            System.out.println("map.onNext:" + name);
            seen.add(name);
            return pancake;
        }).onErrorResume(throwable -> {
            System.out.println("map.onErrorResumeNext:" + throwable);
            exceptions.add(throwable);
            return Mono.empty();
        }).blockLast();

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertTrue(exceptions.get(0) instanceof RuntimeException);
        RuntimeException compositeException = (RuntimeException) exceptions.get(0);
        Assertions.assertEquals(compositeException.getSuppressed().length, 2);
        for (Throwable throwable : compositeException.getSuppressed()) {
            String message = throwable.getMessage();
            Assertions.assertTrue(message.equalsIgnoreCase("B") || message.equalsIgnoreCase("G"));
        }
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

        PancakeImpl pancakeM = new PancakeImpl("M", 250);
        PancakeImpl pancakeN = new PancakeImpl("N", 250);
        PancakeImpl pancakeK = new PancakeImpl("K", 250);
        PancakeImpl pancakeQ = new PancakeImpl("Q", 250);
        PancakeImpl pancakeI = new PancakeImpl("I", 250);

        PancakeImpl pancakeJ = new PancakeImpl("J", 250);
        pancakeJ.withInstantPancake(pancakeM);
        pancakeJ.withInstantPancake(pancakeN);
        PancakeImpl pancakeP = new PancakeImpl("P", 250);
        pancakeP.withDelayedPancake(pancakeQ);
        PancakeImpl pancakeH = new PancakeImpl("H", 250);
        pancakeH.withInstantPancake(pancakeI);

        PancakeImpl pancakeA = new PancakeImpl("A", 250);
        PancakeImpl pancakeL = new PancakeImpl("L", 250);
        pancakeL.withInstantPancake(pancakeP);


        PancakeImpl pancakeB = new PancakeImpl("B", 250);
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImpl("C", 250);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImpl("D", 250);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImpl("G", 250);
        pancakeG.withInstantPancake(pancakeC);
        pancakeG.withDelayedPancake(pancakeL);

        PancakeImpl pancakeE = new PancakeImpl("E", 250);
        pancakeE.withInstantPancake(pancakeB);
        pancakeE.withInstantPancake(pancakeG);

        PancakeImpl pancakeF = new PancakeImpl("F", 250, true); // Emit error on root
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
        final Set<String> seen = new HashSet<>();
        final List<Throwable> exceptions = new ArrayList<>();

        TaskGroup pancakeFtg = pancakeF.taskGroup();
        TaskGroup.InvocationContext context = pancakeFtg.newInvocationContext()
                .withTerminateOnErrorStrategy(TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION);
        IPancake rootPancake = pancakeFtg.invokeAsync(context).map(indexable -> {
            IPancake pancake = (IPancake) indexable;
            seen.add(pancake.name());
            System.out.println("map.onNext:" + pancake.name());
            return pancake;
        }).onErrorResume(throwable -> {
            System.out.println("map.onErrorResumeNext:" + throwable);
            exceptions.add(throwable);
            return Mono.empty();
        }).blockLast();

        expectedToSee.removeAll(seen);
        Assertions.assertTrue(expectedToSee.isEmpty());
        Assertions.assertEquals(exceptions.size(), 1);
        Assertions.assertTrue(exceptions.get(0) instanceof RuntimeException);
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0);
        Assertions.assertTrue(runtimeException.getMessage().equalsIgnoreCase("F"));
    }
}
