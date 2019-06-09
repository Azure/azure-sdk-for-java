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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.Utils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosItemSettings extends Resource {

    private static final ObjectMapper mapper = Utils.getSimpleObjectMapper();

    /**
     * Initialize an empty CosmosItemSettings object.
     */
    public CosmosItemSettings() {}

    /**
     * Initialize a CosmosItemSettings object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public CosmosItemSettings(String jsonString) {
        super(jsonString);
    }
    
    /**
     * fromObject returns Document for compatibility with V2 sdk
     *
     * @param cosmosItem
     * @return
     */
    static Document fromObject(Object cosmosItem) {
        Document typedItem;
        if (cosmosItem instanceof CosmosItemSettings) {
            typedItem = new Document(((CosmosItemSettings) cosmosItem).toJson());
        } else {
            try {
                return new Document(CosmosItemSettings.mapper.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
        return typedItem;
    }

    static List<CosmosItemSettings> getFromV2Results(List<Document> results) {
        return results.stream().map(document -> new CosmosItemSettings(document.toJson())).collect(Collectors.toList());
    }

    public <T> T getObject(Class<?> klass) throws IOException {
        return (T) mapper.readValue(this.toJson(), klass);
    }

}
