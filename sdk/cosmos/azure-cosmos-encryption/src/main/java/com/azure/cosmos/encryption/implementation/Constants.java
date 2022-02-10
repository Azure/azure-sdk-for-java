// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

public class Constants {
    public static final int CACHED_ENCRYPTION_SETTING_DEFAULT_DEFAULT_TTL_IN_MINUTES = 60;

    public static final String INTENDED_COLLECTION_RID_HEADER = "x-ms-cosmos-intended-collection-rid";

    public static final String IS_CLIENT_ENCRYPTED_HEADER = "x-ms-cosmos-is-client-encrypted";

    public static final String INCORRECT_CONTAINER_RID_SUB_STATUS = "1024";
}
