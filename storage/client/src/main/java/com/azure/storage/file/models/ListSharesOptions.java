// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

/**
 * A set of options for selecting shares from Storage File service.
 *
 * <ul>
 *     <li>
 *         Providing {@link ListSharesOptions#prefix(String) prefix} will filter selections to {@link ShareItem shares}
 *         that that begin with the prefix.
 *     </li>
 *     <li>
 *         Providing {@link ListSharesOptions#maxResults(Integer) maxResults} will limit the number of {@link ShareItem shares}
 *         returned in a single page.
 *     </li>
 *     <li>
 *         Setting {@link ListSharesOptions#includeMetadata(boolean) includeMetadata} to true will include the metadata
 *         of each {@link ShareItem share}, if false {@link ShareItem#metadata() metadata} for each share will be {@code null}.
 *     </li>
 *     <li>
 *         Setting {@link ListSharesOptions#includeSnapshots(boolean) includeSnapshots} to true will include snapshots
 *         of each {@link ShareItem share}, the snapshot will be included as separate items in the response and will be
 *         identifiable by {@link ShareItem#snapshot() snapshot} having a value. The base share will contain {@code null}
 *         for the snapshot.
 *     </li>
 * </ul>
 */
public final class ListSharesOptions {
    private String prefix;
    private Integer maxResults;
    private boolean includeMetadata;
    private boolean includeSnapshots;

    /**
     * Sets the prefix that a share must match to be included in the listing.
     *
     * @param prefix The prefix that shares must start with to pass the filter
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @return the prefix that a share must match to be included in the listing
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Sets the maximum number of shares to include in a single response.
     *
     * @param maxResults Maximum number of shares to include in a single response. This value must be between 1 and 5000.
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @return the maximum number of shares to inlcude in a single response
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Sets the status of including share metadata when listing shares
     *
     * If listing snapshots as well this will also determine if the snapshots have their metadata included as well.
     *
     * @param includeMetadata Flag indicating if metadata should be including in the listing
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions includeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * @return the status of include share metadata when listing shares
     */
    public boolean includeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the status of including share snapshots when listing shares
     *
     * @param includeSnapshots Flag indicating if snapshots should be included in the listing
     * @return An updated ListSharesOptions object
     */
    public ListSharesOptions includeSnapshots(boolean includeSnapshots) {
        this.includeSnapshots = includeSnapshots;
        return this;
    }

    /**
     * @return the status of including share snapshots when listing shares
     */
    public boolean includeSnapshots() {
        return includeSnapshots;
    }
}
