// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Objects;

/**
 * {@code CosmosSliceImpl} implementation.
 *
 * @param <T> the type of which the CosmosSliceImpl consists.
 */
public class CosmosSliceImpl<T> extends SliceImpl<T> {

    private static final long serialVersionUID = 4487077496284030775L;

    //  For any query, CosmosDB returns documents less than or equal to page size
    //  Depending on the number of RUs, the number of returned documents can change
    //  Storing the offset of current page, helps to check hasNext and next values
    private final long offset;

    /**
     * Constructor of {@code CosmosSliceImpl}.
     *
     * @param content the content of this page, must not be {@literal null}.
     * @param pageable the paging information, must not be {@literal null}.
     * @param hasNext whether the query has any more results to fetch
     */
    public CosmosSliceImpl(List<T> content, Pageable pageable, boolean hasNext) {
        super(content, pageable, hasNext);
        this.offset = pageable.getOffset();
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
        final CosmosSliceImpl<?> that = (CosmosSliceImpl<?>) o;
        return offset == that.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), offset);
    }
}
