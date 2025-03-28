// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

/**
 * Extended options for a directory listing operation.
 */
public final class ShareListFilesAndDirectoriesOptions {
    private String prefix;
    private Integer maxResultsPerPage;
    private boolean includeTimestamps;
    private boolean includeETag;
    private boolean includeAttributes;
    private boolean includePermissionKey;
    private Boolean includeExtendedInfo;

    /**
     * Creates a new instance of {@link ShareListFilesAndDirectoriesOptions}.
     */
    public ShareListFilesAndDirectoriesOptions() {
    }

    /**
     * Gets the prefix for a listing operation.
     *
     * @return prefix for this listing operation.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix for a listing operation.
     *
     * @param prefix the prefix.
     * @return updated options.
     */
    public ShareListFilesAndDirectoriesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets the max results per page for a listing operation.
     *
     * @return max results per page for this listing operation.
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Sets the max results per page for a listing operation.
     *
     * @param maxResultsPerPage the max results per page.
     * @return updated options.
     */
    public ShareListFilesAndDirectoriesOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }

    /**
     * Note that setting timestamps, etag, attributes, or permission key will also set this option as true. Attempting
     * to set it back to false while any of these options are true will be unsuccessful.
     * <p>
     * Including extended info in a listing operation can result in a more expensive operation, but will present
     * more accurate information on the listing item.
     *
     * @return whether to include extended info on this listing operation.
     */
    public Boolean includeExtendedInfo() {
        return includeExtendedInfo;
    }

    /**
     * Note that setting timestamps, etag, attributes, or permission key will also set this option as true. Attempting
     * to set it back to false will be unsuccessful.
     * <p>
     * Sets the prefix for a listing operation.
     * <p>
     * Including extended info in a listing operation can result in a more expensive operation, but will present
     * more accurate information on the listing item.
     *
     * @param includeExtendedInfo whether to include extended info.
     * @return updated options.
     * @throws IllegalStateException Throws when attempting to set null when other parameters require it to be true.
     */
    public ShareListFilesAndDirectoriesOptions setIncludeExtendedInfo(Boolean includeExtendedInfo) {
        this.includeExtendedInfo = includeExtendedInfo;
        return this;
    }

    /**
     * Gets whether to include timestamps on a listing operation.
     *
     * @return whether to include timestamps on this listing operation.
     */
    public boolean includeTimestamps() {
        return includeTimestamps;
    }

    /**
     * Sets whether to include timestamps on a listing operation.
     *
     * @param includeTimestamps whether to include timestamps on this listing operation.
     * @return updated options
     */
    public ShareListFilesAndDirectoriesOptions setIncludeTimestamps(boolean includeTimestamps) {
        this.includeTimestamps = includeTimestamps;
        return this;
    }

    /**
     * Gets whether to include the etag on a listing operation.
     *
     * @return whether to include the etag on this listing operation.
     */
    public boolean includeETag() {
        return includeETag;
    }

    /**
     * Sets whether to include the etag on a listing operation.
     *
     * @param includeETag whether to include the etag on this listing operation.
     * @return updated options
     */
    public ShareListFilesAndDirectoriesOptions setIncludeETag(boolean includeETag) {
        this.includeETag = includeETag;
        return this;
    }

    /**
     * Gets whether to include file attributes on a listing operation.
     *
     * @return whether to include file attributes on this listing operation.
     */
    public boolean includeAttributes() {
        return includeAttributes;
    }

    /**
     * Sets whether to include file attributes on a listing operation.
     *
     * @param includeAttributes whether to include file attributes on this listing operation.
     * @return updated options
     */
    public ShareListFilesAndDirectoriesOptions setIncludeAttributes(boolean includeAttributes) {
        this.includeAttributes = includeAttributes;
        return this;
    }

    /**
     * Gets whether to include the permission key on a listing operation.
     *
     * @return whether to include the permission key on this listing operation.
     */
    public boolean includePermissionKey() {
        return includePermissionKey;
    }

    /**
     * Sets whether to include the permission key on a listing operation.
     *
     * @param includePermissionKey whether to include the permission key on this listing operation.
     * @return updated options
     */
    public ShareListFilesAndDirectoriesOptions setIncludePermissionKey(boolean includePermissionKey) {
        this.includePermissionKey = includePermissionKey;
        return this;
    }
}
