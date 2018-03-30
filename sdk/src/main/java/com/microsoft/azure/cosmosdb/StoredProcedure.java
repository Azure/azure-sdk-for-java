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

package com.microsoft.azure.cosmosdb;

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a stored procedure in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB allows stored procedures to be executed in the storage tier, directly against a document collection. The
 * script gets executed under ACID transactions on the primary storage partition of the specified collection. For
 * additional details, refer to the server-side JavaScript API documentation.
 */
@SuppressWarnings("serial")
public class StoredProcedure extends Resource {

    /**
     * Constructor.
     */
    public StoredProcedure() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the stored procedure.
     */
    public StoredProcedure(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the stored procedure.
     */
    public StoredProcedure(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Get the body of the stored procedure.
     *
     * @return the body of the stored procedure.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the stored procedure.
     *
     * @param body the body of the stored procedure.
     */
    public void setBody(String body) {
        super.set(Constants.Properties.BODY, body);
    }
}

