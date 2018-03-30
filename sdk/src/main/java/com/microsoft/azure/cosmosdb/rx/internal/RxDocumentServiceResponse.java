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

package com.microsoft.azure.cosmosdb.rx.internal;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.Conflict;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Address;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.StoreResponse;

/**
 * This is core Transport/Connection agnostic response for the Azure Cosmos DB database service.
 */
public class RxDocumentServiceResponse {
    private final int statusCode;
    private final Map<String, String> headersMap;
    private final StoreResponse storeResponse;

    public RxDocumentServiceResponse(StoreResponse response) {
        String[] headerNames = response.getResponseHeaderNames();
        String[] headerValues = response.getResponseHeaderValues();

        this.headersMap = new HashMap<>(headerNames.length);

        // Gets status code.
        this.statusCode = response.getStatus();

        // TODO: handle session token

        // Extracts headers.
        for (int i = 0; i < headerNames.length; i++) {
            if (!headerNames[i].equals(HttpConstants.HttpHeaders.LSN)) {
                this.headersMap.put(headerNames[i], headerValues[i]);
            }
        }

        this.storeResponse = response;
    }

    public static <T extends Resource> String getResourceKey(Class<T> c) {
        if (c.equals(Attachment.class)) {
            return InternalConstants.ResourceKeys.ATTACHMENTS;
        } else if (c.equals(Conflict.class)) {
            return InternalConstants.ResourceKeys.CONFLICTS;
        } else if (c.equals(Database.class)) {
            return InternalConstants.ResourceKeys.DATABASES;
        } else if (Document.class.isAssignableFrom(c)) {
            return InternalConstants.ResourceKeys.DOCUMENTS;
        } else if (c.equals(DocumentCollection.class)) {
            return InternalConstants.ResourceKeys.DOCUMENT_COLLECTIONS;
        } else if (c.equals(Offer.class)) {
            return InternalConstants.ResourceKeys.OFFERS;
        } else if (c.equals(PartitionKeyRange.class)) {
            return InternalConstants.ResourceKeys.PARTITION_KEY_RANGES;
        } else if (c.equals(Permission.class)) {
            return InternalConstants.ResourceKeys.PERMISSIONS;
        } else if (c.equals(Trigger.class)) {
            return InternalConstants.ResourceKeys.TRIGGERS;
        } else if (c.equals(StoredProcedure.class)) {
            return InternalConstants.ResourceKeys.STOREDPROCEDURES;
        } else if (c.equals(User.class)) {
            return InternalConstants.ResourceKeys.USERS;
        } else if (c.equals(UserDefinedFunction.class)) {
            return InternalConstants.ResourceKeys.USER_DEFINED_FUNCTIONS;
        } else if (c.equals(Address.class)) {
            return InternalConstants.ResourceKeys.ADDRESSES;
        } else if (c.equals(PartitionKeyRange.class)) {
            return InternalConstants.ResourceKeys.PARTITION_KEY_RANGES;
        }

        throw new IllegalArgumentException("c");
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public Map<String, String> getResponseHeaders() {
        return this.headersMap;
    }

    public String getReponseBodyAsString() {
        return this.storeResponse.getResponseBody();
    }

    public <T extends Resource> T getResource(Class<T> c) {
        String responseBody = this.getReponseBodyAsString();
        if (StringUtils.isEmpty(responseBody))
            return null;

        try {
            return c.getConstructor(String.class).newInstance(responseBody);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to instantiate class object.", e);
        }
    }

    public <T extends Resource> List<T> getQueryResponse(Class<T> c) {
        String responseBody = this.getReponseBodyAsString();
        if (responseBody == null) {
            return new ArrayList<T>();
        }

        JSONObject jobject = new JSONObject(responseBody);
        String resourceKey = RxDocumentServiceResponse.getResourceKey(c);
        JSONArray jTokenArray = jobject.getJSONArray(resourceKey);

        // Aggregate queries may return a nested array
        JSONArray innerArray;
        while (jTokenArray != null && jTokenArray.length() == 1 && (innerArray = jTokenArray.optJSONArray(0)) != null) {
            jTokenArray = innerArray;
        }

        List<T> queryResults = new ArrayList<T>();

        if (jTokenArray != null) {
            for (int i = 0; i < jTokenArray.length(); ++i) {
                Object jToken = jTokenArray.get(i);
                // Aggregate on single partition collection may return the aggregated value only
                // In that case it needs to encapsulated in a special document
                String resourceJson = jToken instanceof Number || jToken instanceof Boolean
                        ? String.format("{\"%s\": %s}", Constants.Properties.AGGREGATE, jToken.toString())
                                : jToken.toString();
                        T resource = null;
                        try {
                            resource = c.getConstructor(String.class).newInstance(resourceJson);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            throw new IllegalStateException("Failed to instantiate class object.", e);
                        }

                        queryResults.add(resource);
            }
        }

        return queryResults;
    }

    public InputStream getContentStream() {
        return this.storeResponse.getResponseStream();
    }
}
