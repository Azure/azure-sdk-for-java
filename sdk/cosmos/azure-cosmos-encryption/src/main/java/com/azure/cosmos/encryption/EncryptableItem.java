// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;

/// <summary>
/// Input type that can be used to allow for lazy decryption in the write path.
/// </summary>
/// <typeparam name="T">Type of item.</typeparam>
/// <example>
/// This example takes in a item, encrypts it and writes to Cosmos container.
/// <code language="c#">
/// <![CDATA[
/// public class ToDoActivity{
///     public string id {get; set;}
///     public string status {get; set;}
/// }
///
/// ToDoActivity test = new ToDoActivity()
/// {
///    id = Guid.NewGuid().ToString(),
///    status = "InProgress"
/// };
///
/// ItemResponse<EncryptableItem<ToDoActivity>> createResponse = await encryptionContainer
// .CreateItemAsync<EncryptableItem<ToDoActivity>>(
///     new EncryptableItem<ToDoActivity>(test),
///     new PartitionKey(test.Status),
///     EncryptionItemRequestOptions);
///
/// if (!createResponse.IsSuccessStatusCode)
/// {
///     //Handle and log exception
///     return;
/// }
///
/// (ToDoActivity toDo, DecryptionContext _) = await item.DecryptableItem.GetItemAsync<ToDoActivity>();
/// ]]>
/// </code>
/// </example>

// TODO moderakh add example

/**
 * Input type that can be used to allow for lazy decryption in the write path.
 * @param <T> Type of item.
 */
public class EncryptableItem<T> extends AbstractEncryptableItem {

    private T item;

    /**
     * Gets the input item.
     * @return the input item.
     */
    public T getItem() {
        return this.item;
    }

    @Override
    public DecryptableItem getDecryptableItem() {
        if (this.decryptableItem == null) {
            throw new IllegalStateException("Decryptable content is not initialized.");
        }

        return this.decryptableItem;
    }

    /**
     * Initializes a new instance of the {@link EncryptableItem}
     * @param input Item to be written
     */
    public EncryptableItem(T input) {
        Preconditions.checkNotNull(input, "input");
        this.item = input;
    }

    @Override
    protected byte[] toStream(ItemDeserializer serializer) {
        // TODO: we should wire this up through serializer
        return EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), item);
    }
}
