// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

/**
 * Azure Context utilities.
 */
public abstract class AzureContextUtils {

    private AzureContextUtils() {

    }

    /**
     * Default token credential bean name.
     */
    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureDefaultCredential";

    /**
     * Default credential task executory bean name.
     */
    public static final String DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME = "springCloudAzureCredentialTaskExecutor";

    /**
     * Default credential thread name prefix.
     */
    public static final String DEFAULT_CREDENTIAL_THREAD_NAME_PREFIX = "az-identity-";

    /**
     * Event Hubs client builder factory bean name.
     */
    public static final String EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsClientBuilderFactory";

    /**
     * Event Hubs consumer client builder factory bean name.
     */
    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsConsumerClientBuilderFactory";

    /**
     * Event Hubs consumer client builder bean name.
     */
    public static final String EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsConsumerClientBuilder";

    /**
     * Event Hubs producer client builder factory bean name.
     */
    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsProducerClientBuilderFactory";

    /**
     * Event Hubs producer client builder bean name.
     */
    public static final String EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsProducerClientBuilder";

    /**
     * Azure global properties bean name.
     */
    public static final String AZURE_GLOBAL_PROPERTY_BEAN_NAME =
        "springCloudAzureGlobalProperties";

    /**
     * Event Hubs processor checkpoint store storage client builder factory bean name.
     */
    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureEventHubsProcessorCheckpointStoreStorageClientBuilderFactory";

    /**
     * Event Hubs processor checkpoint store storage client builder bean name.
     */
    public static final String EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureEventHubsProcessorCheckpointStoreStorageClientBuilder";

    /**
     * Storage blob client builder factory bean name.
     */
    public static final String STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME =
        "springCloudAzureStorageBlobClientBuilderFactory";

    /**
     * Storage blob client builder bean name.
     */
    public static final String STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME =
        "springCloudAzureStorageBlobClientBuilder";
}
