// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.RequestTimeoutException;

import java.time.Duration;
import java.time.Instant;

public class TimeoutHelper {
    private final Instant startTime;
    private final Duration timeOut;

    public TimeoutHelper(Duration timeOut) {
        this.startTime = Instant.now();
        this.timeOut = timeOut;
    }

    public boolean isElapsed() {
        Duration elapsed = Duration.ofMillis(Instant.now().toEpochMilli() - startTime.toEpochMilli());
        return elapsed.compareTo(this.timeOut) >= 0;
    }

    public Duration getRemainingTime() {
        Duration elapsed = Duration.ofMillis(Instant.now().toEpochMilli() - startTime.toEpochMilli());
        return this.timeOut.minus(elapsed);
    }

    public void throwTimeoutIfElapsed() throws RequestTimeoutException {
        if (this.isElapsed()) {
            throw new RequestTimeoutException();
        }
    }

    public void throwGoneIfElapsed() throws GoneException {
        if (this.isElapsed()) {
            throw new GoneException();
        }
    }
}
