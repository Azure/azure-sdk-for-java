// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

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
 * ServiceSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage service. Once all
 * the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS which
 * can actually be applied to blob urls. Note: that both this class and {@link SASQueryParameters} exist because the
 * former is mutable and a logical representation while the latter is immutable and used to generate actual REST
 * requests.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas>here </a> for
 * more details on each value, including which are required.
 *
 * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_sas
 * "Sample code for ServiceSASSignatureValues")] \n For more samples, please see the [Samples
 * file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
 */
final class ServiceSASSignatureValues {

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
    ServiceSASSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime
     * @param permissions
     */
    ServiceSASSignatureValues(OffsetDateTime expiryTime, String permissions) {
        this.expiryTime = expiryTime;
        this.permissions = permissions;
    }

    /**
     * Creates an object with the specified identifier
     *
     * @param identifier
     */
    ServiceSASSignatureValues(String identifier) {
        this.identifier = identifier;
    }

    ServiceSASSignatureValues(String version, SASProtocol sasProtocol, OffsetDateTime startTime,
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
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
     */
    public String version() {
        return version;
    }

    /**
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
     */
    public ServiceSASSignatureValues version(String version) {
        this.version = version;
        return this;
    }

    /**
     * {@link SASProtocol}
     */
    public SASProtocol protocol() {
        return protocol;
    }

    /**
     * {@link SASProtocol}
     */
    public ServiceSASSignatureValues protocol(SASProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * When the SAS will take effect.
     */
    public OffsetDateTime startTime() {
        return startTime;
    }

    /**
     * When the SAS will take effect.
     */
    public ServiceSASSignatureValues startTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * The time after which the SAS will no longer work.
     */
    public OffsetDateTime expiryTime() {
        return expiryTime;
    }

    /**
     * The time after which the SAS will no longer work.
     */
    public ServiceSASSignatureValues expiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * Please refer to either {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource
     * being accessed for help constructing the permissions string.
     */
    public String permissions() {
        return permissions;
    }

    /**
     * Please refer to either {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource
     * being accessed for help constructing the permissions string.
     */
    public ServiceSASSignatureValues permissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * {@link IPRange}
     */
    public IPRange ipRange() {
        return ipRange;
    }

    /**
     * {@link IPRange}
     */
    public ServiceSASSignatureValues ipRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * The resource the SAS user may access.
     */
    public String resource() {
        return resource;
    }

    /**
     * The resource the SAS user may access.
     */
    public ServiceSASSignatureValues resource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * The canonical name of the object the SAS user may access.
     */
    public String canonicalName() {
        return canonicalName;
    }

    /**
     * The canonical name of the object the SAS user may access.
     */
    public ServiceSASSignatureValues canonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
        return this;
    }

    /**
     * The canonical name of the object the SAS user may access.
     *
     * @throws RuntimeException If urlString is a malformed URL.
     */
    public ServiceSASSignatureValues canonicalName(String urlString, String accountName) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        StringBuilder canonicalName = new StringBuilder("/blob");
        canonicalName.append('/').append(accountName).append(url.getPath());
        this.canonicalName = canonicalName.toString();

        return this;
    }

    /**
     * The specific snapshot the SAS user may access.
     */
    public String snapshotId() {
        return this.snapshotId;
    }

    /**
     * The specific snapshot the SAS user may access.
     */
    public ServiceSASSignatureValues snapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
        return this;
    }

    /**
     * The name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String identifier() {
        return identifier;
    }

    /**
     * The name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public ServiceSASSignatureValues identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * The cache-control header for the SAS.
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * The cache-control header for the SAS.
     */
    public ServiceSASSignatureValues cacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * The content-disposition header for the SAS.
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * The content-disposition header for the SAS.
     */
    public ServiceSASSignatureValues contentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * The content-encoding header for the SAS.
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * The content-encoding header for the SAS.
     */
    public ServiceSASSignatureValues contentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * The content-language header for the SAS.
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * The content-language header for the SAS.
     */
    public ServiceSASSignatureValues contentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * The content-type header for the SAS.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * The content-type header for the SAS.
     */
    public ServiceSASSignatureValues contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * @param sharedKeyCredentials A {@link SharedKeyCredential} object used to sign the SAS values.
     * @return {@link SASQueryParameters}
     * @throws RuntimeException If the HMAC-SHA256 algorithm isn't support, if the key isn't a valid Base64 encoded
     * string, or the UTF-8 charset isn't supported.
     */
    public SASQueryParameters generateSASQueryParameters(SharedKeyCredential sharedKeyCredentials) {
        Utility.assertNotNull("sharedKeyCredentials", sharedKeyCredentials);
        assertGenerateOK(false);

        // Signature is generated on the un-url-encoded values.
        String signature = sharedKeyCredentials.computeHmac256(stringToSign());

        return new SASQueryParameters(this.version, null, null,
            this.protocol, this.startTime, this.expiryTime, this.ipRange, this.identifier, resource,
            this.permissions, signature, this.cacheControl, this.contentDisposition, this.contentEncoding,
            this.contentLanguage, this.contentType, null /* delegate */);
    }

    /**
     * Uses a user delegation key to sign these signature values to produce the proper SAS query parameters.
     *
     * @param delegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @return {@link SASQueryParameters}
     * @throws RuntimeException If the HMAC-SHA256 algorithm isn't support, if the key isn't a valid Base64 encoded
     * string, or the UTF-8 charset isn't supported.
     */
    public SASQueryParameters generateSASQueryParameters(UserDelegationKey delegationKey) {
        Utility.assertNotNull("delegationKey", delegationKey);
        assertGenerateOK(true);

        // Signature is generated on the un-url-encoded values.
        String signature = Utility.computeHMac256(delegationKey.value(), stringToSign(delegationKey));

        return new SASQueryParameters(this.version, null, null,
            this.protocol, this.startTime, this.expiryTime, this.ipRange, null /* identifier */, resource,
            this.permissions, signature, this.cacheControl, this.contentDisposition, this.contentEncoding,
            this.contentLanguage, this.contentType, delegationKey);
    }

    /**
     * Common assertions for generateSASQueryParameters overloads.
     */
    private void assertGenerateOK(boolean usingUserDelegation) {
        Utility.assertNotNull("version", this.version);
        Utility.assertNotNull("canonicalName", this.canonicalName);

        // Ensure either (expiryTime and permissions) or (identifier) is set
        if (this.expiryTime == null || this.permissions == null) {
            // Identifier is not required if user delegation is being used
            if (!usingUserDelegation) {
                Utility.assertNotNull("identifier", this.identifier);
            }
        } else {
            Utility.assertNotNull("expiryTime", this.expiryTime);
            Utility.assertNotNull("permissions", this.permissions);
        }

        if (this.resource != null && this.resource.equals(Constants.UrlConstants.SAS_CONTAINER_CONSTANT)) {
            if (this.snapshotId != null) {
                throw new IllegalArgumentException("Cannot set a snapshotId without resource being a blob.");
            }
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
