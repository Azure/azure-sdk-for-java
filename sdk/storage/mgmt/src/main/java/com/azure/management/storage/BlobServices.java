/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;


import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.models.BlobServicesInner;
import reactor.core.publisher.Mono;

/**
 * Type representing BlobServices.
 */
@Fluent
public interface BlobServices extends SupportsCreating<BlobServiceProperties.DefinitionStages.Blank>, HasInner<BlobServicesInner> {
    /**
     * Gets the properties of a storage account’s Blob service, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<BlobServiceProperties> getServicePropertiesAsync(String resourceGroupName, String accountName);
}