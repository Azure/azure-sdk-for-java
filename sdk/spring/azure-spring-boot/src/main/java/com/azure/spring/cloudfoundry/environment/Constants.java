// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

/**
 * utils class of Constants
 */
public class Constants {
    // Property namespaces
    /**
     * ServiceBus namespace
     */
    public static final String NAMESPACE_SERVICE_BUS = "azure.servicebus";

    /**
     * Document DB namespace
     */
    public static final String NAMESPACE_DOCUMENTDB = "azure.documentdb";

    // VCAP credential key names
    /**
     * Shared access key name
     */
    public static final String SHARED_ACCESS_NAME = "shared_access_key_name";

    /**
     * Shared access key value
     */
    public static final String SHARED_ACCESS_KEY_VALUE = "shared_access_key_value";

    /**
     * Namespace name
     */
    public static final String NAMESPACE_NAME = "namespace_name";

    /**
     * Host endpoint
     */
    public static final String HOST_ENDPOINT = "documentdb_host_endpoint";

    /**
     * Master key
     */
    public static final String MASTER_KEY = "documentdb_master_key";

    /**
     * Database ID
     */
    public static final String DATABASE_ID = "documentdb_database_id";

}
