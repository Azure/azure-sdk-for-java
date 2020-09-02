// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants for Tables service.
 */
public final class TablesConstants {

    /**
     * Name in the Map to get the table name.
     */
    public static final String TABLE_NAME_KEY = "TableName";

    /**
     * Name in the Map to get the partition key.
     */
    public static final String PARTITION_KEY = "PartitionKey";

    /**
     * Name in the Map to get the row key.
     */
    public static final String ROW_KEY = "RowKey";

    /**
     * Name in the Map to get the timestamp.
     */
    public static final String TIMESTAMP_KEY = "Timestamp";

    /**
     * Name in the map to get the eTag.
     */
    public static final String ODATA_ETAG_KEY = "odata.etag";

    /**
     * Name in the map for key of metadata related to object.
     */
    public static final String ODATA_METADATA_KEY = "odata.metadata";

    /**
     * Name in the map for the table or entity's URL.
     */
    public static final String ODATA_EDIT_LINK_KEY = "odata.editLink";

    /**
     * Name in the map for the table or entity's type.
     */
    public static final String ODATA_TYPE_KEY = "odata.type";

    /**
     * Name suffix expressing the value's type
     */
    public static final String ODATA_TYPE_KEY_SUFFIX = "@odata.type";

    /**
     * Name in the map for the table or entity's ID.
     */
    public static final String ODATA_ID_KEY = "odata.id";

    /**
     * Set of keys returned in OData metadata.
     */
    public static final Set<String> METADATA_KEYS = Stream.of(
        ODATA_EDIT_LINK_KEY,
        ODATA_ETAG_KEY,
        ODATA_ID_KEY,
        ODATA_METADATA_KEY,
        ODATA_TYPE_KEY
    ).collect(Collectors.toCollection(HashSet::new));

    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TablesConstants() {
    }
}
