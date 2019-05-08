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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;

public class CosmosContainerResponse extends CosmosResponse<CosmosContainerSettings> {

    private CosmosContainer container;

    CosmosContainerResponse(ResourceResponse<DocumentCollection> response, CosmosDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.setResourceSettings(null);
        }else{
            super.setResourceSettings(new CosmosContainerSettings(response));
            container = new CosmosContainer(getResourceSettings().getId(), database);
        }
    }

    /**
     * Gets the container settings
     * @return the cosmos container settings
     */
    public CosmosContainerSettings getCosmosContainerSettings() {
        return getResourceSettings();
    }

    /**
     * Gets the Container object
     * @return the Cosmos container object
     */
    public CosmosContainer getContainer() {
        return container;
    }
}
