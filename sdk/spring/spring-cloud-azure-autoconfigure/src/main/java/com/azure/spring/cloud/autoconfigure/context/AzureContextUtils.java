// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

/**
 *
 */
public abstract class AzureContextUtils {

    private AzureContextUtils() {

    }

    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureDefaultCredential";

    public static final String DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME = "springCloudAzureCredentialTaskExecutor";

    public static final String DEFAULT_CREDENTIAL_THREAD_NAME_PREFIX = "az-identity-";

    public static final String EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsClientBuilderFactory";

    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsConsumerClientBuilderFactory";

    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsConsumerClientBuilder";

    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsProducerClientBuilderFactory";

    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsProducerClientBuilder";

    public static final String AZURE_GLOBAL_PROPERTY_BEAN_NAME =
        "springCloudAzureGlobalProperties";

    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsProcessorCheckpointStoreStorageClientBuilderFactory";

    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsProcessorCheckpointStoreStorageClientBuilder";

    public static final String STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureStorageBlobClientBuilderFactory";

    public static final String STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureStorageBlobClientBuilder";
}
