// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

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
     * The SAS blob container constant.
     */
    private static final String SAS_CONTAINER_CONSTANT = "c";

    private final ClientLogger logger = new ClientLogger(BlobSasImplUtil.class);

    private String version;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String containerName;

    private String blobName;

    private String resource;

    private String snapshotId;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    /**
     * Creates a new {@link BlobSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link BlobServiceSasSignatureValues}
     * @param containerName The container name
     */
    public BlobSasImplUtil(BlobServiceSasSignatureValues sasValues, String containerName) {
        this(sasValues, containerName, null, null);
    }

    /**
     * Creates a new {@link BlobSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link BlobServiceSasSignatureValues}
     * @param containerName The container name
     * @param blobName The blob name
     * @param snapshotId The snapshot id
     */
    public BlobSasImplUtil(BlobServiceSasSignatureValues sasValues, String containerName, String blobName,
        String snapshotId) {
        Objects.requireNonNull(sasValues);
        this.version = sasValues.getVersion();
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.containerName = containerName;
        this.blobName = blobName;
        this.snapshotId = snapshotId;
        this.identifier = sasValues.getIdentifier();
        this.cacheControl = sasValues.getCacheControl();
        this.contentDisposition = sasValues.getContentDisposition();
        this.contentEncoding = sasValues.getContentEncoding();
        this.contentLanguage = sasValues.getContentLanguage();
        this.contentType = sasValues.getContentType();
    }

    /**
     * Generates a Sas signed with a {@link StorageSharedKeyCredential}
     *
     * @param storageSharedKeyCredentials {@link StorageSharedKeyCredential}
     * @return A String representing the Sas
     */
    public String generateSas(StorageSharedKeyCredential storageSharedKeyCredentials) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName());
        final String signature = storageSharedKeyCredentials.computeHmac256(stringToSign(canonicalName));

        return encode(null /* userDelegationKey */, signature);
    }

    /**
     * Generates a Sas signed with a {@link UserDelegationKey}
     *
     * @param delegationKey {@link UserDelegationKey}
     * @param accountName The account name
     * @return A String representing the Sas
     */
    public String generateUserDelegationSas(UserDelegationKey delegationKey, String accountName) {
        StorageImplUtils.assertNotNull("delegationKey", delegationKey);
        StorageImplUtils.assertNotNull("accountName", accountName);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(accountName);
        String signature = StorageImplUtils.computeHMac256(
            delegationKey.getValue(), stringToSign(delegationKey, canonicalName));

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

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, this.version);
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
     *    c. Otherwise, it is a blob resource.
     * 4. Reparse permissions depending on what the resource is. If it is an unrecognised resource, do nothing.
     *
     * Taken from:
     * https://github.com/Azure/azure-storage-blob-go/blob/master/azblob/sas_service.go#L33
     * https://github.com/Azure/azure-sdk-for-net/blob/master/sdk/storage/Azure.Storage.Blobs/src/Sas/BlobSasBuilder.cs
     */
    private void ensureState() {
        if (version == null) {
            version = BlobServiceVersion.getLatest().getVersion();
        }

        if (identifier == null) {
            if (expiryTime == null || permissions == null) {
                throw logger.logExceptionAsError(new IllegalStateException("If identifier is not set, expiry time "
                    + "and permissions must be set"));
            }
        }

        if (CoreUtils.isNullOrEmpty(blobName)) {
            resource = SAS_CONTAINER_CONSTANT;
        } else if (snapshotId != null) {
            resource = SAS_BLOB_SNAPSHOT_CONSTANT;
        } else {
            resource = SAS_BLOB_CONSTANT;
        }

        if (permissions != null) {
            switch (resource) {
                case SAS_BLOB_CONSTANT:
                case SAS_BLOB_SNAPSHOT_CONSTANT:
                    permissions = BlobSasPermission.parse(permissions).toString();
                    break;
                case SAS_CONTAINER_CONSTANT:
                    permissions = BlobContainerSasPermission.parse(permissions).toString();
                    break;
                default:
                    // We won't reparse the permissions if we don't know the type.
                    logger.info("Not re-parsing permissions. Resource type '{}' is unknown.", resource);
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
        return String.join("\n",
            this.permissions == null ? "" : permissions,
            this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            version,
            resource,
            this.snapshotId == null ? "" : this.snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }

    private String stringToSign(final UserDelegationKey key, String canonicalName) {
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
            version,
            resource,
            this.snapshotId == null ? "" : this.snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }
}
