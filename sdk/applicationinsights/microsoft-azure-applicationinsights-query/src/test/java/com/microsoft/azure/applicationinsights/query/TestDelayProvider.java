// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.microsoft.azure.arm.utils.DelayProvider;
import rx.Observable;

/**
 * From:
 * https://github.com/Azure/autorest-clientruntime-for-java/blob/master/azure-arm-client-runtime/src/test/java/com/microsoft/azure/arm/core/TestDelayProvider.java
 */
class TestDelayProvider extends DelayProvider {
    private final boolean isLiveMode;

    TestDelayProvider(boolean isLiveMode) {
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
