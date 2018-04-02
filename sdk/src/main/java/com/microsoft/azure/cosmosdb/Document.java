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

import java.io.IOException;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a document in the Azure Cosmos DB database service.
 * <p>
 * A document is a structured JSON document. There is no set schema for the JSON documents, and a document may contain
 * any number of custom properties as well as an optional list of attachments. Document is an application resource and
 * can be authorized using the master key or resource keys.
 */
@SuppressWarnings("serial")
public class Document extends Resource {

    /**
     * Initialize a document object.
     */
    public Document() {
        super();
    }

    /**
     * Initialize a document object from json string.
     *
     * @param jsonString the json string that represents the document object.
     * @param objectMapper the custom object mapper
     */
    public Document(String jsonString, ObjectMapper objectMapper) {
        super(jsonString, objectMapper);
    }

    /**
     * Initialize a document object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public Document(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize a document object from json object.
     *
     * @param jsonObject the json object that represents the document object.
     */
    public Document(JSONObject jsonObject) {
        super(jsonObject);
    }

    static Document FromObject(Object document, ObjectMapper objectMapper) {
        Document typedDocument;
        if (document instanceof Document) {
            typedDocument = (Document) document;
        } else {
            try {
                return new Document(objectMapper.writeValueAsString(document));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
        return typedDocument;
    }

    /**
     * Gets the document's time-to-live value.
     *
     * @return the document's time-to-live value in seconds.
     */
    public Integer getTimeToLive() {
        if (super.has(Constants.Properties.TTL)) {
            return super.getInt(Constants.Properties.TTL);
        }

        return null;
    }

    /**
     * Sets the document's time-to-live value.
     * <p>
     * A document's time-to-live value is an optional property. If set, the document expires after the specified number
     * of seconds since its last write time. The value of this property should be one of the following:
     * <p>
     * null - indicates the time-to-live value for this document inherits from the parent collection's default time-to-live value.
     * <p>
     * nonzero positive integer - indicates the number of seconds before the document expires. It overrides the default time-to-live
     * value specified on the parent collection, unless the parent collection's default time-to-live is null.
     * <p>
     * -1 - indicates the document never expires. It overrides the default time-to-live
     * value specified on the parent collection, unless the parent collection's default time-to-live is null.
     *
     * @param timeToLive the document's time-to-live value in seconds.
     */
    public void setTimeToLive(Integer timeToLive) {
        // a "null" value is represented as a missing element on the wire.
        // setting timeToLive to null should remove the property from the property bag.
        if (timeToLive != null) {
            super.set(Constants.Properties.TTL, timeToLive);
        } else if (super.has(Constants.Properties.TTL)) {
            super.remove(Constants.Properties.TTL);
        }
    }
}
