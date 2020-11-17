// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import java.time.Instant;

public abstract class ChangeFeedStartFromInternal {
    ChangeFeedStartFromInternal() {
    }

    abstract void accept(ChangeFeedStartFromVisitor visitor);

    public static ChangeFeedStartFromInternal createFromBeginning() {
        return InstanceHolder.FROM_BEGINNING_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromNow() {
        return InstanceHolder.FROM_NOW_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromPointInTime(Instant pointInTime) {
        return new ChangeFeedStartFromPointInTimeImpl(pointInTime);
    }

    public static ChangeFeedStartFromInternal createFromContinuation(String continuation) {
        return new ChangeFeedStartFromContinuationImpl(continuation);
    }

    private static final class InstanceHolder {
        static final ChangeFeedStartFromBeginningImpl FROM_BEGINNING_SINGLETON =
            new ChangeFeedStartFromBeginningImpl();

        static final ChangeFeedStartFromNowImpl FROM_NOW_SINGLETON =
            new ChangeFeedStartFromNowImpl();
    }
}