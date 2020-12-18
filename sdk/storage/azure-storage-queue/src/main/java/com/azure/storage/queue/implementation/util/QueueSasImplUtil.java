// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.queue.QueueServiceVersion;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.storage.common.implementation.SasImplUtils.formatQueryParameterDate;
import static com.azure.storage.common.implementation.SasImplUtils.tryAppendQueryParameter;

/**
 * This class provides helper methods for common queue service sas patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class QueueSasImplUtil {

    private final ClientLogger logger = new ClientLogger(QueueSasImplUtil.class);

    private String version;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String queueName;

    private String identifier;

    /**
     * Creates a new {@link QueueSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link QueueServiceSasSignatureValues}
     * @param queueName The queue name
     */
    public QueueSasImplUtil(QueueServiceSasSignatureValues sasValues, String queueName) {
        Objects.requireNonNull(sasValues);
        this.version = sasValues.getVersion();
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.queueName = queueName;
        this.identifier = sasValues.getIdentifier();
    }

    /**
     * Generates a Sas signed with a {@link StorageSharedKeyCredential}
     *
     * @param storageSharedKeyCredentials {@link StorageSharedKeyCredential}
     * @param context Additional context that is passed through the code when generating a SAS.
     * @return A String representing the Sas
     */
    public String generateSas(StorageSharedKeyCredential storageSharedKeyCredentials, Context context) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName());
        String stringToSign = stringToSign(canonicalName);
        StorageImplUtils.logStringToSign(logger, stringToSign, context);
        String signature = storageSharedKeyCredentials.computeHmac256(stringToSign);

        return encode(signature);
    }

    private String encode(String signature) {
        /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, this.version);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_START_TIME, formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_EXPIRY_TIME, formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_IDENTIFIER, this.identifier);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNATURE, signature);

        return sb.toString();
    }

    /**
     * Ensures that the builder's properties are in a consistent state.

     * 1. If there is no version, use latest.
     * 2. If there is no identifier set, ensure expiryTime and permissions are set.
     * 4. Reparse permissions depending on what the resource is. If it is an unrecognised resource, do nothing.
     */
    private void ensureState() {
        if (version == null) {
            version = QueueServiceVersion.getLatest().getVersion();
        }

        if (identifier == null) {
            if (expiryTime == null || permissions == null) {
                throw logger.logExceptionAsError(new IllegalStateException("If identifier is not set, expiry time "
                    + "and permissions must be set"));
            }
        }

        if (permissions != null) {
            if (queueName != null) {
                permissions = QueueSasPermission.parse(permissions).toString();
            } else {
                // We won't reparse the permissions if we don't know the type.
                logger.info("Not re-parsing permissions. Resource type is not queue.");
            }
        }
    }

    /**
     * Computes the canonical name for a queue resource for SAS signing.
     * @param account Account of the storage account.
     * @return Canonical name as a string.
     */
    private String getCanonicalName(String account) {
        // Queue: "/queue/account/queuename"
        return String.join("", new String[] { "/queue/", account, "/", queueName });
    }

    private String stringToSign(String canonicalName) {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version
        );
    }

}
