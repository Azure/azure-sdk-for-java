// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is a simple version of PropertyMapper in Spring Boot project.
 */
public final class PropertyMapper {

    private static final String MSG_PREDICATE_CANNOT_BE_NULL = "Predicate must not be null";

    private final boolean alwaysApplyNonNull;

    /**
     * Creates a new instance that applies {@link Source#whenNonNull() whenNonNull} to every source by default.
     */
    public PropertyMapper() {
        this(true);
    }

    /**
     * Create a new instance with flag indicating whether to apply {@link Source#whenNonNull() whenNonNull} to every
     * source.
     * @param alwaysApplyNonNull Whether to apply {@link Source#whenNonNull() whenNonNull} to every source.
     */
    public PropertyMapper(boolean alwaysApplyNonNull) {
        this.alwaysApplyNonNull = alwaysApplyNonNull;
    }

    /**
     * Return a new {@link Source} from the specified value supplier that can be used to
     * perform the mapping.
     * @param <T> the source type
     * @param supplier the value supplier
     * @return a {@link Source} that can be used to complete the mapping
     */
    public <T> Source<T> from(Supplier<T> supplier) {
        Assert.notNull(supplier, "Supplier must not be null");
        Source<T> source = new Source<>(supplier, (t) -> true);
        if (alwaysApplyNonNull) {
            source = source.whenNonNull();
        }
        return source;
    }

    /**
     * Return a new {@link Source} from the specified value that can be used to perform
     * the mapping.
     * @param <T> the source type
     * @param val the value
     * @return a {@link Source} that can be used to complete the mapping
     */
    public <T> Source<T> from(T val) {
        return from(() -> val);
    }

    /**
     * Represents the source in the mapping process.
     *
     * @param <T> type of the actual value
     */
    public static class Source<T> {

        private final Supplier<T> supplier;
        private final Predicate<T> predicate;

        Source(Supplier<T> supplier, Predicate<T> predicate) {
            Assert.notNull(predicate, MSG_PREDICATE_CANNOT_BE_NULL);
            this.supplier = supplier;
            this.predicate = predicate;
        }

        /**
         * Complete the mapping by passing any non-filtered value to the specified consumer.
         * @param consumer the consumer that should accept the value if it's not been filtered.
         */
        public void to(Consumer<T> consumer) {
            T val = this.supplier.get();
            if (this.predicate.test(val)) {
                consumer.accept(val);
            }
        }

        /**
         * Return a filtered version of the source that won't map values that don't match the given predicate.
         * @param predicate the predicate used to filter values.
         * @return a new filtered source instance.
         */
        public Source<T> when(Predicate<T> predicate) {
            Assert.notNull(predicate, MSG_PREDICATE_CANNOT_BE_NULL);
            return new Source<>(this.supplier, this.predicate.and(predicate));
        }

        /**
         * Return a filtered version of the source that won't map values that match the given predicate.
         * @param predicate the predicate used to filter values.
         * @return a new filtered source instance.
         */
        public Source<T> whenNot(Predicate<T> predicate) {
            Assert.notNull(predicate, MSG_PREDICATE_CANNOT_BE_NULL);
            return when(predicate.negate());
        }

        /**
         * Return a filtered version of the source that won't map non-null values or suppliers that throw a NullPointerException.
         * @return a new filtered source instance.
         */
        public Source<T> whenNonNull() {
            return new Source<>(new NullSafeSupplier<>(this.supplier), Objects::nonNull);
        }

        /**
         * Return a filtered version of the source that will only map values that are true.
         * @return a new filtered source instance.
         */
        public Source<T> whenTrue() {
            return when(Boolean.TRUE::equals);
        }

        /**
         * Return a filtered version of the source that will only map values that are false.
         * @return a new filtered source instance.
         */
        public Source<T> whenFalse() {
            return when(Boolean.FALSE::equals);
        }

    }

    /**
     * Supplier that will catch and ignore any {@link NullPointerException}.
     * @param <T> the source type.
     */
    private static class NullSafeSupplier<T> implements Supplier<T> {

        private final Supplier<T> supplier;

        NullSafeSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            try {
                return supplier.get();
            } catch (NullPointerException ignored) {
                return null;
            }
        }
    }

}
