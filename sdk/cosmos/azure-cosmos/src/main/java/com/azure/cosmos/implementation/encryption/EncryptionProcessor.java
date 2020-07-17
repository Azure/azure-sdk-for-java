// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.api.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class EncryptionProcessor {

    public ObjectNode encryptAsync(
        ObjectNode itemJObj,
        EncryptionOptions encryptionOptions,
        DataEncryptionKeyProvider dataEncryptionKeyProvider) {
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

        if (dataEncryptionKeyProvider == null) {
            throw new IllegalArgumentException(RMResources.EncryptionKeyProviderNotConfigured);
        }

        ObjectNode toEncryptJObj = Utils.getSimpleObjectMapper().createObjectNode();

        for (String pathToEncrypt : encryptionOptions.getPathsToEncrypt()) {
            String propertyName = pathToEncrypt.substring(1);
            JsonNode propertyValueHolder = itemJObj.get(propertyName);

            // Even null in the JSON is a JToken with Type Null, this null check is just a sanity check
            if (propertyValueHolder != null) {
                toEncryptJObj.set(propertyName, itemJObj.get(propertyName));
                itemJObj.remove(propertyName);
            }
        }

        SensitiveDataTransformer serializer = new SensitiveDataTransformer();
        byte[] plainText = serializer.toByteArray(toEncryptJObj);

        DataEncryptionKey dataEncryptionKey = dataEncryptionKeyProvider.getDataEncryptionKey(encryptionOptions.getDataEncryptionKeyId(), encryptionOptions.getEncryptionAlgorithm());

        EncryptionProperties encryptionProperties = new EncryptionProperties(
            /* encryptionFormatVersion: */ 2,
            encryptionOptions.getEncryptionAlgorithm(),
            encryptionOptions.getDataEncryptionKeyId(),
            dataEncryptionKey.encryptData(plainText));

        itemJObj.set(Constants.Properties.EncryptedInfo, encryptionProperties.toObjectNode());

        return itemJObj;
    }


    public ObjectNode decryptAsync(
        ObjectNode itemJObj,
        DataEncryptionKeyProvider keyProvider) {
        assert (itemJObj != null);
        assert (keyProvider != null);

        JsonNode encryptionPropertiesJProp = itemJObj.get(Constants.Properties.EncryptedInfo);
        ObjectNode encryptionPropertiesJObj = null;
        if (encryptionPropertiesJProp != null && !encryptionPropertiesJProp.isNull() && encryptionPropertiesJProp.getNodeType() == JsonNodeType.OBJECT) {
            encryptionPropertiesJObj = (ObjectNode) encryptionPropertiesJProp;
        }

        if (encryptionPropertiesJProp == null) {
            return itemJObj;
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

        // get key
        DataEncryptionKey inMemoryRawDek = keyProvider.getDataEncryptionKey(encryptionProperties.getDataEncryptionKeyId(), CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized);

        byte[] plainText = inMemoryRawDek.decryptData(encryptionProperties.getEncryptedData());

        SensitiveDataTransformer parser = new SensitiveDataTransformer();
        ObjectNode plainTextJObj = parser.toObjectNode(plainText);

        Iterator<Map.Entry<String, JsonNode>> it = plainTextJObj.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            itemJObj.set(entry.getKey(), entry.getValue());
        }

        itemJObj.remove(Constants.Properties.EncryptedInfo);
        return itemJObj;
    }

    static class SensitiveDataTransformer {
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
