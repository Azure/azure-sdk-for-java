package io.clientcore.core.observability;

public interface Scope extends AutoCloseable {
    @Override
    default void close() {
    }
}
