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
 * BlobServiceSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage service. Once all
 * the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS which
 * can actually be applied to blob urls. Note: that both this class and {@link BlobServiceSASQueryParameters} exist because the
 * former is mutable and a logical representation while the latter is immutable and used to generate actual REST
 * requests.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas>here </a> for
 * more details on each value, including which are required.
 *
 * <p>Please see
 * <a href=https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java>here</a>
 * for additional samples.</p>
 */

final class BlobServiceSASSignatureValues {
    private final ClientLogger logger = new ClientLogger(BlobServiceSASSignatureValues.class);

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    private SASProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private IPRange ipRange;

    private String canonicalName;

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
    BlobServiceSASSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime Time the SAS becomes valid
     * @param permissions Permissions granted by the SAS
     */
    BlobServiceSASSignatureValues(OffsetDateTime expiryTime, String permissions) {
        this.expiryTime = expiryTime;
        this.permissions = permissions;
    }

    /**
     * Creates an object with the specified identifier
     *
     * @param identifier Identifier for the SAS
     */
    BlobServiceSASSignatureValues(String identifier) {
        this.identifier = identifier;
    }

    BlobServiceSASSignatureValues(String version, SASProtocol sasProtocol, OffsetDateTime startTime,
                              OffsetDateTime expiryTime, String permission, IPRange ipRange, String identifier, String cacheControl,
                              String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {
        if (version != null) {
            this.version = version;
        }
        this.protocol = sasProtocol;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
        this.permissions = permission;
        this.ipRange = ipRange;
        this.identifier = identifier;
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.contentEncoding = contentEncoding;
        this.contentLanguage = contentLanguage;
        this.contentType = contentType;
    }

    /**
     * @return the version of the service this SAS will target. If not specified, it will default to the version targeted
     * by the library.
     */
    public String version() {
        return version;
    }

    /**
     * Sets the version of the service this SAS will target. If not specified, it will default to the version targeted
     * by the library.
     *
     * @param version Version to target
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues version(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SASProtocol} which determines the protocols allowed by the SAS.
     */
    public SASProtocol protocol() {
        return protocol;
    }

    /**
     * Sets the {@link SASProtocol} which determines the protocols allowed by the SAS.
     *
     * @param protocol Protocol for the SAS
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues protocol(SASProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * @return when the SAS will take effect.
     */
    public OffsetDateTime startTime() {
        return startTime;
    }

    /**
     * Sets when the SAS will take effect.
     *
     * @param startTime When the SAS takes effect
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues startTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * @return the time after which the SAS will no longer work.
     */
    public OffsetDateTime expiryTime() {
        return expiryTime;
    }

    /**
     * Sets the time after which the SAS will no longer work.
     *
     * @param expiryTime When the SAS will no longer work
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues expiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to either {@link ContainerSASPermission} or
     * {@link BlobSASPermission} depending on the resource being accessed for help determining the permissions allowed.
     */
    public String permissions() {
        return permissions;
    }

    /**
     * Sets the permissions string allowed by the SAS. Please refer to either {@link ContainerSASPermission} or
     * {@link BlobSASPermission} depending on the resource being accessed for help constructing the permissions string.
     *
     * @param permissions Permissions string for the SAS
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues permissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * @return the {@link IPRange} which determines the IP ranges that are allowed to use the SAS.
     */
    public IPRange ipRange() {
        return ipRange;
    }

    /**
     * Sets the {@link IPRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @param ipRange Allowed IP range to set
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues ipRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * @return the resource the SAS user may access.
     */
    public String resource() {
        return resource;
    }

    /**
     * Sets the resource the SAS user may access.
     *
     * @param resource Allowed resources string to set
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues resource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * @return the canonical name of the object the SAS user may access.
     */
    public String canonicalName() {
        return canonicalName;
    }

    /**
     * Sets the canonical name of the object the SAS user may access.
     *
     * @param canonicalName Canonical name of the object the SAS grants access
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues canonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
        return this;
    }

    /**
     * Sets the canonical name of the object the SAS user may access. Constructs a canonical name of
     * "/blob/{accountName}{Path of urlString}".
     *
     * @param urlString URL string that contains the path to the object
     * @param accountName Name of the account that contains the object
     * @return the updated BlobServiceSASSignatureValues object
     * @throws RuntimeException If {@code urlString} is a malformed URL.
     */
    public BlobServiceSASSignatureValues canonicalName(String urlString, String accountName) {
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
     * @return the specific snapshot the SAS user may access.
     */
    public String snapshotId() {
        return this.snapshotId;
    }

    /**
     * Sets the specific snapshot the SAS user may access.
     *
     * @param snapshotId Identifier of the snapshot
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues snapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
        return this;
    }

    /**
     * @return the name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String identifier() {
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
    public BlobServiceSASSignatureValues identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @return the cache-control header for the SAS.
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * Sets the cache-control header for the SAS.
     *
     * @param cacheControl Cache-Control header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues cacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * @return the content-disposition header for the SAS.
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * Sets the content-disposition header for the SAS.
     *
     * @param contentDisposition Content-Disposition header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues contentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * @return the content-encoding header for the SAS.
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the content-encoding header for the SAS.
     *
     * @param contentEncoding Content-Encoding header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues contentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * @return the content-language header for the SAS.
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the content-language header for the SAS.
     *
     * @param contentLanguage Content-Language header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues contentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * @return the content-type header for the SAS.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content-type header for the SAS.
     *
     * @param contentType Content-Type header value
     * @return the updated BlobServiceSASSignatureValues object
     */
    public BlobServiceSASSignatureValues contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * @param sharedKeyCredentials A {@link SharedKeyCredential} object used to sign the SAS values.
     * @return {@link BlobServiceSASQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     */
    public BlobServiceSASQueryParameters generateSASQueryParameters(SharedKeyCredential sharedKeyCredentials) {
        Utility.assertNotNull("sharedKeyCredentials", sharedKeyCredentials);
        assertGenerateOK(false);

        // Signature is generated on the un-url-encoded values.
        String signature = sharedKeyCredentials.computeHmac256(stringToSign());

        return new BlobServiceSASQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime, this.ipRange,
            this.identifier, this.resource, this.permissions, signature, this.cacheControl, this.contentDisposition,
            this.contentEncoding, this.contentLanguage, this.contentType, null /* delegate */);
    }

    /**
     * Uses a user delegation key to sign these signature values to produce the proper SAS query parameters.
     *
     * @param delegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @return {@link BlobServiceSASQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     */
    public BlobServiceSASQueryParameters generateSASQueryParameters(UserDelegationKey delegationKey) {
        Utility.assertNotNull("delegationKey", delegationKey);
        assertGenerateOK(true);

        // Signature is generated on the un-url-encoded values.
        String signature = Utility.computeHMac256(delegationKey.value(), stringToSign(delegationKey));

        return new BlobServiceSASQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime, this.ipRange,
            null /* identifier */, this.resource, this.permissions, signature, this.cacheControl, this.contentDisposition,
            this.contentEncoding, this.contentLanguage, this.contentType, delegationKey);
    }

    /**
     * Common assertions for generateSASQueryParameters overloads.
     */
    private void assertGenerateOK(boolean usingUserDelegation) {
        Utility.assertNotNull("version", this.version);
        Utility.assertNotNull("canonicalName", this.canonicalName);
        Utility.assertNotNull("resource", this.resource);

        // If a UserDelegation key or a SignedIdentifier is not being used both expiryDate and permissions must be set.
        if (usingUserDelegation || identifier == null) {
            Utility.assertNotNull("expiryTime", this.expiryTime);
            Utility.assertNotNull("permissions", this.permissions);
        } else if (!usingUserDelegation) {
            // Otherwise a SignedIdentifier must be used.
            Utility.assertNotNull("identifier", this.identifier);
        }

        if (Constants.UrlConstants.SAS_CONTAINER_CONSTANT.equals(this.resource) && this.snapshotId != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Cannot set a snapshotId without resource being a blob."));
        }
    }

    private String stringToSign() {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.canonicalName == null ? "" : this.canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.ipRange == null ? "" : this.ipRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version,
            this.resource == null ? "" : this.resource,
            this.snapshotId == null ? "" : this.snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }

    private String stringToSign(final UserDelegationKey key) {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.canonicalName == null ? "" : this.canonicalName,
            key.signedOid() == null ? "" : key.signedOid(),
            key.signedTid() == null ? "" : key.signedTid(),
            key.signedStart() == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(key.signedStart()),
            key.signedExpiry() == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(key.signedExpiry()),
            key.signedService() == null ? "" : key.signedService(),
            key.signedVersion() == null ? "" : key.signedVersion(),
            this.ipRange == null ? "" : this.ipRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            this.version == null ? "" : this.version,
            this.resource == null ? "" : this.resource,
            this.snapshotId == null ? "" : this.snapshotId,
            this.cacheControl == null ? "" : this.cacheControl,
            this.contentDisposition == null ? "" : this.contentDisposition,
            this.contentEncoding == null ? "" : this.contentEncoding,
            this.contentLanguage == null ? "" : this.contentLanguage,
            this.contentType == null ? "" : this.contentType
        );
    }
}
