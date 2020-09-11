// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

/**
 * {@code CosmosPageImpl} implementation.
 *
 * @param <T> the type of which the CosmosPageImpl consists.
 */
public class CosmosPageImpl<T> extends PageImpl<T> {

    private static final long serialVersionUID = 5294396337522314504L;

    //  For any query, CosmosDB returns documents less than or equal to page size
    //  Depending on the number of RUs, the number of returned documents can change
    //  Storing the offset of current page, helps to check hasNext and next values
    private final long offset;

    /**
     * Constructor of {@code CosmosPageImpl}.
     *
     * @param content the content of this page, must not be {@literal null}.
     * @param pageable the paging information, must not be {@literal null}.
     * @param total amount of items available. The total might be adapted considering the length of the content
     *          given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies.
     */
    public CosmosPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
        this.offset = pageable.getOffset();
    }

    @Override
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @Override
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @Override
    public boolean hasNext() {
        return this.offset
                + getContent().size() < getTotalElements();
    }

    @Override
    public boolean isLast() {
        return super.isLast();
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
        final CosmosPageImpl<?> that = (CosmosPageImpl<?>) o;
        return offset == that.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), offset);
    }
}
