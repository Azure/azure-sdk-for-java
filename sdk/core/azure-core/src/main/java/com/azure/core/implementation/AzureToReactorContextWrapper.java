// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Context;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * This class wraps a Azure-Core context in the reactor Context API, to avoid unnecessary copying
 */
public final class AzureToReactorContextWrapper implements reactor.util.context.Context {
    private Context azureContext;

    public AzureToReactorContextWrapper(final Context azureContext) {
        this.azureContext = azureContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) azureContext.getData(key).orElseThrow(() -> new NoSuchElementException());
    }

    @Override
    public boolean hasKey(Object key) {
        return azureContext.getData(key).isPresent();
    }

    @Override
    public reactor.util.context.Context put(Object key, Object value) {
        this.azureContext = azureContext.addData(key, value);
        return this;
    }

    @Override
    public reactor.util.context.Context delete(Object key) {
        throw new UnsupportedOperationException("Deleting from this Reactor Context is not supported");
    }

    @Override
    public int size() {
        return azureContext.size();
    }

    @Override
    public Stream<Map.Entry<Object, Object>> stream() {
        return azureContext.getValues().entrySet().stream();
    }
}
