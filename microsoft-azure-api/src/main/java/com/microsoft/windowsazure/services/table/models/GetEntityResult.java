/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request for a single table entity in the storage account returned
 * from a Table Service REST API Query Entities operation. This is returned by calls to implementations of
 * {@link TableContract#getEntity(String, String, String)} and
 * {@link TableContract#getEntity(String, String, String, TableServiceOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query Entities</a> documentation
 * on MSDN for details of the underlying Table Service REST API operation.
 */
public class GetEntityResult {
    private Entity entity;

    /**
     * Gets the entity returned in the server response to the request.
     * 
     * @return
     *         The {@link Entity} instance representing the entity returned in the server response to the request.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Reserved for internal use. Sets the entity value from the properties of the <strong>entry</strong> entity
     * returned in the body of the server response.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param entity
     *            An {@link Entity} instance representing the entity returned in the server response to the request.
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
