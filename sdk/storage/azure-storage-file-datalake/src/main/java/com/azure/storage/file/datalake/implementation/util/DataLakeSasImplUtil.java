// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeServiceVersion;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.sas.PathSasPermission;

import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.storage.common.implementation.SasImplUtils.formatQueryParameterDate;
import static com.azure.storage.common.implementation.SasImplUtils.tryAppendQueryParameter;

/**
 * This class provides helper methods for common datalake service sas patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class DataLakeSasImplUtil {
    /**
     * The SAS blob (datalake file) constant.
     */
    private static final String SAS_BLOB_CONSTANT = "b";

    /**
     * The SAS directory (datalake directory) constant.
     */
    private static final String SAS_DIRECTORY_CONSTANT = "d";

    /**
     * The SAS blob container (datalake file system) constant.
     */
    private static final String SAS_CONTAINER_CONSTANT = "c";

    private static final ClientLogger LOGGER = new ClientLogger(DataLakeSasImplUtil.class);

    private static final String VERSION = Configuration.getGlobalConfiguration()
        .get(Constants.PROPERTY_AZURE_STORAGE_SAS_SERVICE_VERSION, DataLakeServiceVersion.getLatest().getVersion());

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String fileSystemName;

    private String pathName;

    private String resource;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    private Boolean isDirectory;

    private Integer directoryDepth;

    private String authorizedAadObjectId;

    private String unauthorizedAadObjectId;

    private String correlationId;

    /**
     * Creates a new {@link DataLakeSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link DataLakeServiceSasSignatureValues}
     * @param fileSystemName The file system name
     */
    public DataLakeSasImplUtil(DataLakeServiceSasSignatureValues sasValues, String fileSystemName) {
        this(sasValues, fileSystemName, null, false);
    }

    /**
     * Creates a new {@link DataLakeSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link DataLakeServiceSasSignatureValues}
     * @param fileSystemName The file system name
     * @param pathName The path name
     * @param isDirectory Whether or not the path points to a directory.
     */
    public DataLakeSasImplUtil(DataLakeServiceSasSignatureValues sasValues, String fileSystemName, String pathName,
        boolean isDirectory) {
        Objects.requireNonNull(sasValues);
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.fileSystemName = fileSystemName;
        this.pathName = pathName;
        this.identifier = sasValues.getIdentifier();
        this.cacheControl = sasValues.getCacheControl();
        this.contentDisposition = sasValues.getContentDisposition();
        this.contentEncoding = sasValues.getContentEncoding();
        this.contentLanguage = sasValues.getContentLanguage();
        this.contentType = sasValues.getContentType();
        this.authorizedAadObjectId = sasValues.getPreauthorizedAgentObjectId();
        this.unauthorizedAadObjectId = sasValues.getAgentObjectId();
        this.correlationId = sasValues.getCorrelationId();
        this.isDirectory = isDirectory;
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
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_AGENT_OBJECT_ID, this.unauthorizedAadObjectId);
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CORRELATION_ID, this.correlationId);
        }

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_RESOURCE, this.resource);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);

        if (this.isDirectory) {
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_DIRECTORY_DEPTH, this.directoryDepth);
        }

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

     * 1. If there is no identifier set, ensure expiryTime and permissions are set.
     * 2. Resource name is chosen by:
     *    a. If "BlobName" is _not_ set, it is a container resource.
     *    b. Otherwise, if "SnapshotId" is set, it is a blob snapshot resource.
     *    c. Otherwise, if "VersionId" is set, it is a blob version resource.
     *    d. Otherwise, it is a blob resource.
     * 3. Reparse permissions depending on what the resource is. If it is an unrecognized resource, do nothing.
     * 4. Ensure saoid is not set when suoid is set and vice versa.
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

        if (CoreUtils.isNullOrEmpty(pathName)) {
            resource = SAS_CONTAINER_CONSTANT;
        } else {
            if (isDirectory) {
                resource = SAS_DIRECTORY_CONSTANT;
                this.directoryDepth = pathName.split("/").length;
            } else {
                resource = SAS_BLOB_CONSTANT;
            }
        }

        if (permissions != null) {
            switch (resource) {
                case SAS_BLOB_CONSTANT:
                case SAS_DIRECTORY_CONSTANT:
                    permissions = PathSasPermission.parse(permissions).toString();
                    break;
                case SAS_CONTAINER_CONSTANT:
                    permissions = FileSystemSasPermission.parse(permissions).toString();
                    break;
                default:
                    // We won't reparse the permissions if we don't know the type.
                    LOGGER.info("Not re-parsing permissions. Resource type '{}' is unknown.", resource);
                    break;
            }
        }

        if (this.authorizedAadObjectId != null && this.unauthorizedAadObjectId != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("agentObjectId and preauthorizedAgentObjectId "
                + "can not both be set."));
        }
    }

    /**
     * Computes the canonical name for a container or blob resource for SAS signing.
     */
    private String getCanonicalName(String account) {
        // Container: "/blob/account/containername"
        // Blob:      "/blob/account/containername/blobname"
        return CoreUtils.isNullOrEmpty(pathName)
            ? String.format("/blob/%s/%s", account, fileSystemName)
            : String.format("/blob/%s/%s/%s", account, fileSystemName, pathName.replace("\\", "/"));
    }

    private String stringToSign(String canonicalName) {
        if (VERSION.compareTo(DataLakeServiceVersion.V2020_10_02.getVersion()) <= 0) {
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
                "", /* Version segment. */
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        } else {
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
                "", /* Version segment. */
                "", // encryptionScope
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        }
    }

    private String stringToSign(final UserDelegationKey key, String canonicalName) {
        if (VERSION.compareTo(DataLakeServiceVersion.V2019_12_12.getVersion()) <= 0) {
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
                "", /* Version segment. */
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        } if (VERSION.compareTo(DataLakeServiceVersion.V2020_10_02.getVersion()) <= 0) {
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
                this.unauthorizedAadObjectId == null ? "" : this.unauthorizedAadObjectId,
                this.correlationId == null ? "" : this.correlationId,
                this.sasIpRange == null ? "" : this.sasIpRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                VERSION,
                resource,
                "", /* Version segment. */
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        }
        else {
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
                this.unauthorizedAadObjectId == null ? "" : this.unauthorizedAadObjectId,
                this.correlationId == null ? "" : this.correlationId,
                this.sasIpRange == null ? "" : this.sasIpRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                VERSION,
                resource,
                "", /* Version segment. */
                "", /* Encryption scope. */
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
            );
        }
    }
}
