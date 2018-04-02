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
 * Represents a user defined function in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB supports JavaScript UDFs which can be used inside queries, stored procedures and triggers. For additional
 * details, refer to the server-side JavaScript API documentation.
 */
@SuppressWarnings("serial")
public class UserDefinedFunction extends Resource {

    /**
     * Constructor.
     */
    public UserDefinedFunction() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the user defined function.
     */
    public UserDefinedFunction(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the user defined function.
     */
    public UserDefinedFunction(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     */
    public void setBody(String body) {
        super.set(Constants.Properties.BODY, body);
    }
}

