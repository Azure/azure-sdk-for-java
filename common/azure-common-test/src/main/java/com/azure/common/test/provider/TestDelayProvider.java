// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.provider;

import rx.Observable;

/**
 * A thread sleep wrapper based on test mode.
 */
public class TestDelayProvider extends DelayProvider {
    private boolean isLiveMode;

    /**
     * TestDelayProvider Constructor.
     *
     * @param isLiveMode the boolean field checking if it is live mode.
     */
    public TestDelayProvider(boolean isLiveMode) {
        this.isLiveMode = isLiveMode;
    }

    @Override
    public void sleep(int milliseconds) {
        if (isLiveMode) {
            super.sleep(milliseconds);
        }
    }

    @Override
    public <T> Observable<T> delayedEmitAsync(T event, int milliseconds) {
        if (isLiveMode) {
            return super.delayedEmitAsync(event, milliseconds);
        } else {
            return Observable.just(event);
        }
    }

}
