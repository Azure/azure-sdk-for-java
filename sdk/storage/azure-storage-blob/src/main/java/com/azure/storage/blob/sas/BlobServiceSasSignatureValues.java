// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.sas;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;

import java.time.OffsetDateTime;

/**
 * Used to initialize parameters for a Shared Access Signature (SAS) for an Azure Blob Storage service. Once all the
 * values here are set, use the appropriate SAS generation method on the desired container/blob client to obtain a
 * representation of the SAS which can then be applied to a new client using the .sasToken(String) method on the
 * desired client builder.
 *
 * @see <a href=https://docs.microsoft.com/en-ca/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service
 * SAS</a>
 * @see <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas>Constructing a
 * User Delegation SAS</a>
 */
public final class BlobServiceSasSignatureValues {
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

    private final ClientLogger logger = new ClientLogger(BlobServiceSasSignatureValues.class);

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
     * Creates an object with empty values for all fields.
     * @deprecated Please use {@link #BlobServiceSasSignatureValues(String)},
     * {@link #BlobServiceSasSignatureValues(OffsetDateTime, BlobSasPermission)}, or
     * {@link #BlobServiceSasSignatureValues(OffsetDateTime, BlobContainerSasPermission)}
     */
    @Deprecated
    public BlobServiceSasSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link BlobContainerSasPermission} allowed by the SAS.
     */
    public BlobServiceSasSignatureValues(OffsetDateTime expiryTime, BlobContainerSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime When the SAS will no longer work
     * @param permissions {@link BlobSasPermission} allowed by the SAS
     */
    public BlobServiceSasSignatureValues(OffsetDateTime expiryTime, BlobSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified identifier.
     * NOTE: Identifier can not be used for a {@link UserDelegationKey} SAS.
     *
     * @param identifier Name of the access policy
     */
    public BlobServiceSasSignatureValues(String identifier) {
        StorageImplUtils.assertNotNull("identifier", identifier);
        this.identifier = identifier;
    }

    /**
     * Creates an object with the specified values.
     *
     * @param version The version of the service this SAS will target. If not specified, it will default to the version
     *    targeted by the library.
     * @param sasProtocol The {@link SasProtocol} which determines the protocols allowed by the SAS.
     * @param startTime When the SAS will take effect.
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permission The permissions string allowed by the SAS.
     * @param sasIpRange The {@link SasIpRange} which determines the IP ranges that are allowed to use the SAS.
     * @param identifier The name of the access policy on the container this SAS references if any.
     * @param cacheControl The cache-control header for the SAS.
     * @param contentDisposition The content-disposition header for the SAS.
     * @param contentEncoding The content-encoding header for the SAS.
     * @param contentLanguage The content-language header for the SAS.
     * @param contentType The content-type header for the SAS.
     * @deprecated Please use {@link #BlobServiceSasSignatureValues(String)},
     * {@link #BlobServiceSasSignatureValues(OffsetDateTime, BlobSasPermission)}, or
     * {@link #BlobServiceSasSignatureValues(OffsetDateTime, BlobContainerSasPermission)}
     * followed by calls to the desired setters.
     */
    @Deprecated
    public BlobServiceSasSignatureValues(String version, SasProtocol sasProtocol, OffsetDateTime startTime,
        OffsetDateTime expiryTime, String permission, SasIpRange sasIpRange, String identifier, String cacheControl,
        String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {
        if (version != null) {
            this.version = version;
        }
        this.protocol = sasProtocol;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
        this.permissions = permission;
        this.sasIpRange = sasIpRange;
        this.identifier = identifier;
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.contentEncoding = contentEncoding;
        this.contentLanguage = contentLanguage;
        this.contentType = contentType;
    }

    /**
     * @return the version of the service this SAS will target. If not specified, it will default to the version
     * targeted by the library.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the service this SAS will target. If not specified, it will default to the version targeted
     * by the library.
     *
     * @param version Version to target
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SasProtocol} which determines the protocols allowed by the SAS.
     */
    public SasProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link SasProtocol} which determines the protocols allowed by the SAS.
     *
     * @param protocol Protocol for the SAS
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setProtocol(SasProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * @return when the SAS will take effect.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets when the SAS will take effect.
     *
     * @param startTime When the SAS takes effect
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * @return the time after which the SAS will no longer work.
     */
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    /**
     * Sets the time after which the SAS will no longer work.
     *
     * @param expiryTime When the SAS will no longer work
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to either {@link BlobContainerSasPermission} or
     * {@link BlobSasPermission} depending on the resource being accessed for help determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the Blob permissions allowed by the SAS.
     *
     * @param permissions {@link BlobSasPermission}
     * @return the updated BlobServiceSASSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public BlobServiceSasSignatureValues setPermissions(BlobSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.permissions = permissions.toString();
        return this;
    }

    /**
     * Sets the Container permissions allowed by the SAS.
     *
     * @param permissions {@link BlobContainerSasPermission}
     * @return the updated BlobServiceSASSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public BlobServiceSasSignatureValues setPermissions(BlobContainerSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.permissions = permissions.toString();
        return this;
    }

    /**
     * @return the {@link SasIpRange} which determines the IP ranges that are allowed to use the SAS.
     */
    public SasIpRange getSasIpRange() {
        return sasIpRange;
    }

    /**
     * Sets the {@link SasIpRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @param sasIpRange Allowed IP range to set
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * Gets the name of the container the SAS user may access.
     *
     * @return The name of the container the SAS user may access.
     * @deprecated Container name is now auto-populated by the SAS generation methods provided on the desired
     * container/blob client.
     */
    @Deprecated
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the container the SAS user may access.
     *
     * @param containerName The name of the container.
     * @return The updated BlobServiceSASSignatureValues object.
     * @deprecated Please use the SAS generation methods provided on the desired container/blob client that will
     * auto-populate the container name.
     */
    @Deprecated
    public BlobServiceSasSignatureValues setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Decodes and gets the name of the blob the SAS user may access. {@code null} or an empty string is returned when a
     * creating a container SAS.
     *
     * @return The decoded name of the blob the SAS user may access. {@code null} or an empty string is returned when a
     * creating a container SAS.
     * @deprecated Blob name is now auto-populated by the SAS generation methods provided on the desired blob client.
     */
    @Deprecated
    public String getBlobName() {
        return blobName;
    }

    /**
     * Sets the blob the SAS user may access. Use {@code null} or an empty string to create a container SAS.
     *
     * @param blobName The name of the blob. Use {@code null} or an empty string to create a container SAS.
     * @return The updated BlobServiceSASSignatureValues object.
     * @deprecated Please use the SAS generation methods provided on the desired blob client that will auto-populate the
     * blob name.
     */
    @Deprecated
    public BlobServiceSasSignatureValues setBlobName(String blobName) {
        this.blobName = (blobName == null) ? null : Utility.urlDecode(blobName);
        return this;
    }

    /**
     * @return the specific snapshot the SAS user may access.
     * @deprecated Snapshot id is now auto-populated by the SAS generation methods provided on the desired (snapshot)
     * blob client.
     */
    @Deprecated
    public String getSnapshotId() {
        return this.snapshotId;
    }

    /**
     * Sets the specific snapshot the SAS user may access.
     *
     * <p>{@link #resource} will be set to {@link #SAS_BLOB_SNAPSHOT_CONSTANT} if the passed {@code snapshotId} isn't
     * {@code null} amd {@link #resource} is set to {@link #SAS_BLOB_CONSTANT}.</p>
     *
     * @param snapshotId Identifier of the snapshot
     * @return the updated BlobServiceSASSignatureValues object
     * @deprecated Please use the SAS generation methods provided on the desired (snapshot) blob client that will
     * auto-populate the snapshot id.
     */
    @Deprecated
    public BlobServiceSasSignatureValues setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
        if (snapshotId != null && SAS_BLOB_CONSTANT.equals(resource)) {
            this.resource = SAS_BLOB_SNAPSHOT_CONSTANT;
        }
        return this;
    }

    /**
     * @return the name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     *
     * @param identifier Name of the access policy
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @return the cache-control header for the SAS.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Sets the cache-control header for the SAS.
     *
     * @param cacheControl Cache-Control header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * @return the content-disposition header for the SAS.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Sets the content-disposition header for the SAS.
     *
     * @param contentDisposition Content-Disposition header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * @return the content-encoding header for the SAS.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the content-encoding header for the SAS.
     *
     * @param contentEncoding Content-Encoding header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * @return the content-language header for the SAS.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the content-language header for the SAS.
     *
     * @param contentLanguage Content-Language header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * @return the content-type header for the SAS.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content-type header for the SAS.
     *
     * @param contentType Content-Type header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSasSignatureValues setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * <p><strong>Notes on SAS generation</strong></p>
     * <p>
     * <ul>
     * <li>If {@link #setVersion(String) version} is not set, the {@link BlobServiceVersion#getLatest() latest service
     * version} is used.</li>
     * <li>If {@link #setIdentifier(String) identifier} is set, {@link #setExpiryTime(OffsetDateTime) expiryTime} and
     * permissions should not be set. These values are inherited from the stored access policy.</li>
     * <li>Otherwise, {@link #setExpiryTime(OffsetDateTime) expiryTime} and {@link #getPermissions() permissions} must
     * be set.</li>
     * </ul>
     *
     * <p>
     * The type of SAS query parameters returned depends on the following:
     * <ol>
     *     <li>If {@link #getBlobName()} is not set, <b>container SAS</b> query parameters are returned.</li>
     *     <li>If {@link #getBlobName()} and {@link #getSnapshotId()} are set, <b>blob snapshot</b> SAS query parameters
     *     are returned.</li>
     *     <li>If only {@link #getBlobName()} is set, <b>blob SAS</b> query parameters are returned.</li>
     * </ol>
     *
     *  See class level JavaDocs for code snippets.
     *
     * @param storageSharedKeyCredentials A {@link StorageSharedKeyCredential} object used to sign the SAS values.
     * @return {@link BlobServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws IllegalArgumentException if {@link #getPermissions()} contains an invalid character for the SAS resource.
     * @throws NullPointerException if {@code storageSharedKeyCredentials} is null.
     * @deprecated Please use the generateSas(BlobServiceSasSignatureValues) method on the desired container/blob client
     * after initializing {@link BlobServiceSasSignatureValues}.
     */
    @Deprecated
    public BlobServiceSasQueryParameters generateSasQueryParameters(
        StorageSharedKeyCredential storageSharedKeyCredentials) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName());
        final String signature = storageSharedKeyCredentials.computeHmac256(stringToSign(canonicalName));

        return new BlobServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, this.identifier, this.resource, this.permissions, signature, this.cacheControl,
            this.contentDisposition, this.contentEncoding, this.contentLanguage, this.contentType, null /* delegate */);
    }

    /**
     * Uses a user delegation key to sign these signature values to produce the proper SAS query parameters.
     *
     * <p><strong>Notes on SAS generation</strong></p>
     * <p>
     * <ul>
     * <li>If {@link #setVersion(String) version} is not set, the {@link BlobServiceVersion#getLatest() latest service
     * version} is used.</li>
     * <li>If {@link #setIdentifier(String) identifier} is set, {@link #setExpiryTime(OffsetDateTime) expiryTime} and
     * permissions should not be set. These values are inherited from the stored access policy.</li>
     * <li>Otherwise, {@link #setExpiryTime(OffsetDateTime) expiryTime} and {@link #getPermissions() permissions} must
     * be set.</li>
     * </ul>
     *
     * <p>
     * The type of SAS query parameters returned depends on the following:
     * <ol>
     *     <li>If {@link #getBlobName()} is not set, <b>container SAS</b> query parameters are returned.</li>
     *     <li>If {@link #getBlobName()} and {@link #getSnapshotId()} are set, <b>blob snapshot</b> SAS query parameters
     *     are returned.</li>
     *     <li>If only {@link #getBlobName()} is set, <b>blob SAS</b> query parameters are returned.</li>
     * </ol>
     *
     *  See class level JavaDocs for code snippets.
     *
     * @param delegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @param accountName Azure Storage account name to generate SAS for.
     * @return {@link BlobServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws IllegalArgumentException if {@link #getPermissions()} contains an invalid character for the SAS resource.
     * @throws NullPointerException if {@code delegationKey} or {@code account} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-user-delegation-sas">
     *     Create a user delegation SAS</a>
     * @deprecated Please use the generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey) method on
     * the desired container/blob client after initializing {@link BlobServiceSasSignatureValues}.
     */
    @Deprecated
    public BlobServiceSasQueryParameters generateSasQueryParameters(UserDelegationKey delegationKey,
        String accountName) {
        StorageImplUtils.assertNotNull("delegationKey", delegationKey);
        StorageImplUtils.assertNotNull("accountName", accountName);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(accountName);
        String signature = StorageImplUtils.computeHMac256(
            delegationKey.getValue(), stringToSign(delegationKey, canonicalName));


        return new BlobServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, null /* identifier */, this.resource, this.permissions, signature,
            this.cacheControl, this.contentDisposition, this.contentEncoding, this.contentLanguage, this.contentType,
            delegationKey);
    }

    /**
     * Ensures that the builder's properties are in a consistent state.

     * 1. If there is no version, use latest.
     * 2. Resource name is chosen by:
     *    a. If "BlobName" is _not_ set, it is a container resource.
     *    b. Otherwise, if "SnapshotId" is set, it is a blob snapshot resource.
     *    c. Otherwise, it is a blob resource.
     * 3. Reparse permissions depending on what the resource is. If it is an unrecognised resource, do nothing.
     *
     * Taken from:
     * https://github.com/Azure/azure-storage-blob-go/blob/master/azblob/sas_service.go#L33
     * https://github.com/Azure/azure-sdk-for-net/blob/master/sdk/storage/Azure.Storage.Blobs/src/Sas/BlobSasBuilder.cs
     */
    private void ensureState() {
        if (version == null) {
            version = BlobServiceVersion.getLatest().getVersion();
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
