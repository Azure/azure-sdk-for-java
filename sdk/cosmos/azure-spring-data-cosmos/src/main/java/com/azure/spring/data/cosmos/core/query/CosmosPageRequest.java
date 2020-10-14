// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import com.azure.cosmos.models.FeedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * CosmosPageRequest representing page request during pagination query, field
 * {@link FeedResponse#getContinuationToken()}  response continuation token} is saved
 * to help query next page.
 * <p>
 * The requestContinuation token should be saved after each request and reused in later queries.
 */
public class CosmosPageRequest extends PageRequest {
    private static final long serialVersionUID = 6093304300037688375L;

    private long offset;

    // Request continuation token used to resume query
    private final String requestContinuation;

    /**
     * Creates a new {@link PageRequest} with unsorted parameters applied.
     *
     * @param page zero-based page index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @param requestContinuation must not be {@literal null}.
     */
    public CosmosPageRequest(int page, int size, String requestContinuation) {
        super(page, size, Sort.unsorted());
        this.requestContinuation = requestContinuation;
    }

    /**
     * Creates a new {@link CosmosPageRequest} with sort parameters applied.
     *
     * @param page zero-based page index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @param sort must not be {@literal null}, use {@link Sort#unsorted()} instead.
     * @param requestContinuation must not be {@literal null}.
     */
    public CosmosPageRequest(int page, int size, String requestContinuation, Sort sort) {
        super(page, size, sort);
        this.requestContinuation = requestContinuation;
    }

    private CosmosPageRequest(long offset, int page, int size, String requestContinuation) {
        super(page, size, Sort.unsorted());
        this.offset = offset;
        this.requestContinuation = requestContinuation;
    }

    private CosmosPageRequest(long offset, int page, int size, String requestContinuation,
                              Sort sort) {
        super(page, size, sort);
        this.offset = offset;
        this.requestContinuation = requestContinuation;
    }

    /**
     * Creates a new {@link CosmosPageRequest}
     *
     * @param page zero-based page index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @param requestContinuation cannot be null
     * @param sort cannot be null
     * @return CosmosPageRequest
     */
    public static CosmosPageRequest of(int page, int size, String requestContinuation, Sort sort) {
        return new CosmosPageRequest(0, page, size, requestContinuation, sort);
    }

    /**
     * Creates a new {@link CosmosPageRequest}
     *
     * @param offset cannot be null
     * @param page zero-based page index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @param requestContinuation cannot be null
     * @param sort cannot be null
     * @return CosmosPageRequest
     */
    public static CosmosPageRequest of(long offset, int page, int size, String requestContinuation, Sort sort) {
        return new CosmosPageRequest(offset, page, size, requestContinuation, sort);
    }

    @Override
    public Pageable next() {
        return new CosmosPageRequest(this.offset + (long) this.getPageSize(),
            this.getPageNumber() + 1, getPageSize(), this.requestContinuation, getSort());
    }

    @Override
    public long getOffset() {
        return offset;
    }

    /**
     * To get request continuation
     * @return String
     */
    public String getRequestContinuation() {
        return this.requestContinuation;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result
                    + (requestContinuation != null ? requestContinuation.hashCode() : 0);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CosmosPageRequest)) {
            return false;
        }

        final CosmosPageRequest that = (CosmosPageRequest) obj;

        final boolean continuationTokenEquals = Objects.equals(requestContinuation, that.requestContinuation);

        return continuationTokenEquals
            && super.equals(that);
    }
}
