// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@code CosmosLazyPageImpl} implementation.
 *
 * @param <T> the type of which the CosmosLazyPageImpl consists.
 */
public class CosmosLazyPageImpl<T> extends PageImpl<T> {

    private static final long serialVersionUID = -2805108120909259912L;

    private final String continuationToken;
    private final transient Supplier<Long> totalFunction;
    private Long totalElements;

    public CosmosLazyPageImpl(List<T> content, Pageable pageable, Supplier<Long> totalFunction, String continuationToken) {
        super(content, pageable, -1);
        this.totalFunction = totalFunction;
        this.continuationToken = continuationToken;
    }

    @Override
    public int getTotalPages() {
        return this.getSize() == 0 ? 1 : (int) Math.ceil((double) this.getTotalElements() / (double) this.getSize());
    }

    @Override
    public long getTotalElements() {
        if (totalElements == null) {
            if (totalFunction == null) {
                throw new IllegalStateException("getTotalElements must be invoked prior to serialization");
            }
            totalElements = totalFunction.get();
        }
        return totalElements;
    }

    @Override
    public boolean hasNext() {
        return continuationToken != null;
    }

    @Override
    public boolean isLast() {
        return continuationToken == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final CosmosLazyPageImpl<?> that = (CosmosLazyPageImpl<?>) o;
        return continuationToken == that.continuationToken;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), continuationToken);
    }

    /**
     * Factory for CosmosLazyPageImpl
     */
    public static class Factory implements CosmosPageFactory {

        @Override
        public <T> Page<T> createPage(List<T> content, Pageable pageable, Supplier<Long> totalFunction) {
            if (!(pageable instanceof CosmosPageRequest)) {
                throw new IllegalArgumentException("Input pageable must be of type " + CosmosPageRequest.class);
            }
            String continuationToken = ((CosmosPageRequest) pageable).getRequestContinuation();
            return new CosmosLazyPageImpl<>(content, pageable, totalFunction, continuationToken);
        }

    }

}
