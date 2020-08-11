// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

/**
 * Constants for Tables service.
 */
public final class TableConstants {
    /**
     * Name in the Map to get the partition key.
     */
    public static final String PARTITION_KEY = "PartitionKey";

    /**
     * Name in the Map to get the row key.
     */
    public static final String ROW_KEY = "RowKey";

    /**
     * Name in the map to get row key.
     */
    public static final String ETAG_KEY = "odata.etag";

    /**
     * Name in the map for key of metadata related to object.
     */
    public static final String ODATA_METADATA_KEY = "odata.metadata";

    /**
     * Name in the map for the entity's URL.
     */
    public static final String EDIT_LINK_KEY = "odata.editLink";

    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TableConstants() {
    }
}
