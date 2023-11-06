package com.azure.compute.batch.models;

import java.util.List;

public class GetBatchNodeExtensionOptions extends BatchBaseOptions {
    private List<String> select;

    /**
     * Gets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @return The OData $select clause.
     */
    public List<String> getSelect() {
        return select;
    }

    /**
     * Sets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @param select The OData $select clause.
     */
    public void setSelect(List<String> select) {
        this.select = select;
    }

}
