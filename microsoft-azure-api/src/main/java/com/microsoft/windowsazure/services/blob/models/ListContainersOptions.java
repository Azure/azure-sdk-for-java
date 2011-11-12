package com.microsoft.windowsazure.services.blob.models;

import java.util.EnumSet;


public class ListContainersOptions extends BlobOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private EnumSet<ContainerListingDetails> listingDetails = EnumSet.noneOf(ContainerListingDetails.class);

    public String getPrefix() {
        return prefix;
    }

    public ListContainersOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListContainersOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListContainersOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public EnumSet<ContainerListingDetails> getListingDetails() {
        return listingDetails;
    }

    public ListContainersOptions setListingDetails(EnumSet<ContainerListingDetails> listingDetails) {
        this.listingDetails = listingDetails;
        return this;
    }
}