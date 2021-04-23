// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import java.util.List;
import reactor.core.publisher.Mono;

/** Type representing BlobContainers. */
@Fluent
public interface BlobContainers {
    /**
     * Begins definition for a new Container resource.
     *
     * @param name resource name.
     * @return the first stage of the new Container definition.
     */
    BlobContainer.DefinitionStages.Blank defineContainer(String name);

    /**
     * Begins definition for a new ImmutabilityPolicy resource.
     *
     * @param name resource name.
     * @return the first stage of the new ImmutabilityPolicy definition.
     */
    ImmutabilityPolicy.DefinitionStages.Blank defineImmutabilityPolicy(String name);

    /**
     * Lists all containers and does not support a prefix like data plane. Also SRP today does not return continuation
     * token.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    PagedFlux<ListContainerItemInner> listAsync(String resourceGroupName, String accountName);

    /**
     * Gets properties of a specified container.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<BlobContainer> getAsync(String resourceGroupName, String accountName, String containerName);

    /**
     * Deletes specified container under its account.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<Void> deleteAsync(String resourceGroupName, String accountName, String containerName);

    /**
     * Sets legal hold tags. Setting the same tag results in an idempotent operation. SetLegalHold follows an append
     * pattern and does not clear out the existing tags that are not specified in the request.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param tags Each tag should be 3 to 23 alphanumeric characters and is normalized to lower case at SRP.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<LegalHold> setLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags);

    /**
     * Clears legal hold tags. Clearing the same or non-existent tag results in an idempotent operation. ClearLegalHold
     * clears out only the specified tags in the request.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param tags Each tag should be 3 to 23 alphanumeric characters and is normalized to lower case at SRP.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<LegalHold> clearLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags);

    /**
     * Gets the existing immutability policy along with the corresponding ETag in response headers and body.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName);

    /**
     * Gets the existing immutability policy along with the corresponding ETag in response headers and body.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param eTagValue The entity state (ETag) version of the immutability policy to update. A value of "*" can be used
     *     to apply the operation only if the immutability policy already exists. If omitted, this operation will always
     *     be applied.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue);

    /**
     * Aborts an unlocked immutability policy. The response of delete has immutabilityPeriodSinceCreationInDays set to
     * 0. ETag in If-Match is required for this operation. Deleting a locked immutability policy is not allowed, only
     * way is to delete the container after deleting all blobs inside the container.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<Void> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName);

    /**
     * Aborts an unlocked immutability policy. The response of delete has immutabilityPeriodSinceCreationInDays set to
     * 0. ETag in If-Match is required for this operation. Deleting a locked immutability policy is not allowed, only
     * way is to delete the container after deleting all blobs inside the container.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param eTagValue The entity state (ETag) version of the immutability policy to update. A value of "*" can be used
     *     to apply the operation only if the immutability policy already exists. If omitted, this operation will always
     *     be applied.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<Void> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue);

    /**
     * Sets the ImmutabilityPolicy to Locked state. The only action allowed on a Locked policy is
     * ExtendImmutabilityPolicy action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName);

    /**
     * Sets the ImmutabilityPolicy to Locked state. The only action allowed on a Locked policy is
     * ExtendImmutabilityPolicy action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param eTagValue The entity state (ETag) version of the immutability policy to update. A value of "*" can be used
     *     to apply the operation only if the immutability policy already exists. If omitted, this operation will always
     *     be applied.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue);

    /**
     * Extends the immutabilityPeriodSinceCreationInDays of a locked immutabilityPolicy. The only action allowed on a
     * Locked policy will be this action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param immutabilityPeriodSinceCreationInDays The immutability period for the blobs in the container since the
     *     policy creation, in days.
     * @param allowProtectedAppendWrites This property can only be changed for unlocked time-based retention policies.
     *     When enabled, new blocks can be written to an append blob while maintaining immutability protection and
     *     compliance. Only new blocks can be added and any existing blocks cannot be modified or deleted. This property
     *     cannot be changed with ExtendImmutabilityPolicy API.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> extendImmutabilityPolicyAsync(
        String resourceGroupName,
        String accountName,
        String containerName,
        int immutabilityPeriodSinceCreationInDays,
        Boolean allowProtectedAppendWrites);

    /**
     * Extends the immutabilityPeriodSinceCreationInDays of a locked immutabilityPolicy. The only action allowed on a
     * Locked policy will be this action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param immutabilityPeriodSinceCreationInDays The immutability period for the blobs in the container since the
     *     policy creation, in days.
     * @param allowProtectedAppendWrites This property can only be changed for unlocked time-based retention policies.
     *     When enabled, new blocks can be written to an append blob while maintaining immutability protection and
     *     compliance. Only new blocks can be added and any existing blocks cannot be modified or deleted. This property
     *     cannot be changed with ExtendImmutabilityPolicy API.
     * @param eTagValue The entity state (ETag) version of the immutability policy to update. A value of "*" can be used
     *     to apply the operation only if the immutability policy already exists. If omitted, this operation will always
     *     be applied.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<ImmutabilityPolicy> extendImmutabilityPolicyAsync(
        String resourceGroupName,
        String accountName,
        String containerName,
        int immutabilityPeriodSinceCreationInDays,
        Boolean allowProtectedAppendWrites,
        String eTagValue);

    /**
     * Lists all containers and does not support a prefix like data plane. Also SRP today does not return continuation
     * token.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response schema.
     */
    PagedIterable<ListContainerItemInner> list(String resourceGroupName, String accountName);

    /**
     * Gets properties of a specified container.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return properties of a specified container.
     */
    BlobContainer get(String resourceGroupName, String accountName, String containerName);

    /**
     * Deletes specified container under its account.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String accountName, String containerName);

    /**
     * Sets legal hold tags. Setting the same tag results in an idempotent operation. SetLegalHold follows an append
     * pattern and does not clear out the existing tags that are not specified in the request.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param tags Each tag should be 3 to 23 alphanumeric characters and is normalized to lower case at SRP.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the LegalHold property of a blob container.
     */
    LegalHold setLegalHold(
        String resourceGroupName, String accountName, String containerName, List<String> tags);

    /**
     * Clears legal hold tags. Clearing the same or non-existent tag results in an idempotent operation. ClearLegalHold
     * clears out only the specified tags in the request.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param tags Each tag should be 3 to 23 alphanumeric characters and is normalized to lower case at SRP.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the LegalHold property of a blob container.
     */
    LegalHold clearLegalHold(
        String resourceGroupName, String accountName, String containerName, List<String> tags);

    /**
     * Gets the existing immutability policy along with the corresponding ETag in response headers and body.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the existing immutability policy along with the corresponding ETag in response headers and body.
     */
    ImmutabilityPolicy getImmutabilityPolicy(String resourceGroupName, String accountName, String containerName);

    /**
     * Aborts an unlocked immutability policy. The response of delete has immutabilityPeriodSinceCreationInDays set to
     * 0. ETag in If-Match is required for this operation. Deleting a locked immutability policy is not allowed, the
     * only way is to delete the container after deleting all expired blobs inside the policy locked container.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteImmutabilityPolicy(String resourceGroupName, String accountName, String containerName);

    /**
     * Sets the ImmutabilityPolicy to Locked state. The only action allowed on a Locked policy is
     * ExtendImmutabilityPolicy action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the ImmutabilityPolicy property of a blob container, including Id, resource name, resource type, Etag.
     */
    ImmutabilityPolicy lockImmutabilityPolicy(
        String resourceGroupName, String accountName, String containerName);

    /**
     * Extends the immutabilityPeriodSinceCreationInDays of a locked immutabilityPolicy. The only action allowed on a
     * Locked policy will be this action. ETag in If-Match is required for this operation.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
     *     insensitive.
     * @param accountName The name of the storage account within the specified resource group. Storage account names
     *     must be between 3 and 24 characters in length and use numbers and lower-case letters only.
     * @param containerName The name of the blob container within the specified storage account. Blob container names
     *     must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-) only. Every
     *     dash (-) character must be immediately preceded and followed by a letter or number.
     * @param immutabilityPeriodSinceCreationInDays The immutability period for the blobs in the container since the
     *     policy creation, in days.
     * @param allowProtectedAppendWrites This property can only be changed for unlocked time-based retention policies.
     *     When enabled, new blocks can be written to an append blob while maintaining immutability protection and
     *     compliance. Only new blocks can be added and any existing blocks cannot be modified or deleted. This property
     *     cannot be changed with ExtendImmutabilityPolicy API.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the ImmutabilityPolicy property of a blob container, including Id, resource name, resource type, Etag.
     */
    ImmutabilityPolicy extendImmutabilityPolicy(
        String resourceGroupName, String accountName, String containerName,
        int immutabilityPeriodSinceCreationInDays, Boolean allowProtectedAppendWrites);
}
