// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class EncryptionProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptionProcessor.class);

    public static Mono<byte[]> encryptAsync(byte[] payload, Encryptor encryptor, EncryptionOptions encryptionOptions) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Encrypting byte[] of size [{}] on thread [{}]",
                payload == null ? null : payload.length,
                Thread.currentThread().getName());
        }

        ObjectNode itemJObj = Utils.parse(payload, ObjectNode.class);

        assert (itemJObj != null);
        assert (encryptionOptions != null);
        assert (encryptionOptions.getPathsToEncrypt() != null);
        assert (!encryptionOptions.getPathsToEncrypt().isEmpty());

        for (String path : encryptionOptions.getPathsToEncrypt()) {
            if (StringUtils.isEmpty(path) || path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
                throw new IllegalArgumentException("Invalid encryption path: " + path);
            }
        }

        if (encryptionOptions.getDataEncryptionKeyId() == null) {
            throw new IllegalArgumentException("Invalid encryption options: encryptionOptions.getDataEncryptionKeyId." + encryptionOptions.getDataEncryptionKeyId());
        }

        if (encryptionOptions.getEncryptionAlgorithm() == null) {
            throw new IllegalArgumentException("Invalid encryption options: encryptionOptions.getEncryptionAlgorithm." + encryptionOptions.getEncryptionAlgorithm());
        }

        ObjectNode toEncryptJObj = Utils.getSimpleObjectMapper().createObjectNode();

        for (String pathToEncrypt : encryptionOptions.getPathsToEncrypt()) {
            String propertyName = pathToEncrypt.substring(1);
            // TODO: moderakh should support JPath
            JsonNode propertyValueHolder = itemJObj.get(propertyName);

            // Even null in the JSON is a JToken with Type Null, this null check is just a sanity check
            if (propertyValueHolder != null) {
                toEncryptJObj.set(propertyName, itemJObj.get(propertyName));
                itemJObj.remove(propertyName);
            }
        }

        SensitiveDataTransformer serializer = new SensitiveDataTransformer();
        byte[] plainText = serializer.toByteArray(toEncryptJObj);

        Mono<byte[]> cipherTextMono = encryptor.encryptAsync(plainText, encryptionOptions.getDataEncryptionKeyId(), encryptionOptions.getEncryptionAlgorithm());

        return cipherTextMono.switchIfEmpty(Mono.error(new NullPointerException("cipherText is null")))
                             .flatMap(
                                 cipherText -> {

                                     EncryptionProperties encryptionProperties = new EncryptionProperties(
                                         /* encryptionFormatVersion: */ 2,
                                         encryptionOptions.getEncryptionAlgorithm(),
                                         encryptionOptions.getDataEncryptionKeyId(),
                                         cipherText);

                                     itemJObj.set(Constants.Properties.EncryptedInfo,
                                         encryptionProperties.toObjectNode());

                                     // TODO:             return EncryptionProcessor.BaseSerializer.ToStream(itemJObj);
                                     return Mono.just(EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), itemJObj));
                                 }
                             );
    }

    public static Mono<byte[]> decryptAsync(byte[] input, Encryptor encryptor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Decrypting byte[] of size [{}] on thread [{}]",
                input == null ? null : input.length,
                Thread.currentThread().getName());
        }

        JsonNode itemJObj = Utils.parse(input, JsonNode.class);
        if (itemJObj instanceof ObjectNode) {
            Mono<ObjectNode> itemJObjMono = decryptAsync((ObjectNode) itemJObj, encryptor);
            return itemJObjMono.flatMap(
                decryptedItem -> {
                    return Mono.just(EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), itemJObj));
                }
            );
        } else {
            return Mono.just(input);
        }
    }

    public static Mono<ObjectNode> decryptAsync(ObjectNode itemJObj, Encryptor encryptor) {
        try {
            return decryptAsyncInternal(itemJObj, encryptor);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private static Mono<ObjectNode> decryptAsyncInternal(ObjectNode itemJObj, Encryptor encryptor) {
        assert (itemJObj != null);
        assert (encryptor != null);

        JsonNode encryptionPropertiesJProp = itemJObj.get(Constants.Properties.EncryptedInfo);
        ObjectNode encryptionPropertiesJObj = null;
        if (encryptionPropertiesJProp != null && !encryptionPropertiesJProp.isNull() && encryptionPropertiesJProp.getNodeType() == JsonNodeType.OBJECT) {
            encryptionPropertiesJObj = (ObjectNode) encryptionPropertiesJProp;
        }

        if (encryptionPropertiesJProp == null) {
            return Mono.just(itemJObj);
        }

        EncryptionProperties encryptionProperties = null;
        try {
            encryptionProperties = EncryptionProperties.fromObjectNode(encryptionPropertiesJObj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        if (encryptionProperties.getEncryptionFormatVersion() != 2) {
            throw new InternalServerErrorException(Strings.lenientFormat(
                "Unknown encryption format version: %s. Please upgrade your SDK to the latest version.", encryptionProperties.getEncryptionFormatVersion()));
        }

        Mono<byte[]> plainTextMono = encryptor.decryptAsync(encryptionProperties.getEncryptedData(), encryptionProperties.getDataEncryptionKeyId(), encryptionProperties.getEncryptionAlgorithm());

        return plainTextMono.flatMap(
            plainText -> {

                SensitiveDataTransformer parser = new SensitiveDataTransformer();
                ObjectNode plainTextJObj = parser.toObjectNode(plainText);

                Iterator<Map.Entry<String, JsonNode>> it = plainTextJObj.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    itemJObj.set(entry.getKey(), entry.getValue());
                }

                itemJObj.remove(Constants.Properties.EncryptedInfo);
                return Mono.just(itemJObj);
            }
        );
    }

    private static class SensitiveDataTransformer {
        public <T> ObjectNode toObjectNode(byte[] plainText) {
            if (Utils.isEmpty(plainText)) {
                return null;
            }
            try {
                return (ObjectNode) Utils.getSimpleObjectMapper().readTree(plainText);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse to ObjectNode.", e);
            }
        }

        public byte[] toByteArray(ObjectNode objectNode) {
            try {
                return Utils.getSimpleObjectMapper().writeValueAsBytes(objectNode);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to convert JSON to byte[]", e);
            }
        }
    }
}
