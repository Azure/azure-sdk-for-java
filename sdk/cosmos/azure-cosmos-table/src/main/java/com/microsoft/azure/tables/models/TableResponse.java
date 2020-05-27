package com.microsoft.azure.tables.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableResponse model. */
@Fluent
public final class TableResponse extends TableResponseProperties {
    /*
     * The metadata response of the table.
     */
    @JsonProperty(value = "odata.metadata")
    private String odataMetadata;

    /**
     * Get the odataMetadata property: The metadata response of the table.
     *
     * @return the odataMetadata value.
     */
    public String getOdataMetadata() {
        return this.odataMetadata;
    }

    /**
     * Set the odataMetadata property: The metadata response of the table.
     *
     * @param odataMetadata the odataMetadata value to set.
     * @return the TableResponse object itself.
     */
    public TableResponse setOdataMetadata(String odataMetadata) {
        this.odataMetadata = odataMetadata;
        return this;
    }
}
