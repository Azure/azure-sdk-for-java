// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactoryCore;
import com.azure.cosmos.implementation.encryption.DecryptionResult;
import com.azure.cosmos.implementation.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.implementation.encryption.Encryptor;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.EncryptionBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
public class EncryptionCosmosAsyncContainer extends CosmosAsyncContainer {
    private final Encryptor encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();

    EncryptionCosmosAsyncContainer(String id, CosmosAsyncDatabase database, Encryptor encryptor) {
        super(id, database);
        this.encryptor = encryptor;
    }

    private Mono<CosmosItemResponse<byte[]>> createItemStream(byte[] payload,
                                                              PartitionKey partitionKey,
                                                              CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions, EncryptionItemRequestOptions.class);
        if (encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null) {

            payload = EncryptionProcessor.encryptAsync(payload, encryptor, encryptionItemRequestOptions.getEncryptionOptions());

            Mono<CosmosItemResponse<byte[]>> response = super.createItem(payload, partitionKey, requestOptions);

            return response
                .subscribeOn(Schedulers.elastic())
                .publishOn(Schedulers.elastic())
                .map(rsp -> {
                    EncryptionBridgeInternal.setByteArrayContent(rsp, decryptResponseAsync(EncryptionBridgeInternal.getByteArrayContent(rsp), encryptionItemRequestOptions.getDecryptionResultHandler()));
                    return rsp;
                }
            );

        } else {
            return null;
            // TODO
            // return super.createItem()
        }
    }

    // TODO ensure all other apis call this guy
    public <T> Mono<CosmosItemResponse<T>> createItem(T item,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions option) {
        Preconditions.checkNotNull(item, "item");
        if (option == null) {
            option = new CosmosItemRequestOptions();
        }

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(option, EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null) {
            Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using EncryptionContainer.");


            byte[] payload = cosmosSerializerToStream(item);

            String payloadAsString = new String(payload);


            Mono<CosmosItemResponse<byte[]>> result = this.createItemStream(payload, partitionKey, option);


            return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp, item.getClass()));

        } else {
            return super.createItem(item, partitionKey, option);
        }
    }

    private Mono<CosmosItemResponse<byte[]>> readItemStream(String id,
                                                              PartitionKey partitionKey,
                                                              CosmosItemRequestOptions requestOptions) {


        Mono<CosmosItemResponse<byte[]>> responseMessageAsync = super.readItem(id, partitionKey, requestOptions, byte[].class);

        return responseMessageAsync
            .publishOn(Schedulers.elastic())
            .subscribeOn(Schedulers.elastic())
            .map(
            responseMessage ->

            {
                Consumer<DecryptionResult> decryptionErroHandler = null;
                EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions, EncryptionItemRequestOptions.class);

                if (encryptionItemRequestOptions != null)
                {
                    decryptionErroHandler = encryptionItemRequestOptions.getDecryptionResultHandler();
                }

                EncryptionBridgeInternal.setByteArrayContent(responseMessage, this.decryptResponseAsync(
                    EncryptionBridgeInternal.getByteArrayContent(responseMessage), decryptionErroHandler));

                return responseMessage;

            }
        );

    }

    @Override
    public <T> Mono<CosmosItemResponse<T>> readItem(String id,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions option,
                                                        Class<T> classType) {
        Mono<CosmosItemResponse<byte[]>> asyncResult = readItemStream(id, partitionKey, option);

        return asyncResult.map(
            responseMessage -> {

                return this.responseFactory.createItemResponse(responseMessage, classType);
            }
        );
    }

    private <T> byte[] cosmosSerializerToStream(T item) {
        // TODO:
        return Utils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), item);
    }


    ItemDeserializer getItemDeserializer() {
        return getDatabase().getDocClientWrapper().getItemDeserializer();
    }

    private byte[] decryptResponseAsync(
        byte[] input,
        Consumer<DecryptionResult> decryptionResultHandler) {

        if (input == null) {
            return null;
        }

        try {
            return EncryptionProcessor.DecryptAsync(
                input,
                this.encryptor);
        } catch (Exception exception) {
            if (decryptionResultHandler == null) {
                throw exception;
            }

            decryptionResultHandler.accept(DecryptionResult.createFailure(
                input,
                exception));

            decryptionResultHandler.accept(
                DecryptionResult.createFailure(
                    input,
                    exception));

            return input;
        }
    }

}
