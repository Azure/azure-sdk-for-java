// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;

import com.azure.storage.common.implementation.Constants;
import java.time.OffsetDateTime;

/**
 * AccountSasSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account. Once all
 * the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS which
 * can actually be applied to blob urls. Note: that both this class and {@link AccountSasQueryParameters} exist because
 * the former is mutable and a logical representation while the latter is immutable and used to generate actual REST
 * requests.
 * <p>
 * Please see
 * <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS:
 * <p>
 * <p>
 * Please see
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas>here</a> for further
 * descriptions of the parameters, including which are required:
 *
 * <p>Please see
 * <a href=https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java>here</a>
 * for additional samples.</p>
 */
public final class AccountSasSignatureValues {

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String services;

    private String resourceTypes;

    /**
     * Initializes an {@code AccountSasSignatureValues} object with the version number set to the default and all other
     * values empty.
     */
    public AccountSasSignatureValues() {
    }

    /**
     * Shared method between service clients to generate an account SAS.
     *
     * @param storageSharedKeyCredential The {@code SharedKeyCredential} shared key credential for the account SAS
     * @param accountSasService The {@code AccountSasService} services for the account SAS
     * @param accountSasResourceType An optional {@code AccountSasResourceType} resources for the account SAS
     * @param accountSasPermission The {@code AccountSasPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @param startTime The {@code OffsetDateTime} start time for the account SAS
     * @param version The {@code String} version for the account SAS
     * @param sasIpRange An optional {@code SasIpRange} ip address range for the SAS
     * @param sasProtocol An optional {@code SasProtocol} protocol for the SAS
     * @return A string that represents the SAS token
     * @throws NullPointerException If any of {@code sharedKeyCredentials}, {@code services}, {@code resourceTypes},
     * {@code expiryTime}, {@code permissions} or {@code versions} is null
     */
    public static String generateAccountSas(StorageSharedKeyCredential storageSharedKeyCredential,
            AccountSasService accountSasService, AccountSasResourceType accountSasResourceType,
            AccountSasPermission accountSasPermission, OffsetDateTime expiryTime, OffsetDateTime startTime,
            String version, SasIpRange sasIpRange, SasProtocol sasProtocol) {

        AccountSasSignatureValues values = new AccountSasSignatureValues();

        values.setServices(accountSasService == null ? null : accountSasService.toString());
        values.setResourceTypes(accountSasResourceType == null ? null : accountSasResourceType.toString());
        values.setPermissions(accountSasPermission == null ? null : accountSasPermission.toString());
        values.setExpiryTime(expiryTime);
        values.setStartTime(startTime);

        if (version != null) {
            values.setVersion(version);
        }

        values.setSasIpRange(sasIpRange);
        values.setProtocol(sasProtocol);

        AccountSasQueryParameters sasQueryParameters = values.generateSasQueryParameters(storageSharedKeyCredential);

        return sasQueryParameters.encode();
    }

    /**
     * @return the service version that is targeted, if {@code null} or empty the service version targeted by the
     * library will be used.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the service version that is targeted. Leave this {@code null} or empty to target the version used by the
     * library.
     *
     * @param version Target version to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SasProtocol} which determines the HTTP protocol that will be used.
     */
    public SasProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link SasProtocol} which determines the HTTP protocol that will be used.
     *
     * @param protocol Protocol to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setProtocol(SasProtocol protocol) {
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
     * @param startTime Start time to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @param expiryTime Expiry time to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the operations the SAS user may perform. Please refer to {@link AccountSasPermission} to help determine
     * which permissions are allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the operations the SAS user may perform. Please refer to {@link AccountSasPermission} for help constructing
     * the permissions string.
     *
     * @param permissions Permissions string to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setPermissions(String permissions) {
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
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * @return the services accessible with this SAS. Please refer to {@link AccountSasService} to help determine which
     * services are accessible.
     */
    public String getServices() {
        return services;
    }

    /**
     * Sets the services accessible with this SAS. Please refer to {@link AccountSasService} to construct this value.
     *
     * @param services Allowed services string to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setServices(String services) {
        this.services = services;
        return this;
    }

    /**
     * @return the resource types accessible with this SAS. Please refer to {@link AccountSasResourceType} to help
     * determine the resource types that are accessible.
     */
    public String getResourceTypes() {
        return resourceTypes;
    }

    /**
     * Sets the resource types accessible with this SAS. Please refer to {@link AccountSasResourceType} to construct
     * this value.
     *
     * @param resourceTypes Allowed resource types string to set
     * @return the updated AccountSasSignatureValues object.
     */
    public AccountSasSignatureValues setResourceTypes(String resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

    /**
     * Generates a {@link AccountSasQueryParameters} object which contains all SAS query parameters needed to make an
     * actual REST request.
     *
     * @param storageSharedKeyCredentials Credentials for the storage account and corresponding
     * primary or secondary key.
     * @return {@link AccountSasQueryParameters}
     * @throws RuntimeException If the HMAC-SHA256 signature for {@code storageSharedKeyCredentials} fails to generate.
     * @throws NullPointerException If any of {@code storageSharedKeyCredentials}, {@code services},
     * {@code resourceTypes},
     * {@code expiryTime}, {@code permissions} or {@code versions} is null
     */
    public AccountSasQueryParameters generateSasQueryParameters(
        StorageSharedKeyCredential storageSharedKeyCredentials) {
        Utility.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);
        Utility.assertNotNull("version", this.version);

        // Signature is generated on the un-url-encoded values.
        String signature = storageSharedKeyCredentials.computeHmac256(stringToSign(storageSharedKeyCredentials));

        return new AccountSasQueryParameters(this.version, this.services, resourceTypes,
            this.protocol, this.startTime, this.expiryTime, this.sasIpRange, this.permissions, signature);
    }

    private String stringToSign(final StorageSharedKeyCredential storageSharedKeyCredentials) {
        return String.join("\n",
            storageSharedKeyCredentials.getAccountName(),
            AccountSasPermission.parse(this.permissions).toString(), // guarantees ordering
            this.services,
            resourceTypes,
            this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            this.version,
            "" // Account SAS requires an additional newline character
        );
    }
}
