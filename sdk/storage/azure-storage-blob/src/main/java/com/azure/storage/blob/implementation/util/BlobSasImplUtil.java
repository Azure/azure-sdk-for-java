// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;

import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.storage.common.implementation.SasImplUtils.formatQueryParameterDate;
import static com.azure.storage.common.implementation.SasImplUtils.tryAppendQueryParameter;

/**
 * This class provides helper methods for common blob service sas patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class BlobSasImplUtil {
    /**
     * The SAS blob constant.
     */
    private static final String SAS_BLOB_CONSTANT = "b";

    /**
     * The SAS blob snapshot constant.
     */
    private static final String SAS_BLOB_SNAPSHOT_CONSTANT = "bs";

    /**
     * The SAS blob version constant.
     */
    private static final String SAS_BLOB_VERSION_CONSTANT = "bv";

    /**
     * The SAS blob container constant.
     */
    private static final String SAS_CONTAINER_CONSTANT = "c";

    private static final ClientLogger LOGGER = new ClientLogger(BlobSasImplUtil.class);

    private static final String VERSION = Configuration.getGlobalConfiguration()
        .get(Constants.PROPERTY_AZURE_STORAGE_SAS_SERVICE_VERSION, BlobServiceVersion.getLatest().getVersion());

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String containerName;

    private String blobName;

    private String resource;

    private String snapshotId;

    private String versionId;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    private String authorizedAadObjectId;

    private String correlationId;

    /**
     * Creates a new {@link BlobSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link BlobServiceSasSignatureValues}
     * @param containerName The container name
     */
    public BlobSasImplUtil(BlobServiceSasSignatureValues sasValues, String containerName) {
        this(sasValues, containerName, null, null, null);
    }

    /**
     * Creates a new {@link BlobSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link BlobServiceSasSignatureValues}
     * @param containerName The container name
     * @param blobName The blob name
     * @param snapshotId The snapshot id
     * @param versionId The version id
     */
    public BlobSasImplUtil(BlobServiceSasSignatureValues sasValues, String containerName, String blobName,
        String snapshotId, String versionId) {
        Objects.requireNonNull(sasValues);
        if (snapshotId != null && versionId != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.containerName = containerName;
        this.blobName = blobName;
        this.snapshotId = snapshotId;
        this.versionId = versionId;
        this.identifier = sasValues.getIdentifier();
        this.cacheControl = sasValues.getCacheControl();
        this.contentDisposition = sasValues.getContentDisposition();
        this.contentEncoding = sasValues.getContentEncoding();
        this.contentLanguage = sasValues.getContentLanguage();
        this.contentType = sasValues.getContentType();
        this.authorizedAadObjectId = sasValues.getPreauthorizedAgentObjectId();
        this.correlationId = sasValues.getCorrelationId();
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
        final String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName());
        final String stringToSign = stringToSign(canonicalName);
        StorageImplUtils.logStringToSign(LOGGER, stringToSign, context);
        final String signature = storageSharedKeyCredentials.computeHmac256(stringToSign);

        return encode(null /* userDelegationKey */, signature);
    }

    /**
     * Generates a Sas signed with a {@link UserDelegationKey}
     *
     * @param delegationKey {@link UserDelegationKey}
     * @param accountName The account name
     * @param context Additional context that is passed through the code when generating a SAS.
     * @return A String representing the Sas
     */
    public String generateUserDelegationSas(UserDelegationKey delegationKey, String accountName, Context context) {
        StorageImplUtils.assertNotNull("delegationKey", delegationKey);
        StorageImplUtils.assertNotNull("accountName", accountName);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(accountName);
        final String stringToSign = stringToSign(delegationKey, canonicalName);
        StorageImplUtils.logStringToSign(LOGGER, stringToSign, context);
        String signature = StorageImplUtils.computeHMac256(delegationKey.getValue(), stringToSign);

        return encode(delegationKey, signature);
    }

    /**
     * Encodes a Sas from the values in this type.
     * @param userDelegationKey {@link UserDelegationKey}
     * @param signature The signature of the Sas.
     * @return A String representing the Sas.
     */
    private String encode(UserDelegationKey userDelegationKey, String signature) {
        /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, VERSION);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_START_TIME, formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_EXPIRY_TIME, formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_IDENTIFIER, this.identifier);
        if (userDelegationKey != null) {
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_OBJECT_ID,
                userDelegationKey.getSignedObjectId());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_TENANT_ID,
                userDelegationKey.getSignedTenantId());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_START,
                formatQueryParameterDate(userDelegationKey.getSignedStart()));
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_EXPIRY,
                formatQueryParameterDate(userDelegationKey.getSignedExpiry()));
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_SERVICE,
                userDelegationKey.getSignedService());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_VERSION,
                userDelegationKey.getSignedVersion());

            /* Only parameters relevant for user delegation SAS. */
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PREAUTHORIZED_AGENT_OBJECT_ID, this.authorizedAadObjectId);
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CORRELATION_ID, this.correlationId);
        }
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_RESOURCE, this.resource);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNATURE, signature);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CACHE_CONTROL, this.cacheControl);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_DISPOSITION, this.contentDisposition);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_ENCODING, this.contentEncoding);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_LANGUAGE, this.contentLanguage);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_TYPE, this.contentType);

        return sb.toString();

    }

    /**
     * Ensures that the builder's properties are in a consistent state.

     * 1. If there is no version, use latest.
     * 2. If there is no identifier set, ensure expiryTime and permissions are set.
     * 3. Resource name is chosen by:
     *    a. If "BlobName" is _not_ set, it is a container resource.
     *    b. Otherwise, if "SnapshotId" is set, it is a blob snapshot resource.
     *    c. Otherwise, if "VersionId" is set, it is a blob version resource.
     *    d. Otherwise, it is a blob resource.
     * 4. Reparse permissions depending on what the resource is. If it is an unrecognized resource, do nothing.
     *
     * Taken from:
     * https://github.com/Azure/azure-storage-blob-go/blob/master/azblob/sas_service.go#L33
     * https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/storage/Azure.Storage.Blobs/src/Sas/BlobSasBuilder.cs
     */
    private void ensureState() {
        if (identifier == null) {
            if (expiryTime == null || permissions == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("If identifier is not set, expiry time "
                    + "and permissions must be set"));
            }
        }

        if (CoreUtils.isNullOrEmpty(blobName)) {
            resource = SAS_CONTAINER_CONSTANT;
        } else if (snapshotId != null) {
            resource = SAS_BLOB_SNAPSHOT_CONSTANT;
        } else if (versionId != null) {
            resource = SAS_BLOB_VERSION_CONSTANT;
        } else {
            resource = SAS_BLOB_CONSTANT;
        }

        if (permissions != null) {
            switch (resource) {
                case SAS_BLOB_CONSTANT:
                case SAS_BLOB_SNAPSHOT_CONSTANT:
                case SAS_BLOB_VERSION_CONSTANT:
                    permissions = BlobSasPermission.parse(permissions).toString();
                    break;
                case SAS_CONTAINER_CONSTANT:
                    permissions = BlobContainerSasPermission.parse(permissions).toString();
                    break;
                default:
                    // We won't reparse the permissions if we don't know the type.
                    LOGGER.info("Not re-parsing permissions. Resource type '{}' is unknown.", resource);
                    break;
            }
        }
    }

    /**
     * Computes the canonical name for a container or blob resource for SAS signing.
     */
    private String getCanonicalName(String account) {
        // Container: "/blob/account/containername"
        // Blob:      "/blob/account/containername/blobname"
        return CoreUtils.isNullOrEmpty(blobName)
            ? String.format("/blob/%s/%s", account, containerName)
            : String.format("/blob/%s/%s/%s", account, containerName, blobName.replace("\\", "/"));
    }

    private String stringToSign(String canonicalName) {
        String versionSegment = this.snapshotId == null ? this.versionId : this.snapshotId;
        return String.join("\n",
            this.permissions == null ? "" : permissions,
            this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            VERSION,
            resource,
            versionSegment == null ? "" : versionSegment,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }

    private String stringToSign(final UserDelegationKey key, String canonicalName) {
        String versionSegment = this.snapshotId == null ? this.versionId : this.snapshotId;
        if (VERSION.compareTo(BlobServiceVersion.V2019_12_12.getVersion()) <= 0) {
            return String.join("\n",
                this.permissions == null ? "" : this.permissions,
                this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
                this.expiryTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
                canonicalName,
                key.getSignedObjectId() == null ? "" : key.getSignedObjectId(),
                key.getSignedTenantId() == null ? "" : key.getSignedTenantId(),
                key.getSignedStart() == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedStart()),
                key.getSignedExpiry() == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedExpiry()),
                key.getSignedService() == null ? "" : key.getSignedService(),
                key.getSignedVersion() == null ? "" : key.getSignedVersion(),
                this.sasIpRange == null ? "" : this.sasIpRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                VERSION,
                resource,
                versionSegment == null ? "" : versionSegment,
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        } else {
            return String.join("\n",
                this.permissions == null ? "" : this.permissions,
                this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
                this.expiryTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
                canonicalName,
                key.getSignedObjectId() == null ? "" : key.getSignedObjectId(),
                key.getSignedTenantId() == null ? "" : key.getSignedTenantId(),
                key.getSignedStart() == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedStart()),
                key.getSignedExpiry() == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedExpiry()),
                key.getSignedService() == null ? "" : key.getSignedService(),
                key.getSignedVersion() == null ? "" : key.getSignedVersion(),
                this.authorizedAadObjectId == null ? "" : this.authorizedAadObjectId,
                "", /* suoid - empty since this applies to HNS only accounts. */
                this.correlationId == null ? "" : this.correlationId,
                this.sasIpRange == null ? "" : this.sasIpRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                VERSION,
                resource,
                versionSegment == null ? "" : versionSegment,
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        }
    }
}
