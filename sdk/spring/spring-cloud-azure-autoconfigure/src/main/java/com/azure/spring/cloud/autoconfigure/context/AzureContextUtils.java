// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

/**
 *
 */
public abstract class AzureContextUtils {

    private AzureContextUtils() {

    }

    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "spring_cloud_azure_default_credential";

    public static final String EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME";

    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME";

    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME =
        "EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME";
    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME";

    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME =
        "EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME";

    public static final String AZURE_GLOBAL_PROPERTY_BEAN_NAME =
        "AZURE_GLOBAL_PROPERTY_BEAN_NAME";

    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME";

    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME =
        "EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME";

    public static final String STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME";

    public static final String STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME =
        "STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME";
}
