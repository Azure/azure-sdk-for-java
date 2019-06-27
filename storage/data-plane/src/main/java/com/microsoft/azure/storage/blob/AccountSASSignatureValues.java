// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

/**
 * AccountSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account. Once
 * all the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS
 * which can actually be applied to blob urls. Note: that both this class and {@link SASQueryParameters} exist because
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
 * @apiNote ## Sample Code \n
 * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_sas "Sample code for AccountSASSignatureValues")] \n
 * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
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
     * If null or empty, this defaults to the service version targeted by this version of the library.
     */
    public String version() {
        return version;
    }

    /**
     * If null or empty, this defaults to the service version targeted by this version of the library.
     */
    public AccountSASSignatureValues withVersion(String version) {
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
    public AccountSASSignatureValues withProtocol(SASProtocol protocol) {
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
    public AccountSASSignatureValues withStartTime(OffsetDateTime startTime) {
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
    public AccountSASSignatureValues withExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * Specifies which operations the SAS user may perform. Please refer to {@link AccountSASPermission} for help
     * constructing the permissions string.
     */
    public String permissions() {
        return permissions;
    }

    /**
     * Specifies which operations the SAS user may perform. Please refer to {@link AccountSASPermission} for help
     * constructing the permissions string.
     */
    public AccountSASSignatureValues withPermissions(String permissions) {
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
    public AccountSASSignatureValues withIpRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * The values that indicate the services accessible with this SAS. Please refer to {@link AccountSASService} to
     * construct this value.
     */
    public String services() {
        return services;
    }

    /**
     * The values that indicate the services accessible with this SAS. Please refer to {@link AccountSASService} to
     * construct this value.
     */
    public AccountSASSignatureValues withServices(String services) {
        this.services = services;
        return this;
    }

    /**
     * The values that indicate the resource types accessible with this SAS. Please refer
     * to {@link AccountSASResourceType} to construct this value.
     */
    public String resourceTypes() {
        return resourceTypes;
    }

    /**
     * The values that indicate the resource types accessible with this SAS. Please refer
     * to {@link AccountSASResourceType} to construct this value.
     */
    public AccountSASSignatureValues withResourceTypes(String resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

    /**
     * Generates a {@link SASQueryParameters} object which contains all SAS query parameters needed to make an actual
     * REST request.
     *
     * @param sharedKeyCredentials
     *         Credentials for the storage account and corresponding primary or secondary key.
     *
     * @return {@link SASQueryParameters}
     */
    public SASQueryParameters generateSASQueryParameters(SharedKeyCredentials sharedKeyCredentials) {
        Utility.assertNotNull("SharedKeyCredentials", sharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);
        Utility.assertNotNull("version", this.version);

        // Signature is generated on the un-url-encoded values.
        final String stringToSign = stringToSign(sharedKeyCredentials);

        String signature;
        try {
            signature = sharedKeyCredentials.computeHmac256(stringToSign);
        } catch (InvalidKeyException e) {
            throw new Error(e); // The key should have been validated by now. If it is no longer valid here, we fail.
        }

        return new SASQueryParameters(this.version, this.services, resourceTypes,
                this.protocol, this.startTime, this.expiryTime, this.ipRange, null,
                null, this.permissions, signature, null, null, null, null, null, null);
    }

    private String stringToSign(final SharedKeyCredentials sharedKeyCredentials) {
        return String.join("\n",
                sharedKeyCredentials.getAccountName(),
                AccountSASPermission.parse(this.permissions).toString(), // guarantees ordering
                this.services,
                resourceTypes,
                this.startTime == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
                this.ipRange == null ? (new IPRange()).toString() : this.ipRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                this.version,
                Constants.EMPTY_STRING // Account SAS requires an additional newline character
        );
    }
}
