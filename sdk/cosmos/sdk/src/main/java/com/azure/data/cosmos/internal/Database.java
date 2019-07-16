/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
