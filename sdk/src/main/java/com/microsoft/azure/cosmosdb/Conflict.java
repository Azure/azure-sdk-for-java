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
 * Represents a conflict in the version of a particular resource in the Azure Cosmos DB database service.
 * <p>
 * During rare failure scenarios, conflicts are generated for the documents in transit. Clients can inspect the
 * respective conflict instances  for resources and operations in conflict.
 */
@SuppressWarnings("serial")
public final class Conflict extends Resource {
    /**
     * Initialize a conflict object.
     */
    public Conflict() {
        super();
    }

    /**
     * Initialize a conflict object from json string.
     *
     * @param jsonString the json string that represents the conflict.
     */
    public Conflict(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize a conflict object from json object.
     *
     * @param jsonObject the json object that represents the conflict.
     */
    public Conflict(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets the operation kind.
     *
     * @return the operation kind.
     */
    public String getOperationKind() {
        return super.getString(Constants.Properties.OPERATION_TYPE);
    }

    /**
     * Gets the type of the conflicting resource.
     *
     * @return the resource type.
     */
    public String getResouceType() {
        return super.getString(Constants.Properties.RESOURCE_TYPE);
    }
}
