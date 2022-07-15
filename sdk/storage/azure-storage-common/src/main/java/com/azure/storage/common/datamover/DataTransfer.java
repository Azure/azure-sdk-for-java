package com.azure.storage.common.datamover;

import java.util.concurrent.CountDownLatch;

/**
 * Represents long-running transfer.
 */
public class DataTransfer {

    final CountDownLatch latch = new CountDownLatch(1);

    /**
     * Awaits for tranfer completion.
     */
    public void awaitCompletion() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
