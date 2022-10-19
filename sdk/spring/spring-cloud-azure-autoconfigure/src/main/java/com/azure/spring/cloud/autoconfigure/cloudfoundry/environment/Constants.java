// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry.environment;

/**
 * utils class of Constants
 */
final class Constants {

    private Constants() {

    }

    // Property namespaces
    /**
     * ServiceBus namespace
     */
    static final String NAMESPACE_SERVICE_BUS = "azure.servicebus";

    /**
     * Document DB namespace
     */
    static final String NAMESPACE_DOCUMENTDB = "azure.documentdb";

    // VCAP credential key names
    /**
     * Shared access key name
     */
    static final String SHARED_ACCESS_KEY_NAME = "shared_access_key_name";

    /**
     * Shared access key value
     */
    static final String SHARED_ACCESS_KEY_VALUE = "shared_access_key_value";

    /**
     * Namespace name
     */
    static final String NAMESPACE_NAME = "namespace_name";

    /**
     * Host endpoint
     */
    static final String HOST_ENDPOINT = "documentdb_host_endpoint";

    /**
     * Master key
     */
    static final String MASTER_KEY = "documentdb_master_key";

    /**
     * Database ID
     */
    static final String DATABASE_ID = "documentdb_database_id";

}
