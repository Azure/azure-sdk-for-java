/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.google.common.collect.Sets;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DAGErrorTests {
    @Test
    public void testCompositeErrorTaskCancellation() {
        // Define pancakes
        //
        /**
         *                                                                        |--------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)----->[N](0)
         *                                                       |
         *   |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *   |                             ^                     ^
         *   |                             |                     |
         *  [F](6)---->[E](5)--------------|                     |
         *   |          |                                        |
         *   |          |------->[G](4)-->[C](3)------------------
         *   |                    |
         *   |                    |=============================>[L](2)------->[P](1)=======>[Q](0)
         *   |
         *   |---------------------------------------------------------------->[H](1)------->[I](0)
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


        PancakeImpl pancakeB = new PancakeImpl("B", 3000, true); // Task B wait for 3000 secs then emit error
        pancakeB.withInstantPancake(pancakeA);
        PancakeImpl pancakeC = new PancakeImpl("C", 250);
        pancakeC.withInstantPancake(pancakeA);

        PancakeImpl pancakeD = new PancakeImpl("D", 250);
        pancakeD.withInstantPancake(pancakeB);
        PancakeImpl pancakeG = new PancakeImpl("G", 1500, true); // Task G wait for 1500 secs then emit error
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
        expectedToSee.add("P");
        expectedToSee.add("H");

        expectedToSee.add("A");
        expectedToSee.add("L");

        expectedToSee.add("C");

        final Set<String> seen = new HashSet<>();
        final List<Throwable> exceptions = new ArrayList<>();
        IPancake rootPancake = pancakeF.createAsync().map(new Func1<Indexable, IPancake>() {
            @Override
            public IPancake call(Indexable indexable) {
                IPancake pancake = (IPancake) indexable;
                System.out.println("map.onNext:" + pancake.name());
                seen.add(pancake.name());
                return pancake;
            }
        })
        .onErrorResumeNext(new Func1<Throwable, Observable<IPancake>>() {
            @Override
            public Observable<IPancake> call(Throwable throwable) {
                System.out.println("map.onErrorResumeNext:" + throwable);
                exceptions.add(throwable);
                return Observable.empty();
            }
        }).toBlocking().last();

        Assert.assertTrue(Sets.difference(expectedToSee, seen).isEmpty());
        Assert.assertEquals(exceptions.size(), 1);
        Assert.assertTrue(exceptions.get(0) instanceof CompositeException);
        CompositeException compositeException = (CompositeException) exceptions.get(0);
        Assert.assertEquals(compositeException.getExceptions().size(), 2);
        for (Throwable throwable : compositeException.getExceptions()) {
            String message = throwable.getMessage();
            Assert.assertTrue(message.equalsIgnoreCase("B") || message.equalsIgnoreCase("G"));
        }
    }

    @Test
    public void testErrorOnRoot() {
        // Define pancakes
        //
        /**
         *                                                                        |--------->[M](0)
         *                                                                        |
         *                                                       |==============>[J](1)----->[N](0)
         *                                                       |
         *   |------------------>[D](4)-->[B](3)--------------->[A](2)======================>[K](0)
         *   |                             ^                     ^
         *   |                             |                     |
         *  [F](6)---->[E](5)--------------|                     |
         *   |          |                                        |
         *   |          |------->[G](4)-->[C](3)------------------
         *   |                    |
         *   |                    |=============================>[L](2)------->[P](1)=======>[Q](0)
         *   |
         *   |---------------------------------------------------------------->[H](1)------->[I](0)
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
        IPancake rootPancake = pancakeF.createAsync().map(new Func1<Indexable, IPancake>() {
            @Override
            public IPancake call(Indexable indexable) {
                IPancake pancake = (IPancake) indexable;
                seen.add(pancake.name());
                System.out.println("map.onNext:" + pancake.name());
                return pancake;
            }
        })
        .onErrorResumeNext(new Func1<Throwable, Observable<IPancake>>() {
            @Override
            public Observable<IPancake> call(Throwable throwable) {
                System.out.println("map.onErrorResumeNext:" + throwable);
                exceptions.add(throwable);
                return Observable.empty();
            }
        }).toBlocking().last();

        Assert.assertTrue(Sets.difference(expectedToSee, seen).isEmpty());
        Assert.assertEquals(exceptions.size(), 1);
        Assert.assertTrue(exceptions.get(0) instanceof RuntimeException);
        RuntimeException runtimeException = (RuntimeException) exceptions.get(0);
        Assert.assertTrue(runtimeException.getMessage().equalsIgnoreCase("F"));
    }
}
