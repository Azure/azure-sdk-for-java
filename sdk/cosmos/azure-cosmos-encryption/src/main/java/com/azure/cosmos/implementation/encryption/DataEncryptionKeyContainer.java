// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import reactor.core.publisher.Mono;


/**
 * TODO: change the interface to package private? moderakh
 * TODO: moderakh add read-feed/query apis for data encryption key
 * Container for data encryption keys. Provides methods to create, re-wrap, read and enumerate data encryption keys.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface DataEncryptionKeyContainer {
    /**
     * Generates a data encryption key, wraps it using the key wrap metadata provided
     * with the key wrapping provider in the EncryptionSerializer configured on the client via <see cref="CosmosClientBuilder.WithCustomSerializer"/>,
     * and saves the wrapped data encryption key as an asynchronous operation in the Azure Cosmos service.
     *
     * @param id                        Unique identifier for the data encryption key.
     * @param encryptionAlgorithm       Encryption algorithm that will be used along with this data encryption key to encrypt/decrypt data.
     * @param encryptionKeyWrapMetadata Metadata used by the configured key wrapping provider in order to wrap the key.
     * @param requestOptions            (Optional) The options for the request.
     * @return A Mono response which wraps a {@link DataEncryptionKeyProperties} containing the read resource record.
     * <p>
     * on Failure: {@link com.azure.cosmos.CosmosException} indicating the failure reason.
     * <ui>
     * <li>BadRequest - This means something was wrong with the request supplied. It is likely that an id was not supplied for the new encryption key.</li>
     * <li>Conflict - This means an {@link DataEncryptionKeyProperties} with an id matching the id you supplied already existed.</li>
     * </ui>
     */
    Mono<CosmosItemResponse<DataEncryptionKeyProperties>> createDataEncryptionKeyAsync(
        String id,
        String encryptionAlgorithm,
        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata,
        CosmosItemRequestOptions requestOptions);

    /// <summary>
    /// Wraps the raw data encryption key (after unwrapping using the old metadata if needed) using the provided
    /// metadata with the help of the key wrapping provider in the EncryptionSerializer configured on the client via
    /// <see cref="CosmosClientBuilder.WithCustomSerializer"/>, and saves the re-wrapped data encryption key as an asynchronous
    /// operation in the Azure Cosmos service.
    /// </summary>
    /// <param name="id">Unique identifier of the data encryption key.</param>
    /// <param name="newWrapMetadata">The metadata using which the data encryption key needs to now be wrapped.</param>
    /// <param name="requestOptions">(Optional) The options for the request.</param>
    /// <param name="cancellationToken">(Optional) Token representing request cancellation.</param>
    /// <returns>An awaitable response which wraps a <see cref="DataEncryptionKeyProperties"/> containing details of the data encryption key that was re-wrapped.</returns>
    /// <exception cref="CosmosException">
    /// This exception can encapsulate many different types of errors.
    /// To determine the specific error always look at the StatusCode property.
    /// Some common codes you may get when re-wrapping a data encryption key are:
    /// <list type="table">
    ///     <listheader>
    ///         <term>StatusCode</term>
    ///         <description>Reason for exception</description>
    ///     </listheader>
    ///     <item>
    ///         <term>404</term>
    ///         <description>
    ///         NotFound - This means the resource or parent resource you tried to replace did not exist.
    ///         </description>
    ///     </item>
    ///     <item>
    ///         <term>429</term>
    ///         <description>
    ///         TooManyRequests - This means you have exceeded the number of request units per second.
    ///         Consult the CosmosException.RetryAfter value to see how long you should wait before retrying this operation.
    ///         </description>
    ///     </item>
    /// </list>
    /// </exception>
    /// <example>
    /// <code language="c#">
    /// <![CDATA[
    /// AzureKeyVaultKeyWrapMetadata v2Metadata = new AzureKeyVaultKeyWrapMetadata("/path/to/my/master/key/v2");
    /// await key.RewrapAsync(v2Metadata);
    /// ]]>
    /// </code>
    /// </example>

    /**
     *
     * @param id
     * @param newWrapMetadata
     * @param requestOptions
     * @return
     */
    Mono<CosmosItemResponse<DataEncryptionKeyProperties>> rewrapDataEncryptionKeyAsync(
        String id,
        EncryptionKeyWrapMetadata newWrapMetadata,
        CosmosItemRequestOptions requestOptions);

    /**
     * Reads the properties of a data encryption key from the Azure Cosmos service as an asynchronous operation.
     *
     * @param id             Unique identifier of the data encryption key.
     * @param requestOptions (Optional) The options for the request.
     * @return An Mono response which wraps a {@link DataEncryptionKeyProperties} containing details of the data encryption key that was read.
     * <p>
     * on Failure: {@link com.azure.cosmos.CosmosException} indicating the failure reason.
     * This exception can encapsulate many different types of errors.
     * To determine the specific error always look at the StatusCode property.
     * Some common codes you may get when reading a data encryption key are:
     *
     * <ul>
     *   <li>
     *       NotFound - This means the resource or parent resource you tried to read did not exist.
     *   </li>
     *
     *   <li>
     *       TooManyRequests - This means you have exceeded the number of request units per second.
     *      Consult the CosmosException.RetryAfter value to see how long you should wait before retrying this operation.
     *   </li>
     * </ul>
     */
    Mono<CosmosItemResponse<DataEncryptionKeyProperties>> readDataEncryptionKeyAsync(
        String id,
        CosmosItemRequestOptions requestOptions);
}
