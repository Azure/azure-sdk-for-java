// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation;

import okhttp3.Call;
import okhttp3.EventListener;

/**
 * Implementation of {@link EventListener.Factory} that creates {@link ResponseTimeoutListener} instances.
 */
public final class ResponseTimeoutListenerFactory implements EventListener.Factory {
    private final EventListener.Factory delegate;

    /**
     * Creates a new instance of ResponseTimeoutListenerFactory.
     * @param delegate The delegate factory to create the listener.
     */
    public ResponseTimeoutListenerFactory(EventListener.Factory delegate) {
        this.delegate = delegate;
    }

    @Override
    public EventListener create(Call call) {
        return new ResponseTimeoutListener(delegate.create(call));
    }
}
