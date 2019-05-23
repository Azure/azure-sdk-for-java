package com.azure.core.polling;

import java.io.Serializable;
import java.util.function.Supplier;
/**
 * Serializable version of {@link Supplier}
 */
@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, Serializable {
}
