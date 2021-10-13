// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring;

/**
 * List all the name of segments that could included in an Azure Service connection string.
 */
public final class ConnectionStringSegments {

    public static final String ENDPOINT = "Endpoint";

    // AMQP related
    public static final String SHARED_ACCESS_KEY_NAME = "SharedAccessKeyName";
    public static final String SHARED_ACCESS_KEY = "SharedAccessKey";
    public static final String SHARED_ACCESS_SIGNATURE = "SharedAccessSignature";
    public static final String ENTITY_PATH = "EntityPath";

    // App Configuration
    public static final String ID = "Id";
    public static final String SECRET = "Secret";

    // Storage
    public static final String DEFAULT_DEDPOINTS_PROTOCOL = "DefaultEndpointsProtocol";
    public static final String ACCOUNT_NAME = "AccountName";
    public static final String ACCOUNT_KEY = "AccountKey";
    public static final String ENDPOINT_SUFFIX = "EndpointSuffix";

}
