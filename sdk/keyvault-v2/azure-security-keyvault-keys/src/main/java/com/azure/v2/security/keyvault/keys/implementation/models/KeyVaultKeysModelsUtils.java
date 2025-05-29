// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation.models;

import com.azure.v2.security.keyvault.keys.implementation.DeletedKeyHelper;
import com.azure.v2.security.keyvault.keys.implementation.KeyPropertiesHelper;
import com.azure.v2.security.keyvault.keys.implementation.KeyRotationPolicyHelper;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeyHelper;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import com.azure.v2.security.keyvault.keys.models.KeyProperties;
import com.azure.v2.security.keyvault.keys.models.KeyReleasePolicy;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeysUtils.unpackId;

/**
 * Utility class for converting between different models used in the Key Vault Keys library.
 */
public final class KeyVaultKeysModelsUtils {
    /**
     * Creates an instance {@link KeyVaultKey} from the provided {@link KeyBundle}.
     *
     * @param keyBundle The key bundle.
     * @return The created {@link KeyVaultKey}.
     */
    public static KeyVaultKey createKeyVaultKey(KeyBundle keyBundle) {
        if (keyBundle == null) {
            return null;
        }

        KeyVaultKey keyVaultKey = KeyVaultKeyHelper.createKeyVaultKey(mapJsonWebKeyFromImpl(keyBundle.getKey()));

        populateKeyProperties(keyBundle, keyVaultKey.getProperties());

        return keyVaultKey;
    }

    /**
     * Creates an instance of {@link KeyVaultKey} from the provided {@link KeyItem}.
     *
     * @param keyItem The key item.
     * @return The created {@link KeyVaultKey}.
     */
    public static KeyProperties createKeyProperties(KeyItem keyItem) {
        if (keyItem == null) {
            return null;
        }

        KeyProperties properties = new KeyProperties();

        populateKeyProperties(keyItem, properties);

        return properties;
    }

    /**
     * Sets the properties of a {@link KeyItem} object based on the provided {@link KeyProperties}.
     */
    private static void populateKeyProperties(KeyItem keyItem, KeyProperties properties) {
        if (keyItem != null) {
            populateKeyProperties(null, keyItem.getTags(), keyItem.isManaged(), keyItem.getKid(), properties,
                keyItem.getAttributes());
        }
    }

    /**
     * Sets the properties of a {@link DeletedKeyItem} object based on the provided {@link KeyProperties}.
     */
    private static void populateKeyProperties(DeletedKeyItem item, KeyProperties properties) {
        if (item != null) {
            populateKeyProperties(null, item.getTags(), item.isManaged(), item.getKid(), properties,
                item.getAttributes());
        }
    }

    /**
     * Creates a {@link DeletedKey} from the provided {@link DeletedKeyBundle}.
     *
     * @param bundle The deleted key bundle.
     * @return The created {@link DeletedKey}.
     */
    public static DeletedKey createDeletedKey(DeletedKeyBundle bundle) {
        if (bundle == null) {
            return null;
        }

        DeletedKey deletedKey = DeletedKeyHelper.createDeletedKey(mapJsonWebKeyFromImpl(bundle.getKey()));

        populateKeyProperties(bundle, deletedKey.getProperties());

        DeletedKeyHelper.setRecoveryId(deletedKey, bundle.getRecoveryId());
        DeletedKeyHelper.setScheduledPurgeDate(deletedKey, bundle.getScheduledPurgeDate());
        DeletedKeyHelper.setDeletedOn(deletedKey, bundle.getDeletedDate());

        return deletedKey;
    }

    /**
     * Creates an instance of {@link DeletedKey} from a provided {@link DeletedKeyItem}.
     *
     * @param item The deleted key item.
     * @return The created {@link DeletedKey}.
     */
    public static DeletedKey createDeletedKey(DeletedKeyItem item) {
        if (item == null) {
            return null;
        }

        DeletedKey deletedKey = new DeletedKey();

        populateKeyProperties(item, deletedKey.getProperties());

        DeletedKeyHelper.setRecoveryId(deletedKey, item.getRecoveryId());
        DeletedKeyHelper.setDeletedOn(deletedKey, item.getDeletedDate());
        DeletedKeyHelper.setScheduledPurgeDate(deletedKey, item.getScheduledPurgeDate());

        return deletedKey;
    }

    private static JsonWebKey
        mapJsonWebKeyFromImpl(com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKey impl) {

        if (impl == null) {
            return null;
        }

        return new JsonWebKey().setId(impl.getKid())
            .setKeyType(impl.getKty())
            .setKeyOps(impl.getKeyOps().stream().map(KeyOperation::fromValue).collect(Collectors.toList()))
            .setN(impl.getN())
            .setE(impl.getE())
            .setD(impl.getD())
            .setDp(impl.getDp())
            .setDq(impl.getDq())
            .setQi(impl.getQi())
            .setP(impl.getP())
            .setQ(impl.getQ())
            .setK(impl.getK())
            .setT(impl.getT())
            .setCurveName(impl.getCrv())
            .setX(impl.getX())
            .setY(impl.getY());
    }

    /**
     * Maps a public {@link JsonWebKey} instance to an implementation
     * {@link com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKey} instance.
     *
     * @param key The {@link JsonWebKey} to map.
     * @return The created {@link com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKey}.
     */
    public static com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKey mapJsonWebKey(JsonWebKey key) {
        if (key == null) {
            return null;
        }

        return new com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKey().setKid(key.getId())
            .setKty(key.getKeyType())
            .setKeyOps(key.getKeyOps().stream().map(KeyOperation::toString).collect(Collectors.toList()))
            .setN(key.getN())
            .setE(key.getE())
            .setD(key.getD())
            .setDp(key.getDp())
            .setDq(key.getDq())
            .setQi(key.getQi())
            .setP(key.getP())
            .setQ(key.getQ())
            .setK(key.getK())
            .setT(key.getT())
            .setCrv(key.getCurveName())
            .setX(key.getX())
            .setY(key.getY());
    }

    /**
     * Creates an instance of {@link KeyAttributes} based on the provided {@link CreateKeyOptions}.
     *
     * @param options The create key options.
     * @return The created {@link KeyAttributes}.
     */
    public static KeyAttributes createKeyAttributes(CreateKeyOptions options) {
        if (options == null) {
            return null;
        }

        return new KeyAttributes().setEnabled(options.isEnabled())
            .setExportable(options.isExportable())
            .setExpires(options.getExpiresOn())
            .setNotBefore(options.getNotBefore());
    }

    /**
     * Creates an instance of {@link KeyAttributes} based on the provided {@link KeyProperties}.
     *
     * @param properties The key properties.
     * @return The created {@link KeyAttributes}.
     */
    public static KeyAttributes createKeyAttributes(KeyProperties properties) {
        if (properties == null) {
            return null;
        }

        return new KeyAttributes().setEnabled(properties.isEnabled())
            .setExportable(properties.isExportable())
            .setExpires(properties.getExpiresOn())
            .setNotBefore(properties.getNotBefore());
    }

    private static void populateKeyProperties(KeyBundle bundle, KeyProperties properties) {
        if (bundle != null) {
            populateKeyProperties(mapKeyReleasePolicyImpl(bundle.getReleasePolicy()), bundle.getTags(),
                bundle.isManaged(), bundle.getKey().getKid(), properties, bundle.getAttributes());
        }
    }

    private static void populateKeyProperties(DeletedKeyBundle bundle, KeyProperties properties) {
        if (bundle != null) {
            populateKeyProperties(mapKeyReleasePolicyImpl(bundle.getReleasePolicy()), bundle.getTags(),
                bundle.isManaged(), bundle.getKey().getKid(), properties, bundle.getAttributes());
        }
    }

    private static void populateKeyProperties(KeyReleasePolicy keyReleasePolicy, Map<String, String> tags,
        Boolean isManaged, String kid, KeyProperties properties, KeyAttributes attributes) {

        properties.setReleasePolicy(keyReleasePolicy).setTags(tags);

        KeyPropertiesHelper.setManaged(properties, isManaged);
        KeyPropertiesHelper.setId(properties, kid);

        unpackId(kid, name -> KeyPropertiesHelper.setName(properties, name),
            version -> KeyPropertiesHelper.setVersion(properties, version));

        if (attributes != null) {
            properties.setEnabled(attributes.isEnabled())
                .setExportable(attributes.isExportable())
                .setNotBefore(attributes.getNotBefore())
                .setExpiresOn(attributes.getExpires());

            KeyPropertiesHelper.setCreatedOn(properties, attributes.getCreated());
            KeyPropertiesHelper.setUpdatedOn(properties, attributes.getUpdated());
            KeyPropertiesHelper.setRecoveryLevel(properties, Objects.toString(attributes.getRecoveryLevel(), null));
            KeyPropertiesHelper.setRecoverableDays(properties, attributes.getRecoverableDays());
            KeyPropertiesHelper.setHsmPlatform(properties, attributes.getHsmPlatform());
        }
    }

    /**
     * Maps a public {@link KeyReleasePolicy} instance to an implementation
     * {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyReleasePolicy} instance.
     *
     * @param policy The {@link KeyReleasePolicy} to map.
     * @return The created {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyReleasePolicy}.
     */
    public static com.azure.v2.security.keyvault.keys.implementation.models.KeyReleasePolicy
        mapKeyReleasePolicy(KeyReleasePolicy policy) {

        if (policy == null) {
            return null;
        }

        return new com.azure.v2.security.keyvault.keys.implementation.models.KeyReleasePolicy()
            .setContentType(policy.getContentType())
            .setImmutable(policy.isImmutable())
            .setEncodedPolicy(policy.getEncodedPolicy().toBytes());
    }

    private static KeyReleasePolicy
        mapKeyReleasePolicyImpl(com.azure.v2.security.keyvault.keys.implementation.models.KeyReleasePolicy impl) {
        if (impl == null) {
            return null;
        }

        return new KeyReleasePolicy(BinaryData.fromBytes(impl.getEncodedPolicy())).setContentType(impl.getContentType())
            .setImmutable(impl.isImmutable());
    }

    /**
     * Maps an implementation {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy}
     * instance to a public {@link KeyRotationPolicy} instance.
     *
     * @param impl The implementation
     * {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy} to map.
     * @return The created {@link KeyRotationPolicy}.
     */
    public static KeyRotationPolicy
        mapKeyRotationPolicyImpl(com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {

        return (impl == null) ? null : KeyRotationPolicyHelper.createPolicy(impl);
    }

    /**
     * Maps a public {@link KeyRotationPolicy} instance to an implementation
     * {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy} instance.
     *
     * @param policy The {@link KeyRotationPolicy} to map.
     * @return The created {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy}.
     */
    public static com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy
        mapKeyRotationPolicy(KeyRotationPolicy policy) {

        if (policy == null) {
            return null;
        }

        return KeyRotationPolicyHelper.getImpl(policy);
    }

    private KeyVaultKeysModelsUtils() {
    }
}
