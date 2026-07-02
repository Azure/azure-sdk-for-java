// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A stream wrapper that enables proper parallel processing for paged data.
 * This stream behaves sequentially by default but can efficiently switch to parallel
 * processing when .parallel() is called by collecting all data first.
 * 
 * @param <T> The type of elements in the stream
 */
final class ParallelCapablePagedStream<T> implements Stream<T> {
    private final Iterable<T> source;
    private Stream<T> delegate;
    private boolean isParallel;

    ParallelCapablePagedStream(Iterable<T> source) {
        this.source = source;
        this.delegate = null;
        this.isParallel = false;
    }

    private Stream<T> getDelegate() {
        if (delegate == null) {
            if (isParallel) {
                // For parallel processing, collect all items first and create a parallel stream
                List<T> collected = new ArrayList<>();
                source.forEach(collected::add);
                delegate = collected.parallelStream();
            } else {
                // For sequential processing, use the original iterable
                delegate = StreamSupport.stream(source.spliterator(), false);
            }
        }
        return delegate;
    }

    @Override
    public Stream<T> parallel() {
        if (isParallel) {
            return this;
        }
        ParallelCapablePagedStream<T> parallelStream = new ParallelCapablePagedStream<>(source);
        parallelStream.isParallel = true;
        return parallelStream;
    }

    @Override
    public Stream<T> sequential() {
        if (!isParallel) {
            return this;
        }
        ParallelCapablePagedStream<T> sequentialStream = new ParallelCapablePagedStream<>(source);
        sequentialStream.isParallel = false;
        return sequentialStream;
    }

    @Override
    public boolean isParallel() {
        return isParallel;
    }

    @Override
    public Stream<T> unordered() {
        return getDelegate().unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return getDelegate().onClose(closeHandler);
    }

    @Override
    public void close() {
        if (delegate != null) {
            delegate.close();
        }
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return getDelegate().filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return getDelegate().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return getDelegate().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return getDelegate().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return getDelegate().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return getDelegate().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return getDelegate().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return getDelegate().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return getDelegate().flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return getDelegate().distinct();
    }

    @Override
    public Stream<T> sorted() {
        return getDelegate().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return getDelegate().sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return getDelegate().peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return getDelegate().limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return getDelegate().skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        getDelegate().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        getDelegate().forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return getDelegate().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return getDelegate().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return getDelegate().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return getDelegate().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return getDelegate().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return getDelegate().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return getDelegate().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return getDelegate().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return getDelegate().max(comparator);
    }

    @Override
    public long count() {
        return getDelegate().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return getDelegate().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return getDelegate().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return getDelegate().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return getDelegate().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return getDelegate().findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return getDelegate().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return getDelegate().spliterator();
    }
}
