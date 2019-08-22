// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.credentials.SharedKeyCredential;

import java.time.OffsetDateTime;

/**
 * AccountSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account. Once
 * all the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS
 * which can actually be applied to blob urls. Note: that both this class and {@link AccountSASQueryParameters} exist because
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
     * Initializes an {@code AccountSASSignatureValues} object with the version number set to the default and all
     * other values empty.
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
     */
    public String generateAccountSAS(SharedKeyCredential sharedKeyCredential, AccountSASService accountSASService,
        AccountSASResourceType accountSASResourceType, AccountSASPermission accountSASPermission,
        OffsetDateTime expiryTime, OffsetDateTime startTime, String version, IPRange ipRange, SASProtocol sasProtocol) {

        this.services(accountSASService == null ? null : accountSASService.toString());
        this.resourceTypes(accountSASResourceType == null ? null : accountSASResourceType.toString());
        this.permissions(accountSASPermission == null ? null : accountSASPermission.toString());
        this.expiryTime(expiryTime);
        this.startTime(startTime);

        if (version != null) {
            this.version(version);
        }

        this.ipRange(ipRange);
        this.protocol(sasProtocol);

        AccountSASQueryParameters sasQueryParameters = this.generateSASQueryParameters(sharedKeyCredential);

        return sasQueryParameters.encode();
    }

    /**
     * @return the service version that is targeted, if {@code null} or empty the service version targeted by the
     * library will be used.
     */
    public String version() {
        return version;
    }

    /**
     * Sets the service version that is targeted. Leave this {@code null} or empty to target the version used by the
     * library.
     *
     * @param version Target version to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues version(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the {@link SASProtocol} which determines the HTTP protocol that will be used.
     */
    public SASProtocol protocol() {
        return protocol;
    }

    /**
     * Sets the {@link SASProtocol} which determines the HTTP protocol that will be used.
     *
     * @param protocol Protocol to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues protocol(SASProtocol protocol) {
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
     * @param startTime Start time to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues startTime(OffsetDateTime startTime) {
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
     * @param expiryTime Expiry time to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues expiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the operations the SAS user may perform. Please refer to {@link AccountSASPermission} to help determine
     * which permissions are allowed.
     */
    public String permissions() {
        return permissions;
    }

    /**
     * Sets the operations the SAS user may perform. Please refer to {@link AccountSASPermission} for help constructing
     * the permissions string.
     *
     * @param permissions Permissions string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues permissions(String permissions) {
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
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues ipRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * @return the services accessible with this SAS. Please refer to {@link AccountSASService} to help determine which
     * services are accessible.
     */
    public String services() {
        return services;
    }

    /**
     * Sets the services accessible with this SAS. Please refer to {@link AccountSASService} to construct this value.
     *
     * @param services Allowed services string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues services(String services) {
        this.services = services;
        return this;
    }

    /**
     * @return the resource types accessible with this SAS. Please refer to {@link AccountSASResourceType} to help
     * determine the resource types that are accessible.
     */
    public String resourceTypes() {
        return resourceTypes;
    }

    /**
     * Sets the resource types accessible with this SAS. Please refer to {@link AccountSASResourceType} to construct
     * this value.
     *
     * @param resourceTypes Allowed resource types string to set
     * @return the updated AccountSASSignatureValues object.
     */
    public AccountSASSignatureValues resourceTypes(String resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

    /**
     * Generates a {@link AccountSASQueryParameters} object which contains all SAS query parameters needed to make an actual
     * REST request.
     *
     * @param sharedKeyCredentials Credentials for the storage account and corresponding primary or secondary key.
     *
     * @return {@link AccountSASQueryParameters}
     * @throws RuntimeException If the HMAC-SHA256 signature for {@code sharedKeyCredentials} fails to generate.
     */
    public AccountSASQueryParameters generateSASQueryParameters(SharedKeyCredential sharedKeyCredentials) {
        Utility.assertNotNull("SharedKeyCredential", sharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);
        Utility.assertNotNull("version", this.version);

        // Signature is generated on the un-url-encoded values.
        String signature = sharedKeyCredentials.computeHmac256(stringToSign(sharedKeyCredentials));

        return new AccountSASQueryParameters(this.version, this.services, resourceTypes,
                this.protocol, this.startTime, this.expiryTime, this.ipRange, this.permissions, signature);
    }

    private String stringToSign(final SharedKeyCredential sharedKeyCredentials) {
        return String.join("\n",
                sharedKeyCredentials.accountName(),
                AccountSASPermission.parse(this.permissions).toString(), // guarantees ordering
                this.services,
                resourceTypes,
                this.startTime == null ? Constants.EMPTY_STRING : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
                this.ipRange == null ? Constants.EMPTY_STRING : this.ipRange.toString(),
                this.protocol == null ? Constants.EMPTY_STRING : this.protocol.toString(),
                this.version,
                Constants.EMPTY_STRING // Account SAS requires an additional newline character
        );
    }
}
