// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.CosmosEncryptionType;
import com.azure.cosmos.encryption.DecryptionContext;
import com.azure.cosmos.encryption.DecryptionInfo;
import com.azure.cosmos.encryption.EncryptionAsyncCosmosClient;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava27.Strings;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.EncryptionType;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import com.microsoft.data.encryption.cryptography.ProtectedDataEncryptionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EncryptionProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptionProcessor.class);
    private EncryptionAsyncCosmosClient encryptionCosmosClient;
    private EncryptionCosmosAsyncContainer encryptionCosmosContainer;
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;
    private EncryptionSettings encryptionSettings;
    private boolean isEncryptionSettingsInitDone;
    private ClientEncryptionPolicy clientEncryptionPolicy;

    public EncryptionProcessor(EncryptionCosmosAsyncContainer encryptionCosmosContainer,
                               EncryptionAsyncCosmosClient encryptionCosmosClient) {

        if (encryptionCosmosContainer == null) {
            throw new IllegalStateException("encryptionCosmosContainer is null");
        }

        if (encryptionCosmosClient == null) {
            throw new IllegalStateException("encryptionCosmosClient is null");
        }
        this.encryptionCosmosContainer = encryptionCosmosContainer;
        this.encryptionCosmosClient = encryptionCosmosClient;
        this.isEncryptionSettingsInitDone = false;
    }

    /**
     * Builds up and caches the Encryption Setting by getting the cached entries of Client Encryption Policy and the
     * corresponding keys.
     * Sets up the MDE Algorithm for encryption and decryption by initializing the KeyEncryptionKey and
     * ProtectedDataEncryptionKey.
     *
     * @return Mono
     */
    Mono<Void> initializeEncryptionSettingsAsync() {
         // update the property level setting.
        if (this.isEncryptionSettingsInitDone) {
            throw new IllegalStateException("The Encrypton Processor has already been initialized. ");
        }
        Map<String, EncryptionSettings> settingsByDekId = new HashMap<>();
        return this.encryptionCosmosClient.getClientEncryptionPolicyAsync(this.encryptionCosmosContainer, false).flatMap(clientEncryptionPolicy ->
        {
            if (clientEncryptionPolicy == null) {
                this.isEncryptionSettingsInitDone = true;
                return Mono.empty();
            }
            this.clientEncryptionPolicy = clientEncryptionPolicy;
            this.clientEncryptionPolicy.getIncludedPaths().stream().map(clientEncryptionIncludedPath -> clientEncryptionIncludedPath.clientEncryptionKeyId).distinct().forEach(clientEncryptionKeyId -> {
                AtomicBoolean forceRefreshClientEncryptionKey = new AtomicBoolean(false);
                this.encryptionCosmosClient.getClientEncryptionPropertiesAsync(clientEncryptionKeyId,
                    this.encryptionCosmosContainer, forceRefreshClientEncryptionKey.get()).flatMap(keyProperties -> {
                    ProtectedDataEncryptionKey protectedDataEncryptionKey;
                    try {
                        // we pull out the Encrypted Client Encryption Key and Build the Protected Data Encryption key
                        // Here a request is sent out to unwrap using the Master Key configured via the Key Encryption Key.
                        protectedDataEncryptionKey =
                            this.encryptionSettings.buildProtectedDataEncryptionKey(keyProperties,
                                this.encryptionKeyStoreProvider,
                                clientEncryptionKeyId);
                    } catch (Exception ex) {
                        return Mono.error(ex);
                    }
                    EncryptionSettings encryptionSettings = new EncryptionSettings();
                    encryptionSettings.setEncryptionSettingTimeToLive(Instant.now().plus(Duration.ofMinutes(com.azure.cosmos.implementation.encryption.Constants.CACHED_ENCRYPTION_SETTING_DEFAULT_DEFAULT_TTL_IN_MINUTES)));
                    encryptionSettings.setClientEncryptionKeyId(clientEncryptionKeyId);
                    encryptionSettings.setDataEncryptionKey(protectedDataEncryptionKey);
                    settingsByDekId.put(clientEncryptionKeyId, encryptionSettings);
                    return Mono.empty();
                }).retryWhen(Retry.withThrowable((throwableFlux -> throwableFlux.flatMap(throwable -> {
                    //TODO DO we need to check for MicrosoftDataEncryptionException too ?
                    // ProtectedDataEncryptionKey.getOrCreate throws Exception object and not specific
                    // exceptions

                    // The access to master key was revoked. Try to fetch the latest ClientEncryptionKeyProperties from the backend.
                    // This will succeed provided the user has rewraped the Client Encryption Key with right set of meta data.
                    // This is based on the AKV provider implementaion so we expect a RequestFailedException in case other providers are used in unwrap implementation.
                    InvalidKeyException invalidKeyException = Utils.as(throwable, InvalidKeyException.class);
                    if (invalidKeyException != null) {
                        forceRefreshClientEncryptionKey.set(true);
                        return Mono.delay(Duration.ZERO).flux();
                    }
                    return Flux.error(throwable);
                }))));
            });
            return Mono.empty();
        }).flatMap(ignoreVoid -> {
            for (ClientEncryptionIncludedPath propertyToEncrypt : clientEncryptionPolicy.getIncludedPaths()) {
                EncryptionType encryptionType = EncryptionType.Plaintext;
                switch (propertyToEncrypt.encryptionType) {
                    case CosmosEncryptionType.DETERMINISTIC:
                        encryptionType = EncryptionType.Deterministic;
                        break;
                    case CosmosEncryptionType.RANDOMIZED:
                        encryptionType = EncryptionType.Randomized;
                        break;
                    default:
                        LOGGER.debug("Invalid encryption type {}", propertyToEncrypt.encryptionType);
                        break;
                }
                String propertyName = propertyToEncrypt.path.substring(1);
                try {
                    this.encryptionSettings.setEncryptionSettingForProperty(propertyName,
                        EncryptionSettings.create(settingsByDekId.get(propertyToEncrypt.clientEncryptionKeyId),
                            encryptionType),
                        settingsByDekId.get(propertyToEncrypt.clientEncryptionKeyId).getEncryptionSettingTimeToLive());
                } catch (MicrosoftDataEncryptionException ex) {
                    return Mono.error(ex);
                }
            }
            this.isEncryptionSettingsInitDone = true;
            return Mono.empty();
        });
    }

    Mono<Void> initEncryptionSettingsIfNotInitializedAsync() {
        if (!this.isEncryptionSettingsInitDone) {
            return initializeEncryptionSettingsAsync();
        }
        return Mono.empty();
    }

    ClientEncryptionPolicy getClientEncryptionPolicy() {
        return clientEncryptionPolicy;
    }

    private void setClientEncryptionPolicy(ClientEncryptionPolicy clientEncryptionPolicy) {
        this.clientEncryptionPolicy = clientEncryptionPolicy;
    }

    /**
     * Gets the container that has items which are to be encrypted.
     *
     * @return the encryptionCosmosContainer
     */
    public EncryptionCosmosAsyncContainer getEncryptionCosmosContainer() {
        return this.encryptionCosmosContainer;
    }

    /**
     * Gets the encrypted cosmos client.
     *
     * @return encryptionCosmosClient
     */
    public EncryptionAsyncCosmosClient getEncryptionCosmosClient() {
        return encryptionCosmosClient;
    }

    /**
     * Gets the provider that allows interaction with the master keys.
     *
     * @return encryptionKeyStoreProvider
     */
    public EncryptionKeyStoreProvider getEncryptionKeyStoreProvider() {
        return encryptionKeyStoreProvider;
    }

    EncryptionSettings getEncryptionSettings() {
        return encryptionSettings;
    }

    public Mono<byte[]> encrypt(byte[] payload) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Encrypting byte[] of size [{}] on thread [{}]",
                payload == null ? null : payload.length,
                Thread.currentThread().getName());
        }
        ObjectNode itemJObj = Utils.parse(payload, ObjectNode.class);

        assert (itemJObj != null);

        for (ClientEncryptionIncludedPath includedPath : this.clientEncryptionPolicy.getIncludedPaths()) {
            if (StringUtils.isEmpty(includedPath.path) || includedPath.path.charAt(0) != '/' || includedPath.path.lastIndexOf('/') != 0) {
                return Mono.error(new IllegalArgumentException("Invalid encryption path: " + includedPath.path));
            }
        }
        List<Mono<Void>> encryptionMonoList = new ArrayList<>();
        for (ClientEncryptionIncludedPath includedPath : this.clientEncryptionPolicy.getIncludedPaths()) {
            String propertyName = includedPath.path.substring(1);
            // TODO: moderakh should support JPath
            JsonNode propertyValueHolder = itemJObj.get(propertyName);

            // Even null in the JSON is a JToken with Type Null, this null check is just a sanity check
            if (propertyValueHolder != null) {
                itemJObj.remove(propertyName);
                SensitiveDataTransformer serializer = new SensitiveDataTransformer();
                byte[] plainText = serializer.toByteArray(propertyValueHolder);
                Mono<Void> voidMono = this.encryptionSettings.getEncryptionSettingForPropertyAsync(propertyName,
                    this).flatMap(settings -> {
                    byte[] cipherTextMono = new byte[0];
                    try {
                        cipherTextMono = settings.getAeadAes256CbcHmac256EncryptionAlgorithm().encrypt(plainText);
                        itemJObj.set(propertyName, serializer.toJsonNode(cipherTextMono));
                    } catch (MicrosoftDataEncryptionException ex) {
                        return Mono.error(ex);
                    }
                    itemJObj.set(propertyName, serializer.toJsonNode(cipherTextMono));
                    return Mono.empty();
                });
                encryptionMonoList.add(voidMono);
            }
        }
        Mono<List<Void>> listMono= Flux.mergeSequential(encryptionMonoList).collectList();
        return listMono.flatMap(aVoid -> Mono.just(EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), itemJObj)));
    }

    public static Mono<Pair<byte[], DecryptionContext>> decrypt(byte[] input, Encryptor encryptor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Decrypting byte[] of size [{}] on thread [{}]",
                input == null ? null : input.length,
                Thread.currentThread().getName());
        }

        JsonNode itemJObj = Utils.parse(input, JsonNode.class);
        if (itemJObj instanceof ObjectNode) {
            Mono<Pair<ObjectNode, DecryptionContext>> itemJObjMono = decrypt((ObjectNode) itemJObj, encryptor);
            return itemJObjMono.flatMap(
                decryptedItemWithContext -> {
                    return Mono.just(Pair.of(EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(),
                        decryptedItemWithContext.getLeft()), decryptedItemWithContext.getRight()));
                }
            );
        } else {
            return Mono.just(Pair.of(input, null));
        }
    }

    public static Mono<Pair<ObjectNode, DecryptionContext>> decrypt(ObjectNode itemJObj, Encryptor encryptor) {
        try {
            return decryptInternal(itemJObj, encryptor);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private static Mono<Pair<ObjectNode, DecryptionContext>> decryptInternal(ObjectNode itemJObj, Encryptor encryptor) {
        assert (itemJObj != null);
        assert (encryptor != null);

        JsonNode encryptionPropertiesJProp = itemJObj.get(Constants.Properties.EncryptedInfo);
        ObjectNode encryptionPropertiesJObj = null;
        if (encryptionPropertiesJProp != null && !encryptionPropertiesJProp.isNull() && encryptionPropertiesJProp.getNodeType() == JsonNodeType.OBJECT) {
            encryptionPropertiesJObj = (ObjectNode) encryptionPropertiesJProp;
        }

        if (encryptionPropertiesJProp == null) {
            return Mono.just(Pair.of(itemJObj, null));
        }

        EncryptionProperties encryptionProperties = null;
        try {
            encryptionProperties = EncryptionProperties.fromObjectNode(encryptionPropertiesJObj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        if (encryptionProperties.getEncryptionFormatVersion() != 2) {
            throw new InternalServerErrorException(Strings.lenientFormat(
                "Unknown encryption format version: %s. Please upgrade your SDK to the latest version.",
                encryptionProperties.getEncryptionFormatVersion()));
        }

        final EncryptionProperties encryptionPropertiesFinal = encryptionProperties;

        Mono<byte[]> plainTextMono = encryptor.decrypt(encryptionProperties.getEncryptedData(),
            encryptionProperties.getDataEncryptionKeyId(), encryptionProperties.getEncryptionAlgorithm());

        return plainTextMono.flatMap(
            plainText -> {

                SensitiveDataTransformer parser = new SensitiveDataTransformer();
                ObjectNode plainTextJObj = parser.toObjectNode(plainText);

                List<String> pathsDecrypted = new ArrayList<>();
                Iterator<Map.Entry<String, JsonNode>> it = plainTextJObj.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    itemJObj.set(entry.getKey(), entry.getValue());
                    pathsDecrypted.add("/" + entry.getKey());
                }

                DecryptionInfo decryptionInfo = new DecryptionInfo(pathsDecrypted,
                    encryptionPropertiesFinal.getDataEncryptionKeyId());
                DecryptionContext decryptionContext = new DecryptionContext(ImmutableList.of(decryptionInfo));

                itemJObj.remove(Constants.Properties.EncryptedInfo);
                return Mono.just(Pair.of(itemJObj, decryptionContext));
            }
        );
    }

    private static class SensitiveDataTransformer {
        public <T> JsonNode toJsonNode(byte[] plainText) {
            if (Utils.isEmpty(plainText)) {
                return null;
            }
            try {
                return (JsonNode) Utils.getSimpleObjectMapper().readTree(plainText);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse to JsonNode.", e);
            }
        }

        public byte[] toByteArray(JsonNode jsonNode) {
            try {
                return Utils.getSimpleObjectMapper().writeValueAsBytes(jsonNode);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to convert JSON to byte[]", e);
            }
        }
    }
}
