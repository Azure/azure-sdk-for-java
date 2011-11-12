package com.microsoft.windowsazure.services.blob;

import java.util.EnumSet;

public class ListBlobsOptions extends BlobOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private String delimiter;
    private EnumSet<BlobListingDetails> listingDetails = EnumSet.noneOf(BlobListingDetails.class);

    public String getPrefix() {
        return prefix;
    }

    public ListBlobsOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListBlobsOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListBlobsOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public ListBlobsOptions setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public EnumSet<BlobListingDetails> getListingDetails() {
        return listingDetails;
    }

    public ListBlobsOptions setListingDetails(EnumSet<BlobListingDetails> listingDetails) {
        this.listingDetails = listingDetails;
        return this;
    }
}
