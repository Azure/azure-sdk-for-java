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
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties>{
    private CosmosDatabase database;

    CosmosDatabaseResponse(ResourceResponse<Database> response, CosmosClient client) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosDatabaseProperties(response));
            database = new CosmosDatabase(resourceSettings().id(), client);
        }
    }

    /**
     * Gets the CosmosDatabase object
     *
     * @return {@link CosmosDatabase}
     */
    public CosmosDatabase database() {
        return database;
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties properties() {
        return resourceSettings();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the database quota.
     */
    public long databaseQuota(){
        return resourceResponseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current database usage.
     */
    public long databaseUsage(){
        return resourceResponseWrapper.getDatabaseUsage();
    }

}
