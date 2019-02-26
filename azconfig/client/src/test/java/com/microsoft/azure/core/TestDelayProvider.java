// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.core;

import com.microsoft.azure.utils.DelayProvider;
import reactor.core.publisher.Flux;

public class TestDelayProvider extends DelayProvider {
    private boolean isLiveMode;
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
    public <T> Flux<T> delayedEmitAsync(T event, int milliseconds) {
        if (isLiveMode) {
            return super.delayedEmitAsync(event, milliseconds);
        } else {
            return Flux.just(event);
        }
    }

}
