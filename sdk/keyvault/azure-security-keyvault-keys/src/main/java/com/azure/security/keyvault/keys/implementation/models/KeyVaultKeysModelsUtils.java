// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.implementation.DeletedKeyHelper;
import com.azure.security.keyvault.keys.implementation.KeyPropertiesHelper;
import com.azure.security.keyvault.keys.implementation.KeyRotationPolicyHelper;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeyHelper;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyReleasePolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility class for KeyVault Keys models.
 */
public final class KeyVaultKeysModelsUtils {
    public static KeyVaultKey createKeyVaultKey(KeyBundle keyBundle) {
        if (keyBundle == null) {
            return null;
        }

        KeyVaultKey keyVaultKey = KeyVaultKeyHelper.createKeyVaultKey(mapJsonWebKeyFromImpl(keyBundle.getKey()));
        populateKeyProperties(keyBundle, keyVaultKey.getProperties());

        return keyVaultKey;
    }

    public static KeyProperties createKeyProperties(KeyItem keyItem) {
        if (keyItem == null) {
            return null;
        }

        KeyProperties properties = new KeyProperties();
        populateKeyProperties(keyItem, properties);

        return properties;
    }

    private static void populateKeyProperties(KeyItem keyItem, KeyProperties properties) {
        if (keyItem == null) {
            return;
        }

        properties.setTags(keyItem.getTags());
        KeyPropertiesHelper.setManaged(properties, keyItem.isManaged());
        KeyPropertiesHelper.setId(properties, keyItem.getKid());

        unpackId(keyItem.getKid(), name -> KeyPropertiesHelper.setName(properties, name),
            version -> KeyPropertiesHelper.setVersion(properties, version));

        KeyAttributes attributes = keyItem.getAttributes();
        if (attributes != null) {
            properties.setEnabled(attributes.isEnabled())
                .setExpiresOn(attributes.getExpires())
                .setExportable(attributes.isExportable())
                .setNotBefore(attributes.getNotBefore());

            KeyPropertiesHelper.setCreatedOn(properties, attributes.getCreated());
            KeyPropertiesHelper.setUpdatedOn(properties, attributes.getUpdated());
            KeyPropertiesHelper.setRecoveryLevel(properties, Objects.toString(attributes.getRecoveryLevel(), null));
            KeyPropertiesHelper.setRecoverableDays(properties, attributes.getRecoverableDays());
            KeyPropertiesHelper.setHsmPlatform(properties, attributes.getHsmPlatform());
        }
    }

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

    private static JsonWebKey mapJsonWebKeyFromImpl(
        com.azure.security.keyvault.keys.implementation.models.JsonWebKey impl) {
        if (impl == null) {
            return null;
        }

        return new JsonWebKey()
            .setId(impl.getKid())
            .setKeyType(impl.getKty())
            .setKeyOps(impl.getKeyOps())
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

    public static com.azure.security.keyvault.keys.implementation.models.JsonWebKey mapJsonWebKey(JsonWebKey key) {
        if (key == null) {
            return null;
        }

        return new com.azure.security.keyvault.keys.implementation.models.JsonWebKey()
            .setKid(key.getId())
            .setKty(key.getKeyType())
            .setKeyOps(key.getKeyOps())
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

    public static KeyAttributes createKeyAttributes(CreateKeyOptions options) {
        if (options == null) {
            return null;
        }

        return new KeyAttributes()
            .setEnabled(options.isEnabled())
            .setExportable(options.isExportable())
            .setExpires(options.getExpiresOn())
            .setNotBefore(options.getNotBefore());
    }

    public static KeyAttributes createKeyAttributes(KeyProperties properties) {
        if (properties == null) {
            return null;
        }

        return new KeyAttributes()
            .setEnabled(properties.isEnabled())
            .setExportable(properties.isExportable())
            .setExpires(properties.getExpiresOn())
            .setNotBefore(properties.getNotBefore());
    }

    private static void populateKeyProperties(KeyBundle bundle, KeyProperties properties) {
        if (bundle == null) {
            return;
        }

        properties.setReleasePolicy(mapKeyReleasePolicyImpl(bundle.getReleasePolicy()))
            .setTags(bundle.getTags());

        KeyPropertiesHelper.setManaged(properties, bundle.isManaged());
        KeyPropertiesHelper.setId(properties, bundle.getKey().getKid());
        unpackId(bundle.getKey().getKid(), name -> KeyPropertiesHelper.setName(properties, name),
            version -> KeyPropertiesHelper.setVersion(properties, version));

        KeyAttributes attributes = bundle.getAttributes();
        if (attributes != null) {
            properties.setEnabled(attributes.isEnabled())
                .setEnabled(attributes.isEnabled())
                .setExportable(attributes.isExportable())
                .setNotBefore(attributes.getNotBefore())
                .setExpiresOn(attributes.getExpires());

            KeyPropertiesHelper.setCreatedOn(properties, attributes.getCreated());
            KeyPropertiesHelper.setUpdatedOn(properties, attributes.getUpdated());
            KeyPropertiesHelper.setRecoveryLevel(properties,
                Objects.toString(attributes.getRecoveryLevel().toString(), null));
            KeyPropertiesHelper.setRecoverableDays(properties, attributes.getRecoverableDays());
            KeyPropertiesHelper.setHsmPlatform(properties, attributes.getHsmPlatform());
        }
    }

    public static com.azure.security.keyvault.keys.implementation.models.KeyReleasePolicy mapKeyReleasePolicy(
        KeyReleasePolicy policy) {
        if (policy == null) {
            return null;
        }

        return new com.azure.security.keyvault.keys.implementation.models.KeyReleasePolicy()
            .setContentType(policy.getContentType())
            .setImmutable(policy.isImmutable())
            .setEncodedPolicy(policy.getEncodedPolicy().toBytes());
    }

    private static KeyReleasePolicy mapKeyReleasePolicyImpl(
        com.azure.security.keyvault.keys.implementation.models.KeyReleasePolicy impl) {
        if (impl == null) {
            return null;
        }

        return new KeyReleasePolicy(BinaryData.fromBytes(impl.getEncodedPolicy()))
            .setContentType(impl.getContentType())
            .setImmutable(impl.isImmutable());
    }

    public static KeyRotationPolicy mapKeyRotationPolicyImpl(
        com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {
        return (impl == null) ? null : KeyRotationPolicyHelper.createPolicy(impl);
    }

    public static com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy mapKeyRotationPolicy(
        KeyRotationPolicy policy) {
        if (policy == null) {
            return null;
        }

        return KeyRotationPolicyHelper.getImpl(policy);
    }

    private static void unpackId(String keyId, Consumer<String> nameConsumer, Consumer<String> versionConsumer) {
        if (CoreUtils.isNullOrEmpty(keyId)) {
            return;
        }

        try {
            URL url = new URL(keyId);
            String[] tokens = url.getPath().split("/");
            if (tokens.length >= 3) {
                nameConsumer.accept(tokens[2]);
            }

            if (tokens.length >= 4) {
                versionConsumer.accept(tokens[3]);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private KeyVaultKeysModelsUtils() {
    }
}
