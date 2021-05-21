package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ListFilesIncludeType;

import java.util.EnumSet;

public class ShareDirectoryListFilesAndDirectoriesOptions {

    private String prefix;
    private Integer maxResultsPerPage;
    private EnumSet<ListFilesIncludeType> shareFileTraits;
    private boolean includeExtendedInfo;

    public String getPrefix() {
        return prefix;
    }

    public ShareDirectoryListFilesAndDirectoriesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public ShareDirectoryListFilesAndDirectoriesOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }

    public EnumSet<ListFilesIncludeType> getShareFileTraits() {
        return shareFileTraits;
    }

    public ShareDirectoryListFilesAndDirectoriesOptions setShareFileTraits(EnumSet<ListFilesIncludeType> shareFileTraits) {
        this.shareFileTraits = shareFileTraits;
        return this;
    }

    public boolean includeExtendedInfo() {
        return includeExtendedInfo;
    }

    public ShareDirectoryListFilesAndDirectoriesOptions setIncludeExtendedInfo(boolean includeExtendedInfo) {
        this.includeExtendedInfo = includeExtendedInfo;
        return this;
    }
}
