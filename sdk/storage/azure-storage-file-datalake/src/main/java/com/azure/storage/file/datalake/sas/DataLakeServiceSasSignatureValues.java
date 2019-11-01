// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.sas;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.datalake.DataLakeServiceVersion;
import com.azure.storage.file.datalake.models.UserDelegationKey;

import java.time.OffsetDateTime;

/**
 * Used to generate a Shared Access Signature (SAS) for an Azure Data Lake Storage service. Once all the values here
 * are set, call {@link
 * #generateSasQueryParameters(StorageSharedKeyCredential) generateSasQueryParameters(StorageSharedKeyCredential)} or
 * {@link #generateSasQueryParameters(UserDelegationKey, String) generateSasQueryParameters(UserDelegationKey, String)}
 * to obtain a representation of the SAS which can be applied to dfs urls.
 *
 * <p><strong>Generating SAS query parameters with {@link StorageSharedKeyCredential}</strong></p>
 * The following code generates SAS query parameters for an Azure storage path.
 * <p>
 * {@codesnippet com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential}
 *
 * <p><strong>Generating SAS query parameters with {@link UserDelegationKey}</strong></p>
 * The following sample generates SAS query parameters for an Azure storage file system.
 * <p>
 * {@codesnippet com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues.generateSasQueryParameters#UserDelegationKey-String}
 *
 * @see DataLakeServiceSasQueryParameters
 * @see <a href=https://docs.microsoft.com/en-ca/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service
 * SAS</a>
 */
public final class DataLakeServiceSasSignatureValues {

    /**
     * The SAS blob constant.
     */
    private static final String SAS_BLOB_CONSTANT = "b";

    /**
     * The SAS blob container constant.
     */
    private static final String SAS_CONTAINER_CONSTANT = "c";

    private final ClientLogger logger = new ClientLogger(DataLakeServiceSasSignatureValues.class);

    private String version;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String fileSystemName;

    private String pathName;

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
     */
    public DataLakeServiceSasSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime Time the SAS becomes valid
     * @param permissions Permissions granted by the SAS
     */
    DataLakeServiceSasSignatureValues(OffsetDateTime expiryTime, String permissions) {
        this.expiryTime = expiryTime;
        this.permissions = permissions;
    }

    /**
     * Creates an object with the specified identifier
     *
     * @param identifier Identifier for the SAS
     */
    DataLakeServiceSasSignatureValues(String identifier) {
        this.identifier = identifier;
    }

    public DataLakeServiceSasSignatureValues(String version, SasProtocol sasProtocol, OffsetDateTime startTime,
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setVersion(String version) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setProtocol(SasProtocol protocol) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to either {@link FileSystemSasPermission} or
     * {@link PathSasPermission} depending on the resource being accessed for help determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the Path permissions allowed by the SAS.
     *
     * @param permissions {@link PathSasPermission}
     * @return the updated DataLakeServiceSasSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public DataLakeServiceSasSignatureValues setPermissions(PathSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.permissions = permissions.toString();
        return this;
    }

    /**
     * Sets the File System permissions allowed by the SAS.
     *
     * @param permissions {@link FileSystemSasPermission}
     * @return the updated DataLakeServiceSasSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public DataLakeServiceSasSignatureValues setPermissions(FileSystemSasPermission permissions) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * Gets the name of the file system the SAS user may access.
     *
     * @return The name of the file system the SAS user may access.
     */
    public String getFileSystemName() {
        return fileSystemName;
    }

    /**
     * Sets the container the SAS user may access.
     *
     * @param fileSystemName The name of the file system.
     * @return The updated DataLakeServiceSasSignatureValues object.
     */
    public DataLakeServiceSasSignatureValues setFileSystemName(String fileSystemName) {
        this.fileSystemName = fileSystemName;
        return this;
    }

    /**
     * Decodes and gets the name of the path the SAS user may access. {@code null} or an empty string is returned when a
     * creating a file system SAS.
     *
     * @return The decoded name of the path the SAS user may access. {@code null} or an empty string is returned when a
     * creating a file system SAS.
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * Sets the path the SAS user may access. Use {@code null} or an empty string to create a file system SAS.
     *
     * @param pathName The name of the path. Use {@code null} or an empty string to create a file system SAS.
     * @return The updated DataLakeServiceSasSignatureValues object.
     */
    public DataLakeServiceSasSignatureValues setPathName(String pathName) {
        this.pathName = (pathName == null) ? null : Utility.urlDecode(pathName);
        return this;
    }

    /**
     * @return the name of the access policy on the file system this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the file system this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     *
     * @param identifier Name of the access policy
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setIdentifier(String identifier) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setCacheControl(String cacheControl) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentDisposition(String contentDisposition) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentEncoding(String contentEncoding) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentLanguage(String contentLanguage) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentType(String contentType) {
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
     * <li>If {@link #setVersion(String) version} is not set, the {@link DataLakeServiceVersion#getLatest() latest
     * service version} is used.</li>
     * <li>If {@link #setIdentifier(String) identifier} is set, {@link #setExpiryTime(OffsetDateTime) expiryTime} and
     * permissions should not be set. These values are inherited from the stored access policy.</li>
     * <li>Otherwise, {@link #setExpiryTime(OffsetDateTime) expiryTime} and {@link #getPermissions() permissions} must
     * be set.</li>
     * </ul>
     *
     * <p>
     * The type of SAS query parameters returned depends on the following:
     * <ol>
     *     <li>If {@link #getPathName()} is not set, <b>file system SAS</b> query parameters are returned.</li>
     *     <li>If only {@link #getPathName()} is set, <b>path SAS</b> query parameters are returned.</li>
     * </ol>
     *
     *  See class level JavaDocs for code snippets.
     *
     * @param storageSharedKeyCredentials A {@link StorageSharedKeyCredential} object used to sign the SAS values.
     * @return {@link DataLakeServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws IllegalArgumentException if {@link #getPermissions()} contains an invalid character for the SAS resource.
     * @throws NullPointerException if {@code storageSharedKeyCredentials} is null.
     */
    public DataLakeServiceSasQueryParameters generateSasQueryParameters(
        StorageSharedKeyCredential storageSharedKeyCredentials) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName());
        final String signature = storageSharedKeyCredentials.computeHmac256(stringToSign(canonicalName));

        return new DataLakeServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, this.identifier, this.resource, this.permissions, signature, this.cacheControl,
            this.contentDisposition, this.contentEncoding, this.contentLanguage, this.contentType, null /* delegate */);
    }

    /**
     * Uses a user delegation key to sign these signature values to produce the proper SAS query parameters.
     *
     * <p><strong>Notes on SAS generation</strong></p>
     * <p>
     * <ul>
     * <li>If {@link #setVersion(String) version} is not set, the {@link DataLakeServiceVersion#getLatest() latest
     * service version} is used.</li>
     * <li>If {@link #setIdentifier(String) identifier} is set, {@link #setExpiryTime(OffsetDateTime) expiryTime} and
     * permissions should not be set. These values are inherited from the stored access policy.</li>
     * <li>Otherwise, {@link #setExpiryTime(OffsetDateTime) expiryTime} and {@link #getPermissions() permissions} must
     * be set.</li>
     * </ul>
     *
     * <p>
     * The type of SAS query parameters returned depends on the following:
     * <ol>
     *     <li>If {@link #getPathName()} is not set, <b>file system SAS</b> query parameters are returned.</li>
     *     <li>If only {@link #getPathName()} is set, <b>path SAS</b> query parameters are returned.</li>
     * </ol>
     *
     *  See class level JavaDocs for code snippets.
     *
     * @param delegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @param accountName Azure Storage account name to generate SAS for.
     * @return {@link DataLakeServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws IllegalArgumentException if {@link #getPermissions()} contains an invalid character for the SAS resource.
     * @throws NullPointerException if {@code delegationKey} or {@code account} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-user-delegation-sas">
     *     Create a user delegation SAS</a>
     */
    public DataLakeServiceSasQueryParameters generateSasQueryParameters(UserDelegationKey delegationKey,
        String accountName) {
        StorageImplUtils.assertNotNull("delegationKey", delegationKey);
        StorageImplUtils.assertNotNull("accountName", accountName);

        ensureState();

        // Signature is generated on the un-url-encoded values.
        final String canonicalName = getCanonicalName(accountName);
        String signature = StorageImplUtils.computeHMac256(
            delegationKey.getValue(), stringToSign(delegationKey, canonicalName));


        return new DataLakeServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, null /* identifier */, this.resource, this.permissions, signature,
            this.cacheControl, this.contentDisposition, this.contentEncoding, this.contentLanguage, this.contentType,
            delegationKey);
    }

    /**
     * Ensures that the builder's properties are in a consistent state.

     * 1. If there is no version, use latest.
     * 2. Resource name is chosen by:
     *    a. If "pathName" is _not_ set, it is a file system resource.
     *    b. Otherwise, it is a path resource.
     * 3. Reparse permissions depending on what the resource is. If it is an unrecognised resource, do nothing.
     *
     * Taken from:
     * https://github.com/Azure/azure-storage-blob-go/blob/master/azblob/sas_service.go#L33
     * https://github.com/Azure/azure-sdk-for-net/blob/master/sdk/storage/Azure.Storage.Blobs/src/Sas/BlobSasBuilder.cs
     */
    private void ensureState() {
        if (version == null) {
            version = DataLakeServiceVersion.getLatest().getVersion();
        }

        if (ImplUtils.isNullOrEmpty(pathName)) {
            resource = SAS_CONTAINER_CONSTANT;
        } else {
            resource = SAS_BLOB_CONSTANT;
        }

        if (permissions != null) {
            switch (resource) {
                case SAS_BLOB_CONSTANT:
                    permissions = PathSasPermission.parse(permissions).toString();
                    break;
                case SAS_CONTAINER_CONSTANT:
                    permissions = FileSystemSasPermission.parse(permissions).toString();
                    break;
                default:
                    // We won't reparse the permissions if we don't know the type.
                    logger.info("Not re-parsing permissions. Resource type '{}' is unknown.", resource);
                    break;
            }
        }
    }

    /**
     * Computes the canonical name for a file system or path resource for SAS signing.
     */
    private String getCanonicalName(String account) {
        // File System: "/blob/account/containername"
        // Path:      "/blob/account/containername/blobname"
        return ImplUtils.isNullOrEmpty(pathName)
            ? String.format("/blob/%s/%s", account, fileSystemName)
            : String.format("/blob/%s/%s/%s", account, fileSystemName, pathName.replace("\\", "/"));
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
