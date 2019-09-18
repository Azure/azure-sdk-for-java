// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util;

import java.util.function.Function;

/*
 * Noop Configuration used to opt out of using global configurations when constructing client libraries.
 */
class NoopConfiguration extends Configuration {
    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public <T> T get(String name, T defaultValue) {
        return defaultValue;
    }

    @Override
    public <T> T get(String name, Function<String, T> converter) {
        return null;
    }

    @Override
    public NoopConfiguration put(String name, String value) {
        return this;
    }

    @Override
    public String remove(String name) {
        return null;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public NoopConfiguration clone() {
        return new NoopConfiguration();
    }
}
