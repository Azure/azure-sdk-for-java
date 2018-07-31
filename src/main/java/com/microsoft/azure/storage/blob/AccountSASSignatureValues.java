/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

/**
 * AccountSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account. Once
 * all the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS
 * which can actually be applied to blob urls. Note: that both this class and {@link SASQueryParameters} exist because
 * the former is mutable and a logical representation while the latter is immutable and used to generate actual REST
 * requests.
 *
 * Please see
 * <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS:
 *
 *
 * Please see
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas>here</a> for further
 * descriptions of the parameters, including which are required:
 *
 * @apiNote
 * ## Sample Code \n
 * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_sas "Sample code for AccountSASSignatureValues")] \n
 * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
 */
public final class AccountSASSignatureValues {

    /**
     * If null or empty, this defaults to the service version targeted by this version of the library.
     */
    public String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    /**
     * {@link SASProtocol}
     */
    public SASProtocol protocol;

    /**
     * When the SAS will take effect.
     */
    public OffsetDateTime startTime;

    /**
     * The time after which the SAS will no longer work.
     */
    public OffsetDateTime expiryTime;

    /**
     * Specifies which operations the SAS user may perform. Please refer to {@link AccountSASPermission} for help
     * constructing the permissions string.
     */
    public String permissions;

    /**
     * {@link IPRange}
     */
    public IPRange ipRange;

    /**
     * The values that indicate the services accessible with this SAS. Please refer to {@link AccountSASService} to
     * construct this value.
     */
    public String services;

    /**
     * The values that indicate the resource types accessible with this SAS. Please refer
     * to {@link AccountSASResourceType} to construct this value.
     */
    public String resourceTypes;

    /**
     * Initializes an {@code AccountSASSignatureValues} object with the version number set to the default and all
     * other values empty.
     */
    public AccountSASSignatureValues() { }

    /**
     * Generates a {@link SASQueryParameters} object which contains all SAS query parameters needed to make an actual
     * REST request.
     *
     * @param sharedKeyCredentials
     *      Credentials for the storage account and corresponding primary or secondary key.
     * @return
     *      {@link SASQueryParameters}
     */
    public SASQueryParameters generateSASQueryParameters(SharedKeyCredentials sharedKeyCredentials) {
        Utility.assertNotNull("SharedKeyCredentials", sharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);

        // Signature is generated on the un-url-encoded values.
        String stringToSign = String.join("\n",
                sharedKeyCredentials.getAccountName(),
                AccountSASPermission.parse(this.permissions).toString(), // guarantees ordering
                this.services,
                resourceTypes,
                this.startTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.startTime),
                this.expiryTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.expiryTime),
                this.ipRange == null ? IPRange.DEFAULT.toString() : this.ipRange.toString(),
                this.protocol == null ? "" : this.protocol.toString(),
                this.version,
                Constants.EMPTY_STRING // Account SAS requires an additional newline character
        );

        String signature;
        try {
            signature = sharedKeyCredentials.computeHmac256(stringToSign);
        } catch (InvalidKeyException e) {
            throw new Error(e); // The key should have been validated by now. If it is no longer valid here, we fail.
        }

        return new SASQueryParameters(this.version, this.services, resourceTypes,
                this.protocol, this.startTime, this.expiryTime, this.ipRange, null,
                null, this.permissions, signature);
    }
}
