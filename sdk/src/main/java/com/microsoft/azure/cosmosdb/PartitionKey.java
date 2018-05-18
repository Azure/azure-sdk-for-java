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
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;

/**
 * Represents a partition key value in the Azure Cosmos DB database service. A partition key identifies the partition
 * where the document is stored in.
 */
public class PartitionKey {

    private final Object[] key;
    private final String keyString;
    private PartitionKeyInternal internalPartitionKey;

    /**
     * Constructor. Create a new instance of the PartitionKey object.
     *
     * @param key the value of the partition key.
     */
    @SuppressWarnings("serial")
    public PartitionKey(final Object key) {
        this.key = new Object[] {key};
        this.keyString = toJson(Utils.getSimpleObjectMapper().valueToTree(this.key));
        this.internalPartitionKey = PartitionKeyInternal.fromObjectArray(new ArrayList<Object>() {{ add(key); }}, true);
    }

    /**
     * Create a new instance of the PartitionKey object from a serialized JSON string.
     *
     * @param jsonString the JSON string representation of this PartitionKey object.
     * @return the PartitionKey instance.
     */
    public static PartitionKey FromJsonString(String jsonString) {
        JsonNode node = fromJson(jsonString);
        PartitionKey key = new PartitionKey(node.get(0));

        return key;
    }

    /**
     * Gets the Key property.
     *
     * @return the value of the partition key.
     */
    Object[] getKey() {
        return this.key;
    }

    /**
     * Serialize the PartitionKey object to a JSON string.
     *
     * @return the string representation of this PartitionKey object.
     */
    public String toString() {
        return this.keyString;
    }

    public PartitionKeyInternal getInternalPartitionKey() {
        return internalPartitionKey;
    }

    private static JsonNode fromJson(String json){
        try {
            return Utils.getSimpleObjectMapper().readTree(json);
        } catch (IOException e) {
            //Should not happen while reading from String
            throw new IllegalArgumentException(e);
        }
    }

    private static String toJson(Object object){
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
