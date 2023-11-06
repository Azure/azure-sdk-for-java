package com.azure.compute.batch.models;

public class ListBatchPoolNodeCountsOptions extends BatchBaseOptions {
    private String filter;
    private Integer maxresults;

    /**
     * Gets the OData $filter clause used for filtering results.
     *
     * @return The OData $filter clause.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the OData $filter clause used for filtering results.
     *
     * @param filter The OData $filter clause.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Gets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @return The maximum number of items to return in the response.
     */
    public Integer getMaxresults() {
        return maxresults;
    }

    /**
     * Sets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @param maxresults The maximum number of items to return in the response.
     */
    public void setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
    }
}
