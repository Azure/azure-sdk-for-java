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

import java.util.List;
import java.util.stream.Collectors;

public class CosmosStoredProcedureProperties extends Resource {

    /**
     * Constructor.
     *
     */
    public CosmosStoredProcedureProperties() {
        super();
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return return the Cosmos stored procedure properties with id set
     */
    public CosmosStoredProcedureProperties id(String id){
        super.id(id);
        return this;
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the stored procedure.
     */
    CosmosStoredProcedureProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param id the id of the stored procedure
     * @param body the body of the stored procedure
     */
    public CosmosStoredProcedureProperties(String id, String body) {
        super();
        super.id(id);
        this.body(body);
    }

    CosmosStoredProcedureProperties(ResourceResponse<StoredProcedure> response) {
        super(response.getResource().toJson());
    }

    /**
     * Get the body of the stored procedure.
     *
     * @return the body of the stored procedure.
     */
    public String body() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the stored procedure.
     *
     * @param body the body of the stored procedure.
     */
    public void body(String body) {
        super.set(Constants.Properties.BODY, body);
    }


    static List<CosmosStoredProcedureProperties> getFromV2Results(List<StoredProcedure> results) {
        return results.stream().map(sproc -> new CosmosStoredProcedureProperties(sproc.toJson())).collect(Collectors.toList());
    }
}
