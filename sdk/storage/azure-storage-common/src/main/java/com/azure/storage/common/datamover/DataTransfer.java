// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover;

import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.CountDownLatch;

/**
 * Represents long-running transfer.
 */
public class DataTransfer {
    private static final ClientLogger LOGGER = new ClientLogger(DataTransfer.class);
    final CountDownLatch latch = new CountDownLatch(1);

    /**
     * Awaits for tranfer completion.
     * @throws RuntimeException Sometimes.
     */
    public void awaitCompletion() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
