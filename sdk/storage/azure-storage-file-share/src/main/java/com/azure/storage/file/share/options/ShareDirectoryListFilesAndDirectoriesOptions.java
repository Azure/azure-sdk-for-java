// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ListFilesIncludeType;

import java.util.EnumSet;

/**
 * Extended options for a directory listing operation.
 */
public class ShareDirectoryListFilesAndDirectoriesOptions {

    private String prefix;
    private Integer maxResultsPerPage;
    private EnumSet<ListFilesIncludeType> shareFileTraits;
    private boolean includeExtendedInfo;

    /**
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
    public ShareDirectoryListFilesAndDirectoriesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
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
    public ShareDirectoryListFilesAndDirectoriesOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }

    /**
     * Note that requesting file traits on a listing operation will also request extended info, regardless of whether
     * the value for including extended info is true.
     *
     * Including extended info in a listing operation can result in a longer wait for a response, but will present
     * more accurate information on the listing item.
     *
     * @return file traits to include on this listing operation.
     */
    public EnumSet<ListFilesIncludeType> getShareFileTraits() {
        return shareFileTraits;
    }

    /**
     * Sets the file traits to include for a listing operation. Note that requesting file traits on a listing operation
     * will also request extended info, regardless of whether the value for including extended info is true.
     *
     * Including extended info in a listing operation can result in a longer wait for a response, but will present
     * more accurate information on the listing item.
     *
     * @param shareFileTraits the file traits to include on listing.
     * @return updated options.
     */
    public ShareDirectoryListFilesAndDirectoriesOptions setShareFileTraits(EnumSet<ListFilesIncludeType> shareFileTraits) {
        this.shareFileTraits = shareFileTraits;
        return this;
    }

    /**
     * Note that any operation that requests share file traits will also include extended info, regardless of whether
     * this option is true.
     *
     * Including extended info in a listing operation can result in a longer wait for a response, but will present
     * more accurate information on the listing item.
     *
     * @return whether to include extended info on this listing operation.
     */
    public boolean includeExtendedInfo() {
        return includeExtendedInfo;
    }

    /**
     * Sets the prefix for a listing operation. Note that setting any share file traits to include on a listing will
     * also include extended info, regardless of whether this parameter is true.
     *
     * Including extended info in a listing operation can result in a longer wait for a response, but will present
     * more accurate information on the listing item.
     *
     * @param includeExtendedInfo whether to include extended info..
     * @return updated options.
     */
    public ShareDirectoryListFilesAndDirectoriesOptions setIncludeExtendedInfo(boolean includeExtendedInfo) {
        this.includeExtendedInfo = includeExtendedInfo;
        return this;
    }
}
