// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.connectionstring;

import java.net.URI;

public class ServiceBusConnectionString {

    private final URI endpointUri;
    private final String endpoint;
    private final String entityPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;
    private final String sharedAccessSignature;

    public ServiceBusConnectionString(String str) {
        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.SERVICE_BUS);

        this.endpointUri = connectionString.getEndpointUri();
        this.endpoint = connectionString.getSegment(ConnectionStringSegments.ENDPOINT);
        this.entityPath = connectionString.getSegment(ConnectionStringSegments.ENTITY_PATH);
        this.sharedAccessKeyName = connectionString.getSegment(ConnectionStringSegments.SHARED_ACCESS_KEY_NAME);
        this.sharedAccessKey = connectionString.getSegment(ConnectionStringSegments.SHARED_ACCESS_KEY);
        this.sharedAccessSignature = connectionString.getSegment(ConnectionStringSegments.SHARED_ACCESS_SIGNATURE);
    }

    public String getEntityPath() {
        return this.entityPath;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public URI getEndpointUri() {
        return endpointUri;
    }

    public String getFullyQualifiedNamespace() {
        return this.endpointUri.getHost();
    }

    public String getSharedAccessKeyName() {
        return this.sharedAccessKeyName;
    }

    public String getSharedAccessKey() {
        return this.sharedAccessKey;
    }

    public String getSharedAccessSignature() {
        return this.sharedAccessSignature;
    }

}
