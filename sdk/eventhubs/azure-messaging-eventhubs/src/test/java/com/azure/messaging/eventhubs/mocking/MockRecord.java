// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import org.apache.qpid.proton.engine.Record;

/**
 * Mock implementation of the Record interface.
 */
public class MockRecord implements Record {
    @Override
    public <T> T get(Object key, Class<T> klass) {
        return null;
    }

    @Override
    public <T> void set(Object key, Class<T> klass, T value) {

    }

    @Override
    public void clear() {

    }
}
