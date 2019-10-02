// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.credentials.SharedKeyCredential;

import java.time.OffsetDateTime;

/**
 * AccountSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account. Once all
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
public final class AccountSASSignatureValues {

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    private SASProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private IPRange ipRange;

    private String services;

    private String resourceTypes;

    /**
     * Initializes an {@code AccountSASSignatureValues} object with the version number set to the default and all other
     * values empty.
     */
    public AccountSASSignatureValues() {
    }

    /**
     * Shared method between service clients to generate an account SAS.
     *
     * @param sharedKeyCredential The {@code SharedKeyCredential} shared key credential for the account SAS
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @param startTime The {@code OffsetDateTime} start time for the account SAS
     * @param version The {@code String} version for the account SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @return A string that represents the SAS token
     * @throws NullPointerException If any of {@code sharedKeyCredentials}, {@code services}, {@code resourceTypes},
     * {@code expiryTime}, {@code permissions} or {@code versions} is null
     */
    public static String generateAccountSAS(SharedKeyCredential sharedKeyCredential, AccountSASService
        accountSASService, AccountSASResourceType accountSASResourceType, AccountSASPermission accountSASPermission,
        OffsetDateTime expiryTime, OffsetDateTime startTime, String version, IPRange ipRange, SASProtocol sasProtocol) {

        AccountSASSignatureValues values = new AccountSASSignatureValues();

        values.setServices(accountSASService == null ? null : accountSASService.toString());
        values.setResourceTypes(accountSASResourceType == null ? null : accountSASResourceType.toString());
        values.setPermissions(accountSASPermission == null ? null : accountSASPermission.toString());
        values.setExpiryTime(expiryTime);
        values.setStartTime(startTime);

        if (version != null) {
            values.setVersion(version);
        }

        values.setIpRange(ipRange);
        values.setProtocol(sasProtocol);

        AccountSasQueryParameters sasQueryParameters = values.generateSASQueryParameters(sharedKeyCredential);

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
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SASProtocol} which determines the HTTP protocol that will be used.
     */
    public SASProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link SASProtocol} which determines the HTTP protocol that will be used.
     *
     * @param protocol Protocol to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setProtocol(SASProtocol protocol) {
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
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the operations the SAS user may perform. Please refer to {@link AccountSASPermission} to help determine
     * which permissions are allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the operations the SAS user may perform. Please refer to {@link AccountSASPermission} for help constructing
     * the permissions string.
     *
     * @param permissions Permissions string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setPermissions(String permissions) {
        this.permissions = permissions;
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
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setIpRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * @return the services accessible with this SAS. Please refer to {@link AccountSASService} to help determine which
     * services are accessible.
     */
    public String getServices() {
        return services;
    }

    /**
     * Sets the services accessible with this SAS. Please refer to {@link AccountSASService} to construct this value.
     *
     * @param services Allowed services string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setServices(String services) {
        this.services = services;
        return this;
    }

    /**
     * @return the resource types accessible with this SAS. Please refer to {@link AccountSASResourceType} to help
     * determine the resource types that are accessible.
     */
    public String getResourceTypes() {
        return resourceTypes;
    }

    /**
     * Sets the resource types accessible with this SAS. Please refer to {@link AccountSASResourceType} to construct
     * this value.
     *
     * @param resourceTypes Allowed resource types string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues setResourceTypes(String resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

    /**
     * Generates a {@link AccountSasQueryParameters} object which contains all SAS query parameters needed to make an
     * actual REST request.
     *
     * @param sharedKeyCredentials Credentials for the storage account and corresponding primary or secondary key.
     * @return {@link AccountSasQueryParameters}
     * @throws RuntimeException If the HMAC-SHA256 signature for {@code sharedKeyCredentials} fails to generate.
     * @throws NullPointerException If any of {@code sharedKeyCredentials}, {@code services}, {@code resourceTypes},
     * {@code expiryTime}, {@code permissions} or {@code versions} is null
     */
    public AccountSasQueryParameters generateSASQueryParameters(SharedKeyCredential sharedKeyCredentials) {
        Utility.assertNotNull("SharedKeyCredential", sharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);
        Utility.assertNotNull("version", this.version);

        // Signature is generated on the un-url-encoded values.
        String signature = sharedKeyCredentials.computeHmac256(stringToSign(sharedKeyCredentials));

        return new AccountSasQueryParameters(this.version, this.services, resourceTypes,
            this.protocol, this.startTime, this.expiryTime, this.ipRange, this.permissions, signature);
    }

    private String stringToSign(final SharedKeyCredential sharedKeyCredentials) {
        return String.join("\n",
            sharedKeyCredentials.getAccountName(),
            AccountSASPermission.parse(this.permissions).toString(), // guarantees ordering
            this.services,
            resourceTypes,
            this.startTime == null ? Constants.EMPTY_STRING
                : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.ipRange == null ? Constants.EMPTY_STRING : this.ipRange.toString(),
            this.protocol == null ? Constants.EMPTY_STRING : this.protocol.toString(),
            this.version,
            Constants.EMPTY_STRING // Account SAS requires an additional newline character
        );
    }
}
