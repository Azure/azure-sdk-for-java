// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.core.util.CoreUtils;

public class Constants {
    public static final int CACHED_ENCRYPTION_SETTING_DEFAULT_DEFAULT_TTL_IN_MINUTES = 60;

    public static final String PROPERTIES_FILE_NAME = "azure-cosmos-encryption.properties";
    public static final String CURRENT_VERSION = CoreUtils.getProperties(PROPERTIES_FILE_NAME).get("version");
    public static final String CURRENT_NAME = CoreUtils.getProperties(PROPERTIES_FILE_NAME).get("name");
    public static final String USER_AGENT_SUFFIX = CURRENT_NAME + "/" + CURRENT_VERSION;

    public static final String INTENDED_COLLECTION_RID_HEADER = "x-ms-cosmos-intended-collection-rid";

    public static final String IS_CLIENT_ENCRYPTED_HEADER = "x-ms-cosmos-is-client-encrypted";

    public static final String INCORRECT_CONTAINER_RID_SUB_STATUS = "1024";

    public static final String ALLOW_CACHED_READS_HEADER = "x-ms-cosmos-allow-cachedreads";

    public static final String DATABASE_RID_HEADER = "x-ms-cosmos-database-rid";

    public static final String PROPERTY_NAME_ID = "id";
}
