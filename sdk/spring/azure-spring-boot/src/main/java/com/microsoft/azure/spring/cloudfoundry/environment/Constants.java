/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloudfoundry.environment;

public class Constants {
    // Property namespaces
    public static final String NAMESPACE_STORAGE = "azure.storage";
    public static final String NAMESPACE_SERVICE_BUS = "azure.servicebus";
    public static final String NAMESPACE_DOCUMENTDB = "azure.documentdb";

    // VCAP credential key names
    public static final String STORAGE_ACCOUNT_NAME = "storage_account_name";
    public static final String PRIMARY_ACCESS_KEY = "primary_access_key";

    public static final String SHARED_ACCESS_NAME = "shared_access_key_name";
    public static final String SHARED_ACCESS_KEY_VALUE = "shared_access_key_value";
    public static final String NAMESPACE_NAME = "namespace_name";

    public static final String HOST_ENDPOINT = "documentdb_host_endpoint";
    public static final String MASTER_KEY = "documentdb_master_key";
    public static final String DATABASE_ID = "documentdb_database_id";

}
