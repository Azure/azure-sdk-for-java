// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.models.TableResponseProperties;

/**
 * class for a table object
 */
public class TableItem {
    private final String name;
    private final String odataType;
    private final String odataId;
    private final String odataEditLink;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        ModelHelper.setItemCreator(TableItem::new);
    }

    /**
     * create a table
     * @param properties The TableResponseProperties used to construct this table
     */
    TableItem(TableResponseProperties properties) {
        this.name = properties.getTableName();
        this.odataType = properties.getOdataType();
        this.odataId = properties.getOdataId();
        this.odataEditLink = properties.getOdataEditLink();
    }

    /**
     * returns the name of this table
     *
     * @return table name
     */
    public String getName() {
        return name;
    }

    /**
     * returns the type of this table
     *
     * @return type
     */
    String getOdataType() {
        return odataType;
    }

    /**
     * returns the ID of this table
     *
     * @return ID
     */
    String getOdataId() {
        return odataId;
    }

    /**
     * returns the edit link of this table
     *
     * @return edit link
     */
    String getOdataEditLink() {
        return odataEditLink;
    }
}
