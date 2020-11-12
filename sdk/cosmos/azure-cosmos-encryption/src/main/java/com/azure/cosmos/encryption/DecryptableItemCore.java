// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.encryption.Constants;
import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

class DecryptableItemCore extends DecryptableItem {

    /**
     * The encrypted content which is yet to be decrypted.
     */
    private final JsonNode decryptableContent;
    private final Encryptor encryptor;
    private final ItemDeserializer cosmosSerializer;

    public DecryptableItemCore(JsonNode decryptableContent,
                               Encryptor encryptor,
                               ItemDeserializer cosmosSerializer) {
        Preconditions.checkNotNull(decryptableContent, "decryptableContent");
        Preconditions.checkNotNull(encryptor, "encryptor");
        Preconditions.checkNotNull(cosmosSerializer, "cosmosSerializer");

        this.decryptableContent = decryptableContent;
        this.encryptor = encryptor;
        this.cosmosSerializer = cosmosSerializer;
    }

    @Override
    public <T> Mono<DecryptionResult<T>> getDecryptionResult(final Class<T> classType) {

        ObjectNode decryptableContentAsObjectNode = Utils.as(this.decryptableContent, ObjectNode.class);
        if (decryptableContentAsObjectNode == null) {
            // (this.cosmosSerializer.FromStream<T>(EncryptionProcessor.BaseSerializer.ToStream
            return Mono.just(new DecryptionResult<>(Utils.getSimpleObjectMapper().convertValue(this.decryptableContent, classType), null));
        }

        Mono<Pair<ObjectNode, DecryptionContext>> decryptedItemAndContextPairMono =
            EncryptionProcessor.decrypt(decryptableContentAsObjectNode, this.encryptor);

        return decryptedItemAndContextPairMono.map(
            decryptedItemAndContextPair -> {
                T ii = this.cosmosSerializer.convert(classType, decryptedItemAndContextPair.getLeft());
                return new DecryptionResult<>(ii, decryptedItemAndContextPair.getRight());
            }
        ).onErrorResume(
            t -> {
                Exception exception = Utils.as(t, Exception.class);
                if (exception == null) {
                    return Mono.error(t);
                }

                JsonNode encryptedInfo = decryptableContentAsObjectNode.get(Constants.ENCRYPTION_INFO);
                ObjectNode encryptedInfoObject = Utils.as(encryptedInfo, ObjectNode.class);

                String dataEncryptionKeyId = (encryptedInfoObject != null) ?
                    encryptedInfoObject.get(Constants.DATA_ENCRYPTION_KEY_ID).asText() : null;

                exception = new EncryptionException(
                    dataEncryptionKeyId,
                    this.decryptableContent.toString(),
                    exception);

                return Mono.error(exception);
            }
        );
    }
}


