// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples.utils;

import java.util.concurrent.CountDownLatch;

public class ApplicationManager {
    private final CountDownLatch mLatch = new CountDownLatch(1);

    public void waitingForApplicationStop() {
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopApplication() {
        mLatch.countDown();
    }
}
