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

/**
 * Represents the connection mode to be used by the client in the Azure Cosmos DB database service.
 * <p>
 * DIRECT and GATEWAY connectivity modes are supported. GATEWAY is the default.
 * Refer to &lt;see&gt;http://azure.microsoft.com/documentation/articles/documentdb-
 * interactions-with-resources/#connectivity-options&lt;/see&gt; for additional
 * details.
 * </p>
 */
public enum ConnectionMode {

    /**
     * Specifies that requests to server resources are made through a gateway proxy using HTTPS.
     * <p>
     * In GATEWAY mode, all requests are made through a gateway proxy.
     * </p>
     */
    GATEWAY,

    /**
     * Specifies that requests to server resources are made directly to the data nodes.
     * <p>
     * In DIRECT mode, all requests to server resources within a collection, such as documents, stored procedures
     * and user-defined functions, etc., are made directly to the data nodes within the target Cosmos DB cluster
     * using either the HTTPS or TCP/SSL transport protocol.
     * </p><p>
     * Certain operations on account or database level resources, such as databases, collections and users, etc.,
     * are always routed through the gateway using HTTPS.
     * </p>
     */
    DIRECT
}
