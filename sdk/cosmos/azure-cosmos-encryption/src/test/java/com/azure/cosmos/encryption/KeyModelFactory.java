// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Test Utils factory class for mocking Keys.
 */
public class KeyModelFactory {
    public static class KeyVaultKeyBuilder {
        private static ObjectMapper objectMapper = new ObjectMapper();

        public static KeyVaultKey createInstance() {
            try {
                return objectMapper.readValue("{}", KeyVaultKey.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        private final KeyVaultKey keyVaultKey;

        public KeyVaultKeyBuilder() {
            this.keyVaultKey = createInstance();
            ReflectionUtils.set(this.keyVaultKey, new JsonWebKey(), "key");
        }

        public KeyVaultKeyBuilder withKeyProperties(Function<KeyProperties, KeyPropertiesBuilder> func) {
            func.apply(keyVaultKey.getProperties());
            return this;
        }

        public KeyVaultKeyBuilder withJsonWebKeyBuilder(Function<JsonWebKey, JsonWebKeyBuilder> func) {


            func.apply(keyVaultKey.getKey());
            return this;
        }

        public KeyVaultKey toKeyVaultKey() {
            return keyVaultKey;
        }
    }

    public static class KeyPropertiesBuilder {
        private final KeyProperties keyProperties;

        public KeyPropertiesBuilder() {
            this(new KeyProperties());
        }

        public KeyPropertiesBuilder(KeyProperties keyProperties) {
            this.keyProperties = keyProperties;
        }

        public KeyPropertiesBuilder withRecoveryLevel(String recoveryLevel) {

            ReflectionUtils.set(keyProperties, recoveryLevel, "recoveryLevel");
            assert StringUtils.equals(keyProperties.getRecoveryLevel(), recoveryLevel);

            return this;
        }

        public <T> KeyPropertiesBuilder withKeyValue(KeyProperties keyProperties, String key, T value) {
            ReflectionUtils.set(keyProperties, value, key);
            return this;
        }

        public KeyProperties toKeyProperties() {
            return keyProperties;
        }
    }

    public static class JsonWebKeyBuilder {
        private final JsonWebKey jsonWebKey;

        public JsonWebKey toJsonWebKey() {
            return jsonWebKey;
        }

        public JsonWebKeyBuilder() {
            this(new JsonWebKey());
        }

        public JsonWebKeyBuilder(JsonWebKey jsonWebKey) {
            this.jsonWebKey = jsonWebKey;
        }

        public JsonWebKeyBuilder withKeyType(KeyType keyType) {
            ReflectionUtils.set(jsonWebKey, keyType, "keyType");
            assert jsonWebKey.getKeyType().equals(keyType);

            return this;
        }

        public JsonWebKeyBuilder withKeyOps(List<KeyOperation> keyOps) {
            ReflectionUtils.set(jsonWebKey, keyOps, "keyOps");
            assert jsonWebKey.getKeyOps().equals(keyOps);
            return this;
        }
    }
}
