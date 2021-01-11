// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.keyvault.models.Key.DefinitionStages.WithKey;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure Key Vault key. */
@Fluent
public interface Key extends Indexable, HasInnerModel<KeyProperties>, HasId, HasName, Updatable<Key.Update> {
    /** @return the Json web key. */
    JsonWebKey getJsonWebKey();

    /** @return the Json web key. */
    Mono<JsonWebKey> getJsonWebKeyAsync();

    /** @return the key management attributes. */
    KeyProperties attributes();

    /** @return application specific metadata in the form of key-value pairs. */
    Map<String, String> tags();

    /**
     * @return true if the key's lifetime is managed by key vault. If this is a key backing a certificate, then managed
     *     will be true.
     */
    boolean managed();

    /** @return a list of individual key versions with the same key name */
    PagedIterable<Key> listVersions();

    /** @return a list of individual key versions with the same key name */
    PagedFlux<Key> listVersionsAsync();

    /** @return a backup of the specified key be downloaded to the client */
    byte[] backup();

    /** @return a backup of the specified key be downloaded to the client */
    Mono<byte[]> backupAsync();

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param algorithm the JWK encryption algorithm
     * @param content the content to be encrypted
     * @return the encrypted value
     */
    byte[] encrypt(EncryptionAlgorithm algorithm, byte[] content);

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param algorithm the JWK encryption algorithm
     * @param content the content to be encrypted
     * @return the encrypted value
     */
    Mono<byte[]> encryptAsync(EncryptionAlgorithm algorithm, byte[] content);

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param algorithm the JWK encryption algorithm
     * @param content the content to be decrypted
     * @return the decrypted value
     */
    byte[] decrypt(EncryptionAlgorithm algorithm, byte[] content);

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param algorithm the JWK encryption algorithm
     * @param content the content to be decrypted
     * @return the decrypted value
     */
    Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] content);

    /**
     * Creates a signature from a digest.
     *
     * @param algorithm the JWK signing algorithm
     * @param digest the content to be signed
     * @return the signature in a byte array
     */
    byte[] sign(SignatureAlgorithm algorithm, byte[] digest);

    /**
     * Creates a signature from a digest.
     *
     * @param algorithm the JWK signing algorithm
     * @param digest the content to be signed
     * @return the signature in a byte array
     */
    Mono<byte[]> signAsync(SignatureAlgorithm algorithm, byte[] digest);

    /**
     * Verifies a signature from a digest.
     *
     * @param algorithm the JWK signing algorithm
     * @param digest the content to be signed
     * @param signature the signature to verify
     * @return true if the signature is valid
     */
    boolean verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature);

    /**
     * Verifies a signature from a digest.
     *
     * @param algorithm the JWK signing algorithm
     * @param digest the content to be signed
     * @param signature the signature to verify
     * @return true if the signature is valid
     */
    Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature);

    /**
     * Wraps a symmetric key using the specified algorithm.
     *
     * @param algorithm the JWK encryption algorithm
     * @param key the symmetric key to wrap
     * @return the wrapped key
     */
    byte[] wrapKey(KeyWrapAlgorithm algorithm, byte[] key);

    /**
     * Wraps a symmetric key using the specified algorithm.
     *
     * @param algorithm the JWK encryption algorithm
     * @param key the symmetric key to wrap
     * @return the wrapped key
     */
    Mono<byte[]> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key);

    /**
     * Unwraps a symmetric key wrapped originally by this Key Vault key.
     *
     * @param algorithm the JWK encryption algorithm
     * @param key the key to unwrap
     * @return the unwrapped symmetric key
     */
    byte[] unwrapKey(KeyWrapAlgorithm algorithm, byte[] key);

    /**
     * Unwraps a symmetric key wrapped originally by this Key Vault key.
     *
     * @param algorithm the JWK encryption algorithm
     * @param key the key to unwrap
     * @return the unwrapped symmetric key
     */
    Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key);

    /** Container interface for all the definitions. */
    interface Definition
        extends DefinitionStages.Blank, WithKey, DefinitionStages.WithImport, DefinitionStages.WithCreate {
    }

    /** Grouping of key definition stages. */
    interface DefinitionStages {
        /** The first stage of a key definition. */
        interface Blank extends WithKey {
        }

        /** The stage of a key definition allowing to specify whether to create a key or to import a key. */
        interface WithKey {
            /**
             * Specifies a key type to create a new key.
             *
             * @param keyType the JWK type to create
             * @return the next stage of the definition
             */
            WithCreate withKeyTypeToCreate(KeyType keyType);

            /**
             * Specifies an existing key to import.
             *
             * @param key the existing JWK to import
             * @return the next stage of the definition
             */
            WithImport withLocalKeyToImport(JsonWebKey key);
        }

        /** The stage of a key definition allowing to specify the key size. */
        interface WithKeySize {
            /**
             * Specifies the size of the RSA key to create.
             *
             * @param size the size of the key in integer
             * @return the next stage of the definition
             */
            WithCreate withKeySize(int size);

            /**
             * Specifies the name of the key curve for elliptic-curve key to create.
             *
             * @param keyCurveName name of the key curve
             * @return the next stage of the definition
             */
            WithCreate withKeyCurveName(KeyCurveName keyCurveName);
        }

        /** The stage of a key definition allowing to specify the allowed operations for the key. */
        interface WithKeyOperations {
            /**
             * Specifies the list of allowed key operations. By default all operations are allowed.
             *
             * @param keyOperations the list of JWK operations
             * @return the next stage of the definition
             */
            WithCreate withKeyOperations(List<KeyOperation> keyOperations);

            /**
             * Specifies the list of allowed key operations. By default all operations are allowed.
             *
             * @param keyOperations the list of JWK operations
             * @return the next stage of the definition
             */
            WithCreate withKeyOperations(KeyOperation... keyOperations);
        }

        /** The stage of a key definition allowing to specify whether to store the key in hardware security modules. */
        interface WithHsm {
            /**
             * Specifies whether to store the key in hardware security modules.
             *
             * @param isHsm store in Hsm if true
             * @return the next stage of the definition
             */
            WithImport withHsm(boolean isHsm);
        }

        /** The stage of a key definition allowing to specify the attributes of the key. */
        interface WithAttributes {
            /**
             * Specifies the attributes of the key.
             *
             * @param attributes the object attributes managed by Key Vault service
             * @return the next stage of the definition
             */
            WithCreate withAttributes(KeyProperties attributes);
        }

        /** The stage of a key definition allowing to specify the tags of the key. */
        interface WithTags {
            /**
             * Specifies the tags on the key.
             *
             * @param tags the key value pair of the tags
             * @return the next stage of the definition
             */
            WithCreate withTags(Map<String, String> tags);
        }

        /** The base stage of the key definition allowing for any other optional settings to be specified. */
        interface WithCreateBase extends Creatable<Key>, WithAttributes, WithTags {
        }

        /**
         * The stage of the key definition which contains all the minimum required inputs for the key to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreate extends WithKeyOperations, WithKeySize, WithCreateBase {
        }

        /**
         * The stage of the key definition which contains all the minimum required inputs for the key to be imported but
         * also allows for any other optional settings to be specified.
         */
        interface WithImport extends WithHsm, WithCreateBase {
        }
    }

    /** Grouping of key update stages. */
    interface UpdateStages {
        /** The stage of a key update allowing to create a new version of the key. */
        interface WithKey {
            /**
             * Specifies a key type to create a new key version.
             *
             * @param keyType the JWK type to create
             * @return the next stage of the update
             */
            UpdateWithCreate withKeyTypeToCreate(KeyType keyType);

            /**
             * Specifies an existing key to import as a new version.
             *
             * @param key the existing JWK to import
             * @return the next stage of the update
             */
            UpdateWithImport withLocalKeyToImport(JsonWebKey key);
        }

        /** The stage of a key update allowing to specify the key size. */
        interface WithKeySize {
            /**
             * Specifies the size of the key to create.
             *
             * @param size the size of the key in integer
             * @return the next stage of the update
             */
            UpdateWithCreate withKeySize(int size);
        }

        /** The stage of a key update allowing to specify whether to store the key in hardware security modules. */
        interface WithHsm {
            /**
             * Specifies whether to store the key in hardware security modules.
             *
             * @param isHsm store in Hsm if true
             * @return the next stage of the update
             */
            UpdateWithImport withHsm(boolean isHsm);
        }

        /** The stage of a key update allowing to specify the allowed operations for the key. */
        interface WithKeyOperations {
            /**
             * Specifies the list of allowed key operations. By default all operations are allowed.
             *
             * @param keyOperations the list of JWK operations
             * @return the next stage of the update
             */
            Update withKeyOperations(List<KeyOperation> keyOperations);

            /**
             * Specifies the list of allowed key operations. By default all operations are allowed.
             *
             * @param keyOperations the list of JWK operations
             * @return the next stage of the update
             */
            Update withKeyOperations(KeyOperation... keyOperations);
        }

        /** The stage of a key update allowing to specify the attributes of the key. */
        interface WithAttributes {
            /**
             * Specifies the attributes of the key.
             *
             * @param attributes the object attributes managed by Key Vault service
             * @return the next stage of the update
             */
            Update withAttributes(KeyProperties attributes);
        }

        /** The stage of a key update allowing to specify the tags of the key. */
        interface WithTags {
            /**
             * Specifies the tags on the key.
             *
             * @param tags the key value pair of the tags
             * @return the next stage of the update
             */
            Update withTags(Map<String, String> tags);
        }
    }

    /** The template for a key update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<Key>,
            UpdateStages.WithKey,
            UpdateStages.WithKeyOperations,
            UpdateStages.WithAttributes,
            UpdateStages.WithTags {
    }

    /** The template for a key vault update operation, with a new key version to be created. */
    interface UpdateWithCreate extends Update, UpdateStages.WithKeySize {
    }

    /** The template for a key vault update operation, with a new key version to be imported. */
    interface UpdateWithImport extends Update, UpdateStages.WithHsm {
    }
}
