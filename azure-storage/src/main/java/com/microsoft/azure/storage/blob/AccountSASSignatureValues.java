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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.Date;

/**
 * AccountSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage account.
 */
public final class AccountSASSignatureValues {

    /**
     * If null or empty, this defaults to the service version targeted by this version of the library.
     */
    public String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    /**
     * A {@link SASProtocol} value representing the allowed Internet protocols.
     */
    public SASProtocol protocol;

    /**
     * A {@code java.util.Date} object which contains the shared access signature start time.
     */
    public Date startTime;

    /**
     * A {@code java.util.Date} object which contains the shared access signature expiry time.
     */
    public Date expiryTime;

    /**
     * A {@code String} specifying which operations the SAS user may perform. Please refer to
     * {@link AccountSASPermission} for help constructing the permissions string.
     */
    public String permissions;

    /**
     * An {@link IPRange} representing the IP addresses permitted to use this SAS.
     */
    public IPRange ipRange;

    /**
     * A {@code String} that contains the values that indicate the services accessible with this SAS. Please refer to
     * {@link AccountSASService} to construct this value.
     */
    public String services;

    /**
     * A {@code String} that contains the values that indicate the resource types accessible with this SAS. Please refer
     * to {@link AccountSASResourceType} to construct this value.
     */
    public String resourceTypes;

    /**
     * Initializes an {@code AccountSASSignatureValues} object with the version number set to the default and all
     * other values empty. For more information on how to use this class, please refer to
     * https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
     */
    public AccountSASSignatureValues() { }

    /**
     * Generates {@link SASQueryParameters} object which contains all SAS query parameters.
     *
     * @param sharedKeyCredentials
     *      A {@link SharedKeyCredentials} object for the storage account and corresponding primary or secondary key.
     * @return
     *      A {@link SASQueryParameters} object which contains all SAS query parameters.
     */
    public SASQueryParameters generateSASQueryParameters(SharedKeyCredentials sharedKeyCredentials) {
        Utility.assertNotNull("SharedKeyCredentials", sharedKeyCredentials);
        Utility.assertNotNull("services", this.services);
        Utility.assertNotNull("resourceTypes", this.resourceTypes);
        Utility.assertNotNull("expiryTime", this.expiryTime);
        Utility.assertNotNull("permissions", this.permissions);

        IPRange ipRange;
        if (this.ipRange == null) {
            ipRange = IPRange.DEFAULT;
        }
        else {
            ipRange = this.ipRange;
        }

        // Signature is generated on the un-url-encoded values.
        String stringToSign = Utility.join(new String[]{
                sharedKeyCredentials.getAccountName(),
                AccountSASPermission.parse(this.permissions).toString(), // guarantees ordering
                this.services,
                resourceTypes,
                this.startTime == null ? "" : Utility.ISO8601UTCDateFormat.format(this.startTime),
                this.expiryTime == null ? "" : Utility.ISO8601UTCDateFormat.format(this.expiryTime),
                ipRange.toString(),
                this.protocol.toString(),
                this.version,
                Constants.EMPTY_STRING // Account SAS requires an additional newline character
        }, '\n');

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
