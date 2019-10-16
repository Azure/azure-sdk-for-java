// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.Constants;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.SasProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;

import java.time.OffsetDateTime;

/**
 * QueueServiceSasSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage service. Once
 * all the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS
 * which can actually be applied to queue urls. Note: that both this class and {@link QueueServiceSasQueryParameters}
 * exist because the former is mutable and a logical representation while the latter is immutable and used to generate
 * actual REST requests.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas>here </a> for
 * more details on each value, including which are required.
 *
 * <p>Please see
 * <a href=https://github.com/Azure/azure-storage-java/queue/master/src/test/java/com/microsoft/azure/storage/Samples.java>here</a>
 * for additional samples.</p>
 */
public final class QueueServiceSasSignatureValues {

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String canonicalName;

    private String identifier;

    /**
     * Creates an object with empty values for all fields.
     */
    QueueServiceSasSignatureValues() {
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime Time the SAS becomes valid
     * @param permissions Permissions granted by the SAS
     */
    QueueServiceSasSignatureValues(OffsetDateTime expiryTime, String permissions) {
        this.expiryTime = expiryTime;
        this.permissions = permissions;
    }

    /**
     * Creates an object with the specified identifier
     *
     * @param identifier Identifier for the SAS
     */
    QueueServiceSasSignatureValues(String identifier) {
        this.identifier = identifier;
    }

    QueueServiceSasSignatureValues(String version, SasProtocol sasProtocol, OffsetDateTime startTime,
        OffsetDateTime expiryTime, String permission, SasIpRange sasIpRange, String identifier) {
        if (version != null) {
            this.version = version;
        }
        this.protocol = sasProtocol;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
        this.permissions = permission;
        this.sasIpRange = sasIpRange;
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setVersion(String version) {
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setProtocol(SasProtocol protocol) {
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to {@link QueueSasPermission} for help
     * determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions string allowed by the SAS. Please refer to {@link QueueSasPermission} for help constructing
     * the permissions string.
     *
     * @param permissions Permissions string for the SAS
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setPermissions(String permissions) {
        this.permissions = permissions;
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
        return this;
    }

    /**
     * Sets the canonical name of the object the SAS user may access. Constructs a canonical name of
     * "/queue/{accountName}{queueName}".
     *
     * @param queueName Name of the queue object
     * @param accountName Name of the account that contains the object
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setCanonicalName(String queueName, String accountName) {
        this.canonicalName = String.format("/queue/%s/%s", accountName, queueName);
        return this;
    }

    /**
     * @return the name of the access policy on the queue this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String getId() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the queue this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     *
     * @param identifier Name of the access policy
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * @param sharedKeyCredentials A {@link SharedKeyCredential} object used to sign the SAS values.
     * @return {@link QueueServiceSasQueryParameters}
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws NullPointerException If {@code sharedKeyCredential} is null. Or if any of {@code version} or
     * {@code canonicalName} is null. Or if {@code identifier} is null and any or {@code expiryTime} or
     * {@code permissions} is null. Or if {@code expiryTime} and {@code permissions} and {@code identifier} is null
     */
    public QueueServiceSasQueryParameters generateSASQueryParameters(SharedKeyCredential sharedKeyCredentials) {
        Utility.assertNotNull("sharedKeyCredentials", sharedKeyCredentials);
        assertGenerateOK();

        // Signature is generated on the un-url-encoded values.
        String stringToSign = stringToSign();
        String signature = sharedKeyCredentials.computeHmac256(stringToSign);

        return new QueueServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, this.identifier, this.permissions, signature);
    }

    /**
     * Common assertions for generateSASQueryParameters overloads.
     */
    private void assertGenerateOK() {
        Utility.assertNotNull("version", this.version);
        Utility.assertNotNull("canonicalName", this.canonicalName);

        // If a SignedIdentifier is not being used both expiryDate and permissions must be set.
        if (identifier == null) {
            Utility.assertNotNull("expiryTime", this.expiryTime);
            Utility.assertNotNull("permissions", this.permissions);
        }
        // Still need to check identifier if expiry time and permissions are not both set
        if (expiryTime == null || permissions == null) {
            Utility.assertNotNull("identifier", identifier);
        }
    }

    private String stringToSign() {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.canonicalName == null ? "" : this.canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version
        );
    }
}
