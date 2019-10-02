// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

/**
 * This class provides a fluent API to help aid in the creation of Azure Blob SAS Tokens.
 */
public class BlobSasConstructor {
    private final ClientLogger logger = new ClientLogger(BlobSasConstructor.class);

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
    private SASProtocol protocol;
    private OffsetDateTime startTime;
    private OffsetDateTime expiryTime;
    private IPRange ipRange;
    private String canonicalName;
    private String identifier;
    private String cacheControl;
    private String contentDisposition;
    private String contentEncoding;
    private String contentLanguage;
    private String contentType;

    /**
     * Construct an empty {@link BlobSasConstructor}.
     */
    public BlobSasConstructor() {
    }

    /**
     * Construct a Blob SAS token with the permissions granted by the {@link BlobSASPermission}.
     *
     * @param credential {@link SharedKeyCredential} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructBlobSasToken(SharedKeyCredential credential, BlobSASPermission permission) {
        return constructBlobSasToken(credential, permission, null);
    }

    /**
     * Construct a Blob SAS token with the permissions granted by the {@link BlobSASPermission}.
     *
     * @param key {@link UserDelegationKey} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructBlobSasToken(UserDelegationKey key, BlobSASPermission permission) {
        return constructBlobSasToken(key, permission, null);
    }

    /**
     * Construct a Blob SAS token with the permissions granted by the {@link BlobSASPermission} to a specific snapshot.
     *
     * @param credential {@link SharedKeyCredential} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @param snapshotId Snapshot that the SAS token will grant access.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructBlobSasToken(SharedKeyCredential credential, BlobSASPermission permission,
        String snapshotId) {
        assertGenerateOK(false, permission);

        String resource = (snapshotId == null)
            ? Constants.UrlConstants.SAS_BLOB_CONSTANT
            : Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT;
        String permissionString = permission.toString();

        // Signature is generated on the un-url-encoded values.
        String signature = credential.computeHmac256(stringToSign(permissionString, resource, snapshotId));

        return construct(null, resource, permissionString, signature);
    }

    /**
     * Construct a Blob SAS token with the permissions granted by the {@link BlobSASPermission} to a specific snapshot.
     *
     * @param key {@link UserDelegationKey} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @param snapshotId Snapshot that the SAS token will grant access.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructBlobSasToken(UserDelegationKey key, BlobSASPermission permission, String snapshotId) {
        assertGenerateOK(true, permission);

        String resource = (snapshotId == null)
            ? Constants.UrlConstants.SAS_BLOB_CONSTANT
            : Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT;
        String permissionString = permission.toString();

        // Signature is generated on the un-url-encoded values.
        String signature = Utility.computeHMac256(key.getValue(), stringToSign(key, resource, permissionString, null));

        return construct(key, resource, permissionString, signature);
    }

    /**
     * Constructs a Container SAS token with the permissions granted by the {@link ContainerSASPermission}.
     *
     * @param credential {@link SharedKeyCredential} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructContainerSasToken(SharedKeyCredential credential, ContainerSASPermission permission) {
        assertGenerateOK(false, permission);

        String resource = Constants.UrlConstants.SAS_CONTAINER_CONSTANT;
        String permissionString = permission.toString();

        // Signature is generated on the un-url-encoded values.
        String signature = credential.computeHmac256(stringToSign(permissionString, resource, null));

        return construct(null, resource, permissionString, signature);
    }

    /**
     * Constructs a Container SAS token with the permissions granted by the {@link ContainerSASPermission}.
     *
     * @param key {@link UserDelegationKey} used to sign the SAS token.
     * @param permission Permissions allowed by the SAS token.
     * @return a signed SAS token with the requested permissions.
     */
    public String constructContainerSasToken(UserDelegationKey key, ContainerSASPermission permission) {
        assertGenerateOK(true, permission);

        String resource = Constants.UrlConstants.SAS_CONTAINER_CONSTANT;
        String permissionString = permission.toString();

        // Signature is generated on the un-url-encoded values.
        String signature = Utility.computeHMac256(key.getValue(), stringToSign(key, resource, permissionString, null));

        return construct(key, resource, permissionString, signature);
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SASProtocol} which determines the protocols allowed by the SAS.
     */
    public SASProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link SASProtocol} which determines the protocols allowed by the SAS.
     *
     * @param protocol Protocol for the SAS
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setProtocol(SASProtocol protocol) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setStartTime(OffsetDateTime startTime) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the {@link IPRange} which determines the IP ranges that are allowed to use the SAS.
     */
    public IPRange getIpRange() {
        return ipRange;
    }

    /**
     * Sets the {@link IPRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @param ipRange Allowed IP range to set
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setIpRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * @return the canonical name of the object the SAS user may access.
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Sets the canonical name of the object the SAS user may access.
     *
     * @param canonicalName Canonical name of the object the SAS grants access
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
        return this;
    }

    /**
     * Sets the canonical name of the object the SAS user may access. Constructs a canonical name of
     * "/blob/{accountName}{Path of urlString}".
     *
     * @param urlString URL string that contains the path to the object
     * @param accountName Name of the account that contains the object
     * @return the updated BlobSasConstructor object
     * @throws RuntimeException If {@code urlString} is a malformed URL.
     */
    public BlobSasConstructor setCanonicalName(String urlString, String accountName) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }

        this.canonicalName = String.format("/blob/%s%s", accountName, url.getPath());
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setIdentifier(String identifier) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setCacheControl(String cacheControl) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setContentDisposition(String contentDisposition) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setContentEncoding(String contentEncoding) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setContentLanguage(String contentLanguage) {
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
     * @return the updated BlobSasConstructor object
     */
    public BlobSasConstructor setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    private void assertGenerateOK(boolean usingUserDelegation, Object permissions) {
        Utility.assertNotNull("version", this.version);
        Utility.assertNotNull("canonicalName", this.canonicalName);

        // If a UserDelegation key or a SignedIdentifier is not being used both expiryDate and permissions must be set.
        if (usingUserDelegation || identifier == null) {
            Utility.assertNotNull("expiryTime", this.expiryTime);
            Utility.assertNotNull("permissions", permissions);
        } else {
            // Otherwise a SignedIdentifier must be used.
            Utility.assertNotNull("identifier", this.identifier);
        }
    }

    private String stringToSign(String permissions, String resource, String snapshotId) {
        return String.join("\n",
            permissions == null ? "" : permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.canonicalName == null ? "" : this.canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.ipRange == null ? "" : this.ipRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version,
            resource == null ? "" : resource,
            snapshotId == null ? "" : snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }

    private String stringToSign(UserDelegationKey key, String permissions, String resource, String snapshotId) {
        return String.join("\n",
            permissions == null ? "" : permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.canonicalName == null ? "" : this.canonicalName,
            key.getSignedOid() == null ? "" : key.getSignedOid(),
            key.getSignedTid() == null ? "" : key.getSignedTid(),
            key.getSignedStart() == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedStart()),
            key.getSignedExpiry() == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(key.getSignedExpiry()),
            key.getSignedService() == null ? "" : key.getSignedService(),
            key.getSignedVersion() == null ? "" : key.getSignedVersion(),
            this.ipRange == null ? "" : this.ipRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            this.version == null ? "" : this.version,
            resource == null ? "" : resource,
            snapshotId == null ? "" : snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }

    private String construct(UserDelegationKey key, String resource, String permissions, String signature) {
        /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, this.version);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_START_TIME, formatQueryDate(this.startTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_EXPIRY_TIME, formatQueryDate(this.expiryTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_IP_RANGE, this.ipRange);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_IDENTIFIER, this.identifier);

        if (key != null) {
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_OBJECT_ID, key.getSignedOid());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_TENANT_ID, key.getSignedTid());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_START,
                formatQueryDate(key.getSignedStart()));
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_EXPIRY,
                formatQueryDate(key.getSignedExpiry()));
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_SERVICE, key.getSignedService());
            tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_VERSION, key.getSignedVersion());
        }

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_RESOURCE, resource);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, permissions);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNATURE, signature);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CACHE_CONTROL, this.cacheControl);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_DISPOSITION, this.contentDisposition);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_ENCODING, this.contentEncoding);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_LANGUAGE, this.contentLanguage);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_TYPE, this.contentType);

        return sb.toString();
    }

    private void tryAppendQueryParameter(StringBuilder sb, String param, Object value) {
        if (value != null) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append(Utility.urlEncode(param)).append('=').append(Utility.urlEncode(value.toString()));
        }
    }

    private String formatQueryDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return Utility.ISO_8601_UTC_DATE_FORMATTER.format(dateTime);
        }
    }
}
