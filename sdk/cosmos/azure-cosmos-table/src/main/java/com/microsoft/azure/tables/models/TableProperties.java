package com.microsoft.azure.tables.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableProperties model. */
@Fluent
public final class TableProperties {
    /*
     * The name of the table to create.
     */
    @JsonProperty(value = "TableName")
    private String tableName;

    /**
     * Get the tableName property: The name of the table to create.
     *
     * @return the tableName value.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Set the tableName property: The name of the table to create.
     *
     * @param tableName the tableName value to set.
     * @return the TableProperties object itself.
     */
    public TableProperties setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
}
