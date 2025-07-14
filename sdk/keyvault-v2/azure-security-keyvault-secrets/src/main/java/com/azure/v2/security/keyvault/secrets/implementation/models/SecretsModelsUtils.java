// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.secrets.implementation.models;

import com.azure.v2.security.keyvault.secrets.implementation.DeletedSecretHelper;
import com.azure.v2.security.keyvault.secrets.implementation.SecretPropertiesHelper;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Consumer;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Utility methods for Key Vault Secrets models.
 */
public final class SecretsModelsUtils {
    private static final ClientLogger LOGGER = new ClientLogger(SecretsModelsUtils.class);

    /**
     * Creates a {@link SecretAttributes} from the given {@link SecretProperties}.
     *
     * @param secretProperties The {@link SecretProperties} to create the {@link SecretAttributes} from.
     * @return The created {@link SecretAttributes}.
     */
    public static SecretAttributes createSecretAttributes(SecretProperties secretProperties) {
        if (secretProperties == null) {
            return null;
        }

        return new SecretAttributes().setEnabled(secretProperties.isEnabled())
            .setNotBefore(secretProperties.getNotBefore())
            .setExpires(secretProperties.getExpiresOn());
    }

    /**
     * Creates a {@link KeyVaultSecret} from the given {@link SecretBundle}.
     *
     * @param secretBundle The {@link SecretBundle} to create the {@link KeyVaultSecret} from.
     * @return The created {@link KeyVaultSecret}.
     */
    public static KeyVaultSecret createKeyVaultSecret(SecretBundle secretBundle) {
        if (secretBundle == null) {
            return null;
        }

        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(null, secretBundle.getValue());
        setSecretPropertiesValues(secretBundle, keyVaultSecret.getProperties());

        return keyVaultSecret;
    }

    /**
     * Creates a {@link SecretProperties} from the given {@link SecretBundle}.
     *
     * @param secretBundle The {@link SecretBundle} to create the {@link SecretProperties} from.
     * @return The created {@link SecretProperties}.
     */
    public static SecretProperties createSecretProperties(SecretBundle secretBundle) {
        if (secretBundle == null) {
            return null;
        }

        SecretProperties secretProperties = new SecretProperties();
        setSecretPropertiesValues(secretBundle, secretProperties);

        return secretProperties;
    }

    private static void setSecretPropertiesValues(SecretBundle secretBundle, SecretProperties secretProperties) {
        secretProperties.setContentType(secretBundle.getContentType()).setTags(secretBundle.getTags());

        SecretPropertiesHelper.setId(secretProperties, secretBundle.getId());
        SecretPropertiesHelper.setKeyId(secretProperties, secretBundle.getKid());
        SecretPropertiesHelper.setManaged(secretProperties, secretBundle.isManaged());

        SecretAttributes secretAttributes = secretBundle.getAttributes();
        if (secretAttributes != null) {
            secretProperties.setEnabled(secretAttributes.isEnabled())
                .setExpiresOn(secretAttributes.getExpires())
                .setNotBefore(secretAttributes.getNotBefore());

            SecretPropertiesHelper.setCreatedOn(secretProperties, secretAttributes.getCreated());
            SecretPropertiesHelper.setUpdatedOn(secretProperties, secretAttributes.getUpdated());
            SecretPropertiesHelper.setRecoveryLevel(secretProperties,
                Objects.toString(secretAttributes.getRecoveryLevel(), null));
            SecretPropertiesHelper.setRecoverableDays(secretProperties, secretAttributes.getRecoverableDays());
        }

        unpackId(secretBundle.getId(), name -> SecretPropertiesHelper.setName(secretProperties, name),
            version -> SecretPropertiesHelper.setVersion(secretProperties, version));
    }

    /**
     * Creates a {@link SecretProperties} from the given {@link SecretItem}.
     *
     * @param secretItem The {@link SecretItem} to create the {@link SecretProperties} from.
     * @return The created {@link SecretProperties}.
     */
    public static SecretProperties createSecretProperties(SecretItem secretItem) {
        if (secretItem == null) {
            return null;
        }

        SecretProperties secretProperties = new SecretProperties();
        setSecretPropertiesValues(secretItem, secretProperties);

        return secretProperties;
    }

    private static void setSecretPropertiesValues(SecretItem secretItem, SecretProperties secretProperties) {
        secretProperties.setContentType(secretItem.getContentType()).setTags(secretItem.getTags());

        SecretPropertiesHelper.setId(secretProperties, secretItem.getId());
        SecretPropertiesHelper.setManaged(secretProperties, secretItem.isManaged());

        SecretAttributes secretAttributes = secretItem.getAttributes();
        if (secretAttributes != null) {
            secretProperties.setEnabled(secretAttributes.isEnabled())
                .setExpiresOn(secretAttributes.getExpires())
                .setNotBefore(secretAttributes.getNotBefore());

            SecretPropertiesHelper.setCreatedOn(secretProperties, secretAttributes.getCreated());
            SecretPropertiesHelper.setUpdatedOn(secretProperties, secretAttributes.getUpdated());
            SecretPropertiesHelper.setRecoveryLevel(secretProperties,
                Objects.toString(secretAttributes.getRecoveryLevel(), null));
            SecretPropertiesHelper.setRecoverableDays(secretProperties, secretAttributes.getRecoverableDays());
        }

        unpackId(secretItem.getId(), name -> SecretPropertiesHelper.setName(secretProperties, name),
            version -> SecretPropertiesHelper.setVersion(secretProperties, version));
    }

    /**
     * Creates a {@link DeletedSecret} from the given {@link DeletedSecretBundle}.
     *
     * @param deletedSecretBundle The {@link DeletedSecretBundle} to create the {@link DeletedSecret} from.
     * @return The created {@link DeletedSecret}.
     */
    public static DeletedSecret createDeletedSecret(DeletedSecretBundle deletedSecretBundle) {
        if (deletedSecretBundle == null) {
            return null;
        }

        DeletedSecret deletedSecret = new DeletedSecret();

        deletedSecret.getProperties()
            .setContentType(deletedSecretBundle.getContentType())
            .setTags(deletedSecretBundle.getTags());

        DeletedSecretHelper.setId(deletedSecret, deletedSecretBundle.getId());
        DeletedSecretHelper.setKeyId(deletedSecret, deletedSecretBundle.getKid());
        DeletedSecretHelper.setManaged(deletedSecret, deletedSecretBundle.isManaged());

        SecretAttributes secretAttributes = deletedSecretBundle.getAttributes();

        if (secretAttributes != null) {
            deletedSecret.getProperties()
                .setEnabled(secretAttributes.isEnabled())
                .setExpiresOn(secretAttributes.getExpires())
                .setNotBefore(secretAttributes.getNotBefore());

            DeletedSecretHelper.setCreatedOn(deletedSecret, secretAttributes.getCreated());
            DeletedSecretHelper.setUpdatedOn(deletedSecret, secretAttributes.getUpdated());
            DeletedSecretHelper.setRecoveryLevel(deletedSecret, secretAttributes.getRecoveryLevel().toString());
            DeletedSecretHelper.setRecoverableDays(deletedSecret, secretAttributes.getRecoverableDays());
        }

        unpackId(deletedSecretBundle.getId(), name -> DeletedSecretHelper.setName(deletedSecret, name),
            version -> DeletedSecretHelper.setVersion(deletedSecret, version));

        DeletedSecretHelper.setRecoveryId(deletedSecret, deletedSecretBundle.getRecoveryId());
        DeletedSecretHelper.setScheduledPurgeDate(deletedSecret, deletedSecretBundle.getScheduledPurgeDate());
        DeletedSecretHelper.setDeletedOn(deletedSecret, deletedSecretBundle.getDeletedDate());

        return deletedSecret;
    }

    /**
     * Creates a {@link DeletedSecret} from the given {@link DeletedSecretItem}.
     *
     * @param deletedSecretItem The {@link DeletedSecretItem} to create the {@link DeletedSecret} from.
     * @return The created {@link DeletedSecret}.
     */
    public static DeletedSecret createDeletedSecret(DeletedSecretItem deletedSecretItem) {
        if (deletedSecretItem == null) {
            return null;
        }

        DeletedSecret deletedSecret = new DeletedSecret();

        deletedSecret.getProperties()
            .setContentType(deletedSecretItem.getContentType())
            .setTags(deletedSecretItem.getTags());

        DeletedSecretHelper.setId(deletedSecret, deletedSecretItem.getId());
        DeletedSecretHelper.setManaged(deletedSecret, deletedSecretItem.isManaged());

        SecretAttributes secretAttributes = deletedSecretItem.getAttributes();

        if (secretAttributes != null) {
            deletedSecret.getProperties()
                .setEnabled(secretAttributes.isEnabled())
                .setExpiresOn(secretAttributes.getExpires())
                .setNotBefore(secretAttributes.getNotBefore());

            DeletedSecretHelper.setCreatedOn(deletedSecret, secretAttributes.getCreated());
            DeletedSecretHelper.setUpdatedOn(deletedSecret, secretAttributes.getUpdated());
            DeletedSecretHelper.setRecoveryLevel(deletedSecret, secretAttributes.getRecoveryLevel().toString());
            DeletedSecretHelper.setRecoverableDays(deletedSecret, secretAttributes.getRecoverableDays());
        }

        unpackId(deletedSecretItem.getId(), name -> DeletedSecretHelper.setName(deletedSecret, name),
            version -> DeletedSecretHelper.setVersion(deletedSecret, version));

        DeletedSecretHelper.setRecoveryId(deletedSecret, deletedSecretItem.getRecoveryId());
        DeletedSecretHelper.setScheduledPurgeDate(deletedSecret, deletedSecretItem.getScheduledPurgeDate());
        DeletedSecretHelper.setDeletedOn(deletedSecret, deletedSecretItem.getDeletedDate());

        return deletedSecret;
    }

    /**
     * Unpacks a Key Vault secret ID into a name and version.
     *
     * @param id The Key Vault secret ID to unpack.
     * @param nameConsumer The consumer to accept the name.
     * @param versionConsumer The consumer to accept the version.
     */
    public static void unpackId(String id, Consumer<String> nameConsumer, Consumer<String> versionConsumer) {
        if (isNullOrEmpty(id)) {
            return;
        }

        try {
            URI uri = new URI(id);
            String[] tokens = uri.getPath().split("/");

            if (tokens.length >= 3) {
                nameConsumer.accept(tokens[2]);
            }

            if (tokens.length >= 4) {
                versionConsumer.accept(tokens[3]);
            }
        } catch (URISyntaxException e) {
            // Should never come here.
            LOGGER.atError().setThrowable(e).log("Received malformed secret id URL from Key Vault Service.");
        }
    }

    /**
     * Converts a JSON reader representing an epoch time to an {@link OffsetDateTime}.
     *
     * @param epochReader The JSON reader representing the epoch time.
     * @return The {@link OffsetDateTime} representation of the epoch time.
     * @throws IOException If an I/O error occurs.
     */
    public static OffsetDateTime epochToOffsetDateTime(JsonReader epochReader) throws IOException {
        Instant instant = Instant.ofEpochMilli(epochReader.getLong() * 1000L);

        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private SecretsModelsUtils() {
        // Private constructor to prevent instantiation.
    }
}
