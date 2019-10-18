// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;

import java.time.OffsetDateTime;

/**
 * QueueServiceSasSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage service. Once
 * all the values here are set appropriately, call {@link #generateSasQueryParameters(StorageSharedKeyCredential)}
 * to obtain a representation of the SAS which can be applied to queue urls.
 *
 * <p><strong>Generating a queue SAS with expiryTime and permissions</strong></p>
 * <p>The snippet below generates a queue SAS that lasts for two days and gives the user read, add, and update
 * permissions to the queue.
 *
 * {@codesnippet com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential}
 *
 * <p><strong>Generating a queue SAS with stored access policy identifier</strong></p>
 * <p>The snippet below generates a queue SAS that has the same duration and permissions specified by the
 * {@link #setIdentifier(String) stored access policy}.
 *
 * {@codesnippet com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters.identifier#StorageSharedKeyCredential}
 *
 * @see QueueServiceSasQueryParameters
 * @see <a href=https://docs.microsoft.com/en-ca/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service
 * SAS</a>
 */
public final class QueueServiceSasSignatureValues {
    private String version;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String queueName;

    private String identifier;

    /**
     * Creates an object with empty values for all fields.
     */
    public QueueServiceSasSignatureValues() {
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
     * @param permissions Permissions for the SAS
     * @return the updated QueueServiceSasSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public QueueServiceSasSignatureValues setPermissions(QueueSasPermission permissions) {
        Utility.assertNotNull("permissions", permissions);
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
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * Gets the name of the queue this SAS may access.
     *
     * @return The name of the queue the SAS user may access.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the name of the queue this SAS may access.
     *
     * @param queueName Canonical name of the object the SAS grants access
     * @return the updated QueueServiceSasSignatureValues object
     */
    public QueueServiceSasSignatureValues setQueueName(String queueName) {
        this.queueName = queueName;
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
     * <p><strong>Notes on SAS generation</strong></p>
     * <p>
     * <ul>
     * <li>If {@link #setVersion(String) version} is not set, the {@link QueueServiceVersion#getLatest() latest service
     * version} is used.</li>
     * <li>If {@link #setIdentifier(String) identifier} is set, {@link #setExpiryTime(OffsetDateTime) expiryTime} and
     * permissions should not be set. These values are inherited from the stored access policy.</li>
     * <li>Otherwise, {@link #setExpiryTime(OffsetDateTime) expiryTime} and {@link #getPermissions() permissions} must
     * be set.</li>
     * </ul>
     *
     * <p>For samples, see class level JavaDocs.</p>
     *
     * @param storageSharedKeyCredentials A {@link StorageSharedKeyCredential} object used to sign the SAS values.
     * @return A new {@link QueueServiceSasQueryParameters} represented by the current builder.
     * @throws IllegalStateException If the HMAC-SHA256 algorithm isn't supported, if the key isn't a valid Base64
     * encoded string, or the UTF-8 charset isn't supported.
     * @throws NullPointerException If {@code storageSharedKeyCredentials} is null.
     */
    public QueueServiceSasQueryParameters generateSasQueryParameters(
        StorageSharedKeyCredential storageSharedKeyCredentials) {
        Utility.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);

        if (ImplUtils.isNullOrEmpty(version)) {
            version = QueueServiceVersion.getLatest().getVersion();
        }

        // Signature is generated on the un-url-encoded values.
        String canonicalName = getCanonicalName(storageSharedKeyCredentials.getAccountName(), queueName);
        String stringToSign = stringToSign(canonicalName);
        String signature = storageSharedKeyCredentials.computeHmac256(stringToSign);

        return new QueueServiceSasQueryParameters(this.version, this.protocol, this.startTime, this.expiryTime,
            this.sasIpRange, this.identifier, this.permissions, signature);
    }

    /**
     * Computes the canonical name for a queue resource for SAS signing.
     * @param account Account of the storage account.
     * @param queueName Name of the queue.
     * @return Canonical name as a string.
     */
    private static String getCanonicalName(String account, String queueName) {
        // Queue: "/queue/account/queuename"
        return String.join("", new String[] { "/queue/", account, "/", queueName });
    }

    private String stringToSign(String canonicalName) {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            canonicalName,
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version
        );
    }
}
