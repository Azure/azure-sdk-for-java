package com.microsoft.azure.services.queue;

import java.util.EnumSet;

public class ListQueuesOptions extends QueueServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private EnumSet<QueueListingDetails> listingDetails = EnumSet.noneOf(QueueListingDetails.class);

    public String getPrefix() {
        return prefix;
    }

    public ListQueuesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListQueuesOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListQueuesOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public EnumSet<QueueListingDetails> getListingDetails() {
        return listingDetails;
    }

    public ListQueuesOptions setListingDetails(EnumSet<QueueListingDetails> listingDetails) {
        this.listingDetails = listingDetails;
        return this;
    }
}