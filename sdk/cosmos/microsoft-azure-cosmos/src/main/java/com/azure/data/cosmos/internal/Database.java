// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.Resource;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a Database in the Azure Cosmos DB database service. A database manages users, permissions and a set of collections
 * <p>
 * Each Azure Cosmos DB Service is able to support multiple independent named databases, with the database being the
 * logical container for data. Each Database consists of one or more collections, each of which in turn contain one or
 * more documents. Since databases are an an administrative resource and the Service Master Key will be required in
 * order to access and successfully complete any action using the User APIs.
 */
public final class Database extends Resource {

    /**
     * Initialize a database object.
     */
    public Database() {
        super();
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current instance of Database
     */
    public Database id(String id){
        super.id(id);
        return this;
    }

    /**
     * Initialize a database object from json string.
     *
     * @param jsonString the json string.
     */
    public Database(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the self-link for collections in the database
     *
     * @return the collections link.
     */
    public String getCollectionsLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.selfLink(), "/"),
                super.getString(Constants.Properties.COLLECTIONS_LINK));
    }

    /**
     * Gets the self-link for users in the database.
     *
     * @return the users link.
     */
    public String getUsersLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.selfLink(), "/"),
                super.getString(Constants.Properties.USERS_LINK));
    }
}
