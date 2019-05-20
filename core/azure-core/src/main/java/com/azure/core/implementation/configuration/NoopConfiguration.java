package com.azure.core.implementation.configuration;

import java.util.function.Function;

/*
 * Noop Configuration used to opt out of using global configurations when constructing client libraries.
 */
class NoopConfiguration extends Configuration {
    /**
     * Creates a no-op configuration.
     */
    NoopConfiguration() {
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public <T> T get(String name, T defaultValue) {
        return null;
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
