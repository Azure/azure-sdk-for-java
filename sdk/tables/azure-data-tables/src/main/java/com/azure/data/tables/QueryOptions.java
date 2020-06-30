// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

public class QueryOptions {

    private String filter;
    private String select;
    private Integer top;


    QueryOptions(String filter, String select, Integer top) {
        this.filter = filter;
        this.select = select;
        this.top = top;
    }

    QueryOptions() {
        filter = null;
        select = null;
        top = null;
    }

    /**
     * getter for Filter parameter
     *
     * @return filter parameter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * getter for Select parameter
     *
     * @return select parameter
     */
    public String getSelect() {
        return select;
    }

    /**
     * getter for Top parameter
     *
     * @return top parameter
     */
    public Integer getTop() {
        return top;
    }

    /**
     * setter for filter parameter
     *
     * @param filter the odata filter string
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * setter for select parameter
     *
     * @param select the odata select string
     */
    public void setSelect(String select) {
        this.select = select;
    }

    /**
     * setter for top parameter
     *
     * @param top the odata top integer
     */
    public void setTop(Integer top) {
        this.top = top;
    }

}
