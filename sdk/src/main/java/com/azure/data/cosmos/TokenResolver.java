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

import java.util.Map;

/**
 * This interface is for client side implementation, which can be used for initializing
 * AsyncDocumentClient without passing master key, resource token and permission feed.<br>
 * <br>
 * Each time the SDK create request for CosmosDB, authorization token is generated based on that
 * request at client side which enables creation of one AsyncDocumentClient per application shared across various users
 * with different resource permissions.
 */
@FunctionalInterface
public interface TokenResolver {

    /**
     * This method will consume the request information and based on that it will generate the authorization token.
     * @param properties the user properties.
     * @param requestVerb Request verb i.e. GET, POST, PUT etc.
     * @param resourceIdOrFullName ResourceID or resource full name.
     * @param resourceType Resource type i.e. Database, DocumentCollection, Document etc.
     * @return The authorization token.
     */
    public String getAuthorizationToken(String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object> properties);

}
