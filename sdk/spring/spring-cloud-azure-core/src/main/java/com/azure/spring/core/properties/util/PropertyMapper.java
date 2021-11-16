// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.util;

import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is a simple version of PropertyMapper in Spring Boot project.
 */
public class PropertyMapper {

    private final boolean alwaysApplyNonNull;

    public PropertyMapper() {
        this(true);
    }

    public PropertyMapper(boolean alwaysApplyNonNull) {
        this.alwaysApplyNonNull = alwaysApplyNonNull;
    }

    public <T> Source<T> from(Supplier<T> supplier) {
        Assert.notNull(supplier, "Supplier must not be null");
        Source<T> source = new Source<>(supplier, (t) -> true);
        if (alwaysApplyNonNull) {
            source = source.whenNonNull();
        }
        return source;
    }

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
            Assert.notNull(predicate, "Predicate must not be null");
            this.supplier = supplier;
            this.predicate = predicate;
        }

        public void to(Consumer<T> consumer) {
            T val = this.supplier.get();
            if (this.predicate.test(val)) {
                consumer.accept(val);
            }
        }

        public Source<T> when(Predicate<T> predicate) {
            Assert.notNull(predicate, "Predicate must not be null");
            return new Source<>(this.supplier, this.predicate.and(predicate));
        }

        public Source<T> whenNot(Predicate<T> predicate) {
            Assert.notNull(predicate, "Predicate must not be null");
            return when(predicate.negate());
        }

        public Source<T> whenNonNull() {
            return new Source<>(new NullSafeSupplier<>(this.supplier), Objects::nonNull);
        }

        public Source<T> whenTrue() {
            return when(Boolean.TRUE::equals);
        }

        public Source<T> whenFalse() {
            return when(Boolean.FALSE::equals);
        }

    }

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
