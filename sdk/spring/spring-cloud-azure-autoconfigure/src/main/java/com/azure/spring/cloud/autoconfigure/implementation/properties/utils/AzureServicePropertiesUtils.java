// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.utils;

import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * Util class for processing Azure Service properties.
 */
public final class AzureServicePropertiesUtils {

    public AzureServicePropertiesUtils() {
    }

    /**
     * Load the default value to an Azure Service properties from the Azure Storage properties.
     *
     * @param source The Azure Storage properties.
     * @param target The properties of an Azure Service, such as Storage Blob properties. Some common components of the
     *               service's properties have default value as set to the Azure properties. For example, the proxy of
     *               the Storage Blob properties takes the proxy set to the Azure properties as default.
     * @param <T> The type of the properties of an Azure Service.
     * @return The Azure Service's properties.
     */
    public static <T extends AzureProperties> T loadStorageProperties(AzureStorageProperties source, T target) {
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);
        AzurePropertiesUtils.copyPropertiesIgnoreNull(source, target);
        return target;
    }

}
