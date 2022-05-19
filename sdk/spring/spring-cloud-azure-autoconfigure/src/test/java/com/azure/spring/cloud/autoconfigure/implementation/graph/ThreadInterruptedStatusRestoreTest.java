// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadInterruptedStatusRestoreTest {

    @Test
    void testThreadInterruptedRestoreFromAnotherThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread testThread = new TestThread(latch, true);
        testThread.setDaemon(true);
        testThread.start();
        TimeUnit.SECONDS.sleep(2L);
        testThread.interrupt();
        latch.await(5, TimeUnit.SECONDS);
        System.out.println(Thread.currentThread().getName() + ": begin assertion");
        Assertions.assertTrue(testThread.isInterrupted());
        System.out.println(Thread.currentThread().getName() + ": end assertion");
    }

    @Test
    void testThreadInterruptedNotRestoreFromAnotherThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread testThread = new TestThread(latch, false);
        testThread.setDaemon(true);
        testThread.start();
        TimeUnit.SECONDS.sleep(2L);
        testThread.interrupt();
        latch.await(5, TimeUnit.SECONDS);
        System.out.println(Thread.currentThread().getName() + ": begin assertion");
        Assertions.assertFalse(testThread.isInterrupted());
        System.out.println(Thread.currentThread().getName() + ": end assertion");
    }

    static class TestThread extends Thread {
        private final CountDownLatch latch;

        private final boolean restore;

        TestThread(CountDownLatch latch, boolean restore) {
            this.latch = latch;
            this.restore = restore;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ex) {
                    System.out.println(Thread.currentThread().getName() + ": current thread was interrupted!");
                    if (restore) {
                        Thread.currentThread().interrupt();
                    }

                    latch.countDown();
                    someMethodConsumingTime();
                }
            }
        }

        private void someMethodConsumingTime() {
            long sum = 0;
            for (int i = 0; i < 10000; i++) {
                if (i % 2 != 0) {
                    sum++;
                }
            }

            System.out.println(Thread.currentThread().getName() + ": sum = " + sum);
        }
    }
}
