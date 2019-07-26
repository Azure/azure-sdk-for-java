// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
