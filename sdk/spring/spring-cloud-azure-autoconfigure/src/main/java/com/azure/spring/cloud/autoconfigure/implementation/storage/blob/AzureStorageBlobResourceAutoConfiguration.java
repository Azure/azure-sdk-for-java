// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob;

import com.azure.spring.cloud.autoconfigure.implementation.storage.AzureStorageConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobPropertiesConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Resource with Azure Storage Blob support.
 *
 * @since 4.0.0
 */
@Import({
    AzureStorageConfiguration.class,
    AzureStorageBlobPropertiesConfiguration.class,
    AzureStorageBlobProtocolResolverConfiguration.class
})
public class AzureStorageBlobResourceAutoConfiguration {

}
