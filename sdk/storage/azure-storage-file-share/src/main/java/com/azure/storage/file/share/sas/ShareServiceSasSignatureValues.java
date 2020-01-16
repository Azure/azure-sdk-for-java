// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.sas;

import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.share.ShareServiceVersion;

import java.time.OffsetDateTime;

/**
 * Used to initialize parameters for a Shared Access Signature (SAS) for an Azure File Storage service. Once all the
 * values here are set, use the appropriate SAS generation method on the desired share/file/directory client to obtain a
 * representation of the SAS which can then be applied to a new client using the .sasToken(String) method on the
 * desired client builder.
 *
 * @see <a href=https://docs.microsoft.com/en-ca/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service
 * SAS</a>
 */
public final class ShareServiceSasSignatureValues {
    /**
     * The SAS file constant.
     */
    private static final String SAS_FILE_CONSTANT = "f";

    /**
     * The SAS share constant.
     */
    private static final String SAS_SHARE_CONSTANT = "s";

    private String version;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String shareName;

    private String filePath;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    /**
     * Creates an object with empty values for all fields.
     * @deprecated Please use {@link #ShareServiceSasSignatureValues(String)},
     * {@link #ShareServiceSasSignatureValues(OffsetDateTime, ShareSasPermission)}, or
     * {@link #ShareServiceSasSignatureValues(OffsetDateTime, ShareFileSasPermission)}
     */
    @Deprecated
    public ShareServiceSasSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link ShareSasPermission} allowed by the SAS.
     */
    public ShareServiceSasSignatureValues(OffsetDateTime expiryTime, ShareSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link ShareFileSasPermission} allowed by the SAS.
     */
    public ShareServiceSasSignatureValues(OffsetDateTime expiryTime, ShareFileSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified identifier.
     *
     * @param identifier Name of the access policy.
     */
    public ShareServiceSasSignatureValues(String identifier) {
        StorageImplUtils.assertNotNull("identifier", identifier);
        this.identifier = identifier;
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setVersion(String version) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setProtocol(SasProtocol protocol) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to either {@link ShareSasPermission} or {@link
     * ShareFileSasPermission} depending on the resource being accessed for help determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions allowed by the SAS. Share SASs are created when <b>only</b>
     * {@link #setShareName(String) share name} is set on the builder.
     *
     * @param permissions Permissions for the share SAS.
     * @return the updated FileServiceSasSignatureValues object.
     */
    public ShareServiceSasSignatureValues setPermissions(ShareSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.permissions = permissions.toString();
        return this;
    }

    /**
     * Sets the permissions allowed by the SAS. Share file SASs are created when both a
     * {@link #setShareName(String) share name} and {@link #setFilePath(String) file path} are set on the builder.
     *
     * @param permissions Permissions for the SAS.
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setPermissions(ShareFileSasPermission permissions) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * Gets the name of the share being made accessible.
     *
     * @return The name of the share being made accessible.
     * @deprecated Share name is now auto-populated by the SAS generation methods provided on the desired
     * share/file/directory client.
     */
    @Deprecated
    public String getShareName() {
        return shareName;
    }

    /**
     * Sets the name of the share being made accessible.
     *
     * @param shareName The name of the share being made accessible.
     * @return the updated FileServiceSasSignatureValues object
     * @deprecated Please use the generateSas methods provided on the desired share/file/directory client that will
     * auto-populate the share name.
     */
    @Deprecated
    public ShareServiceSasSignatureValues setShareName(String shareName) {
        this.shareName = shareName;
        return this;
    }

    /**
     * Gets the path of the file or directory being made accessible. {@code null} or an empty string for a share SAS.
     *
     * @return The path of the file or directory being made accessible. {@code null} or an empty string for a share SAS.
     * @deprecated File path is now auto-populated by the SAS generation methods provided on the desired file/directory
     * client.
     */
    @Deprecated
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the path of the file or directory being made accessible. Pass in {@code null} or an empty string for a share
     * SAS.
     *
     * @param filePath The name of the share being made accessible.
     * @return the updated FileServiceSasSignatureValues object
     * @deprecated Please use the generateSas methods provided on the desired file/directory client that will
     * auto-populate the file path.
     */
    @Deprecated
    public ShareServiceSasSignatureValues setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * @return the name of the access policy on the share this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">
     * Establishing a stored access policy</a> for more information.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the share this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">
     * Establishing a stored access policy</a> for more information.
     *
     * @param identifier Name of the stored access policy
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setIdentifier(String identifier) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setCacheControl(String cacheControl) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setContentDisposition(String contentDisposition) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setContentEncoding(String contentEncoding) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setContentLanguage(String contentLanguage) {
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
     * @return the updated FileServiceSasSignatureValues object
     */
    public ShareServiceSasSignatureValues setContentType(String contentType) {
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
     * <li>If {@link #setVersion(String) version} is not set, the {@link ShareServiceVersion#getLatest() latest service
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
     *     <li>If {@link #getFilePath()} is not set, query parameters for a <b>share SAS</b> are returned.</li>
     *     <li>Otherwise, {@link #getShareName()} and {@link #getFilePath()} are used to create query parameters for a
     *     <b>file SAS</b>.</li>
     * </ol>
     *
     * <p>See class level JavaDocs for code snippets.</p>
     *
     * @param storageSharedKeyCredentials A {@link StorageSharedKeyCredential} object used to sign the SAS values.
     * @return {@link ShareServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws IllegalArgumentException if {@link #getPermissions()} contains an invalid character for the SAS resource.
     * @throws NullPointerException If {@code storageSharedKeyCredentials} is null.
     * @deprecated Please use the generateSas(ShareServiceSasSignatureValues) method on the desired share/file/directory
     * client after initializing {@link ShareServiceSasSignatureValues}.
     */
    @Deprecated
    public ShareServiceSasQueryParameters generateSasQueryParameters(
        StorageSharedKeyCredential storageSharedKeyCredentials) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        final String resource;
        if (CoreUtils.isNullOrEmpty(filePath)) {
            resource = SAS_SHARE_CONSTANT;

            // Make sure the permission characters are in the correct order
            if (permissions != null) {
                permissions = ShareSasPermission.parse(permissions).toString();
            }
        } else {
            resource = SAS_FILE_CONSTANT;

            // Make sure the permission characters are in the correct order
            if (permissions != null) {
                permissions = ShareFileSasPermission.parse(permissions).toString();
            }
        }

        if (CoreUtils.isNullOrEmpty(version)) {
            version = ShareServiceVersion.getLatest().getVersion();
        }

        // Signature is generated on the un-url-encoded values.
        String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName(), shareName, filePath);
        String stringToSign = stringToSign(canonicalName);
        String signature = storageSharedKeyCredentials.computeHmac256(stringToSign);

        return new ShareServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, this.identifier, resource, this.permissions, signature, this.cacheControl,
            this.contentDisposition, this.contentEncoding, this.contentLanguage, this.contentType);
    }

    /**
     * Computes the canonical name for a share or file resource for SAS signing.
     * Share: "/file/account/sharename"
     * File: "/file/account/sharename/filename"
     * File: "/file/account/sharename/directoryname/filename"
     *
     * @param account The name of the storage account.
     * @param shareName The name of the share.
     * @param filePath The path of the file.
     * @return The canonical resource name.
     */
    private static String getCanonicalName(String account, String shareName, String filePath) {
        return !CoreUtils.isNullOrEmpty(filePath)
            ? String.format("/file/%s/%s/%s", account, shareName, filePath.replace("\\", "/"))
            : String.format("/file/%s/%s", account, shareName);
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
            this.version == null ? "" : this.version,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }
}
