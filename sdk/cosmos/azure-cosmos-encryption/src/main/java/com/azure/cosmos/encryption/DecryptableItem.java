// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import reactor.core.publisher.Mono;


/// <summary>
/// Allows for lazy decryption, which provides user a way to handle possible exceptions encountered as part of feed /
// query processing.
/// Also provides decryption operation details.
/// </summary>
/// <example>
/// <code language="c#">
/// <![CDATA[
/// public class ToDoActivity{
///     public string id {get; set;}
///     public string status {get; set;}
///     public int cost {get; set;}
/// }
///
/// QueryDefinition queryDefinition = new QueryDefinition("select * from ToDos");
/// using (FeedIterator<DecrytableItem> feedIterator = this.Container.GetItemQueryIterator<DecrytableItem>(
///     queryDefinition,
///     requestOptions: new QueryRequestOptions() { PartitionKey = new PartitionKey("Error")}))
/// {
///     while (feedIterator.HasMoreResults)
///     {
///         FeedResponse<DecryptableItem> decryptableItems = await feedIterator.ReadNextAsync();
///         foreach(DecryptableItem item in decryptableItems){
///         {
///             try
///             {
///                 (ToDoActivity toDo, DecryptionContext _) = await item.GetItemAsync<ToDoActivity>();
///             }
///             catch (EncryptionException encryptionException)
///             {
///                 string dataEncryptionKeyId = encryptionException.DataEncryptionKeyId;
///                 string rawPayload = encryptionException.EncryptedContent;
///             }
///         }
///     }
/// }
/// ]]>
/// </code>
/// </example>

// TODO: moderakh add example
/**
 * Allows for lazy decryption, which provides user a way to handle possible exceptions encountered as part of feed /
 * query processing.
 * Also provides decryption operation details.
 */
public abstract class DecryptableItem {

    // TODO: moderakh finalize the class name
    public static class DecryptionResult<T> {
        private T result;
        private DecryptionContext context;

        DecryptionResult(T result, DecryptionContext context) {
            this.result = result;
            this.context = context;
        }

        // TODO: moderakh finalize the method name
        public T getDecryptedItem() {
            return result;
        }

        // TODO: moderakh finalize the method name
        public DecryptionContext getContext() {
            return context;
        }
    }

    /**
     * Decrypts and deserializes the content.
     *
     * @param classType The class type of item to be returned.
     * @param <T> The type of item to be returned.
     * @return The requested item and the decryption operation related context
     */
    public abstract <T> Mono<DecryptionResult<T>> getDecryptionResult(Class<T> classType);

    /**
     * Validate that the DecryptableItem is initialized.
     *
     * @param decryptableItem Decryptable item to check.
     */
    protected void validate(DecryptableItem decryptableItem) {
        if (decryptableItem == null) {
            throw new IllegalStateException("Decryptable content is not initialized.");
        }
    }
}
