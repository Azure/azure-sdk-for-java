package com.azure.storage.datamover;

import java.util.concurrent.CountDownLatch;

public class DataTransfer {

    final CountDownLatch latch = new CountDownLatch(1);

    public void awaitCompletion() throws InterruptedException {
        latch.await();
    }
}
