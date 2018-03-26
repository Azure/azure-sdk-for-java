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
 * ServiceSASSignatureValues is used to generate a Shared Access Signature (SAS) for an Azure Storage service. Once
 * all the values here are set appropriately, call generateSASQueryParameters to obtain a representation of the SAS
 * which can actually be applied to blob urls. Note: that both this class and {@link SASQueryParameters} exist because
 * the former is mutable and a logical representation while the latter is immutable and used to generate actual REST
 * requests.
 *
 * Please refer to the following for more conceptual information on SAS:
 * https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1
 * https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas
 */
public final class ServiceSASSignatureValues {

    /**
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
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
     * Please refer to either {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource
     * being accessed for help constructing the permissions string.
     */
    public String permissions;

    /**
     * {@link IPRange}
     */
    public IPRange ipRange;

    /**
     * The name of the container the SAS user may access.
     */
    public String containerName;

    /**
     * The name of the container the SAS user may access.
     */
    public String blobName;

    /**
     * The name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String identifier;

    /**
     * The cache-control header for the SAS.
     */
    public String cacheControl;

    /**
     * The content-disposition header for the SAS.
     */
    public String contentDisposition;

    /**
     * The content-encoding header for the SAS.
     *
     */
    public String contentEncoding;

    /**
     * The content-language header for the SAS.
     */
    public String contentLanguage;

    /**
     * The content-type header for the SAS.
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
     *      {@link SASQueryParameters}
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
                 this.startTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.startTime),
                 this.expiryTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.expiryTime),
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
