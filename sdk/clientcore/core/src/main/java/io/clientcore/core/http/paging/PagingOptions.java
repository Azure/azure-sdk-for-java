// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.paging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

/**
 * The paging options for the pageable operation.
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class PagingOptions {

    private Long offset;
    private Long pageSize;
    private Long pageIndex;

    private String continuationToken;

    /**
     * Creates an instance of {@link PagingOptions}.
     */
    public PagingOptions() {
    }

    /**
     * Gets the offset on items.
     *
     * @return the offset on items
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * Sets the offset on items.
     *
     * @param offset the offset on items
     * @return the PagingOptions object itself
     */
    public PagingOptions setOffset(Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the size of a page.
     *
     * @return the size of a page
     */
    public Long getPageSize() {
        return pageSize;
    }

    /**
     * Sets the size of a page.
     *
     * @param pageSize the size of a page
     * @return the PagingOptions object itself
     */
    public PagingOptions setPageSize(Long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Gets the page index.
     *
     * @return the page index
     */
    public Long getPageIndex() {
        return pageIndex;
    }

    /**
     * Sets the page index.
     *
     * @param pageIndex the page index
     * @return the PagingOptions object itself
     */
    public PagingOptions setPageIndex(Long pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    /**
     * Gets the continuation token.
     *
     * @return the continuation token
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets the continuation token.
     *
     * @param continuationToken the continuation token
     * @return the PagingOptions object itself
     */
    public PagingOptions setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }
}
