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

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.UserDefinedFunction;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosUserDefinedFunctionProperties extends Resource {

    /**
     * Constructor
     */
    public CosmosUserDefinedFunctionProperties(){
        super();
    }

    CosmosUserDefinedFunctionProperties(ResourceResponse<UserDefinedFunction> response) {
        super(response.getResource().toJson());
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the cosmos user defined function properties.
     */
    CosmosUserDefinedFunctionProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current instance of cosmos user defined function properties
     */
    public CosmosUserDefinedFunctionProperties id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String body() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     * @return the CosmosUserDefinedFunctionProperties.
     */
    public CosmosUserDefinedFunctionProperties body(String body) {
        super.set(Constants.Properties.BODY, body);
        return this;
    }

    static List<CosmosUserDefinedFunctionProperties> getFromV2Results(List<UserDefinedFunction> results) {
        return results.stream().map(udf -> new CosmosUserDefinedFunctionProperties(udf.toJson())).collect(Collectors.toList());
    }
}
