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

public final class ServiceSASSignatureValues {

    /**
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
     */
    public String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    /**
     * A {@link SASProtocol} value representing the allowed Internet protocols.
     */
    public SASProtocol protocol;

    /**
     * A {@code java.util.Date} specifying when the SAS will take effect.
     */
    public Date startTime;

    /**
     * A {@code java.util.Date} specifying a time after which the SAS will no longer work.
     */
    public Date expiryTime;

    /**
     * A {@code String} specifying which operations the SAS user may perform. Please refer to either
     * {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource being accessed for help
     * constructing the permissions string.
     */
    public String permissions;

    /**
     * An {@link IPRange} object specifying which IP addresses may validly use this SAS.
     */
    public IPRange ipRange;

    /**
     * A {@code String} specifying the name of the container the SAS user may access.
     */
    public String containerName;

    /**
     * A {@code String} specifying the name of the container the SAS user may access.
     */
    public String blobName;

    /**
     * A {@code String} specifying which access policy on the container this SAS references if any.
     */
    public String identifier;

    /**
     * A {@code String} specifying the control header for the SAS.
     */
    public String cacheControl;

    /**
     * A {@code String} specifying the content-disposition header for the SAS.
     */
    public String contentDisposition;

    /**
     * A {@code String} specifying the content-encoding header for the SAS.
     *
     */
    public String contentEncoding;

    /**
     * A {@code String} specifying the content-language header for the SAS.
     */
    public String contentLanguage;

    /**
     * A {@code String} specifying the content-type header for the SAS.
     */
    public String contentType;

    /**
     * Creates an object with empty values for all fields.
     */
    public ServiceSASSignatureValues() { }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * @param sharedKeyCredentials
     *      A {@link SharedKeyCredentials} object used to sign the SAS values.
     * @return
     *      A {@link SASQueryParameters} object containing the signed query parameters.
     */
    public SASQueryParameters GenerateSASQueryParameters(SharedKeyCredentials sharedKeyCredentials) {
        if (sharedKeyCredentials == null) {
            throw new IllegalArgumentException("SharedKeyCredentials cannot be null.");
        }

        String resource = "c";
        String verifiedPermissions;
        // Calling parse and toString guarantees the proper ordering and throws on invalid characters.
        if (Utility.isNullOrEmpty(this.blobName)) {
            verifiedPermissions = ContainerSASPermission.parse(this.permissions).toString();
        }
        else {
            verifiedPermissions = BlobSASPermission.parse(this.permissions).toString();
            resource = "b";
        }

        // Signature is generated on the un-url-encoded values.
         String stringToSign = Utility.join(new String[]{
                 verifiedPermissions,
                 this.startTime == null ? "" : Utility.ISO8601UTCDateFormat.format(this.startTime),
                 this.expiryTime == null ? "" : Utility.ISO8601UTCDateFormat.format(this.expiryTime),
                 getCanonicalName(sharedKeyCredentials.getAccountName()),
                 this.identifier,
                 this.ipRange.toString(),
                 this.protocol.toString(),
                 this.version,
                 this.cacheControl,
                 this.contentDisposition,
                 this.contentEncoding,
                 this.contentLanguage,
                 this.contentType
         }, '\n');

        String signature = null;
        try {
            signature = sharedKeyCredentials.computeHmac256(stringToSign);
        } catch (InvalidKeyException e) {
            throw new Error(e); // The key should have been validated by now. If it is no longer valid here, we fail.
        }

        return new SASQueryParameters(this.version, null, null,
                this.protocol, this.startTime, this.expiryTime, this.ipRange, this.identifier, resource,
                this.permissions, signature);
    }

    private String getCanonicalName(String accountName) {
        // Container: "/blob/account/containername"
        // Blob:      "/blob/account/containername/blobname"
        StringBuilder canonicalName = new StringBuilder("/blob");
        canonicalName.append('/').append(accountName).append('/').append(this.containerName);

        if (!Utility.isNullOrEmpty(this.blobName)) {
            canonicalName.append("/").append(this.blobName);
        }

        return canonicalName.toString();
    }
}
