// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

/**
 * A set of options for selecting shares from Storage File service.
 *
 * <ul>
 *     <li>
 *         Providing {@link ListSharesOptions#setPrefix(String) prefix} will filter selections to
 *         {@link ShareItem shares} that that begin with the prefix.
 *     </li>
 *     <li>
 *         Providing {@link ListSharesOptions#setMaxResultsPerPage(Integer) maxResultsPerPage} will limit the number of
 *         {@link ShareItem shares} returned in a single page.
 *     </li>
 *     <li>
 *         Setting {@link ListSharesOptions#setIncludeMetadata(boolean) includeMetadata} to true will include the
 *         metadata of each {@link ShareItem share}, if false {@link ShareItem#getMetadata()}  metadata} for each share
 *         will be {@code null}.
 *     </li>
 *     <li>
 *         Setting {@link ListSharesOptions#setIncludeSnapshots(boolean) includeSnapshots} to true will include
 *         snapshots of each {@link ShareItem share}, the snapshot will be included as separate items in the response
 *         and will be identifiable by {@link ShareItem#getSnapshot()}  snapshot} having a value. The base share will
 *         contain {@code null} for the snapshot.
 *     </li>
 * </ul>
 */
public final class ListSharesOptions {
    private String prefix;
    private Integer maxResultsPerPage;
    private boolean includeMetadata;
    private boolean includeSnapshots;

    /**
     * Sets the prefix that a share must match to be included in the listing.
     *
     * @param prefix The prefix that shares must start with to pass the filter
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @return the prefix that a share must match to be included in the listing
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the maximum number of shares to include in a single response.
     *
     * @param maxResultsPerPage Maximum number of shares to include in a single response. This value must be between 1
     * and 5000.
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }

    /**
     * @return the maximum number of shares to inlcude in a single response
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Sets the status of including share metadata when listing shares
     *
     * If listing snapshots as well this will also determine if the snapshots have their metadata included as well.
     *
     * @param includeMetadata Flag indicating if metadata should be including in the listing
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * @return the status of include share metadata when listing shares
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the status of including share snapshots when listing shares
     *
     * @param includeSnapshots Flag indicating if snapshots should be included in the listing
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions setIncludeSnapshots(boolean includeSnapshots) {
        this.includeSnapshots = includeSnapshots;
        return this;
    }

    /**
     * @return the status of including share snapshots when listing shares
     */
    public boolean isIncludeSnapshots() {
        return includeSnapshots;
    }
}
