/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.util.HashMap;

/**
 * A class which represents the result of a table operation. The {@link TableResult} class encapsulates the HTTP
 * response
 * and any table entity results returned by the Storage Service REST API operation called for a particular
 * {@link TableOperation}.
 * 
 */
public class TableResult {
    private Object result;

    private int httpStatusCode = -1;

    private String id;

    private String etag;

    private HashMap<String, EntityProperty> properties;

    /**
     * Initializes an empty {@link TableResult} instance.
     */
    public TableResult() {
        // empty ctor
    }

    /**
     * Initializes a {@link TableResult} instance with the specified HTTP status code.
     * 
     * @param httpStatusCode
     *            The HTTP status code for the table operation returned by the server.
     */
    public TableResult(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Gets the Etag returned with the table operation results. The server will return the same Etag value for a
     * table, entity, or entity group returned by an operation as long as it is unchanged on the server.
     * 
     * @return
     *         A <code>String</code> containing the Etag returned by the server with the table operation results.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the HTTP status code returned by a table operation request.
     * 
     * @return
     *         The HTTP status code for the table operation returned by the server.
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * Gets the AtomPub Entry Request ID value for the result returned by a table operation request.
     * 
     * @return
     *         The Entry Request ID for the table operation result.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the map of properties for a table entity returned by the table operation.
     * 
     * @return
     *         A <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty} data
     *         typed values representing the properties of a table entity.
     */
    public HashMap<String, EntityProperty> getProperties() {
        return this.properties;
    }

    /**
     * Gets the result returned by the table operation as an Object.
     * 
     * @return
     *         The result returned by the table operation as an <code>Object</code>.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Gets the result returned by the table operation as an instance of the specified type.
     * 
     * @return
     *         The result returned by the table operation as an instance of type <code>T</code>.
     */
    @SuppressWarnings("unchecked")
    public <T> T getResultAsType() {
        return (T) this.getResult();
    }

    /**
     * Reserved for internal use. Sets the Etag associated with the table operation results.
     * 
     * @param etag
     *            A <code>String</code> containing an Etag to associate with the table operation results.
     */
    protected void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Reserved for internal use. Sets the HTTP status code associated with the table operation results.
     * 
     * @param httpStatusCode
     *            The HTTP status code value to associate with the table operation results.
     */
    protected void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Reserved for internal use. Sets the AtomPub Entry Request ID associated with the table operation result.
     * 
     * @param id
     *            A <code>String</code> containing the request ID to associate with the table operation result.
     */
    protected void setId(final String id) {
        this.id = id;
    }

    /**
     * Reserved for internal use. Sets the map of properties for a table entity to associate with the table operation.
     * 
     * @param properties
     *            A <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty} data
     *            typed values representing the properties of a table entity to associate with the table operation.
     */
    protected void setProperties(final HashMap<String, EntityProperty> properties) {
        this.properties = properties;
    }

    /**
     * Reserved for internal use. Sets a result Object instance to associate with the table operation.
     * 
     * @param result
     *            An instance of a result <code>Object</code> to associate with the table operation.
     */
    protected void setResult(final Object result) {
        this.result = result;
    }

    /**
     * Reserved for internal use. Sets the result to associate with the table operation as a {@link TableEntity}.
     * 
     * @param ent
     *            An instance of an object implementing {@link TableEntity} to associate with the table operation.
     */
    protected void updateResultObject(final TableEntity ent) {
        this.result = ent;
        ent.setEtag(this.etag);
    }
}
