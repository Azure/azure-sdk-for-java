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
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1>here</a>
 * for more conceptual information on SAS.
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas>here </a> for
 * more details on each value, including which are required.
 *
 * @apiNote ## Sample Code \n
 * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_sas "Sample code for ServiceSASSignatureValues")] \n
 * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
 */
public final class ServiceSASSignatureValues {

    private String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

    private SASProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private IPRange ipRange;

    private String containerName;

    private String blobName;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    /**
     * Creates an object with empty values for all fields.
     */
    public ServiceSASSignatureValues() {
    }

    /**
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
     */
    public String version() {
        return version;
    }

    /**
     * The version of the service this SAS will target. If not specified, it will default to the version targeted by the
     * library.
     */
    public ServiceSASSignatureValues withVersion(String version) {
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
    public ServiceSASSignatureValues withProtocol(SASProtocol protocol) {
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
    public ServiceSASSignatureValues withStartTime(OffsetDateTime startTime) {
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
    public ServiceSASSignatureValues withExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * Please refer to either {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource
     * being accessed for help constructing the permissions string.
     */
    public String permissions() {
        return permissions;
    }

    /**
     * Please refer to either {@link ContainerSASPermission} or {@link BlobSASPermission} depending on the resource
     * being accessed for help constructing the permissions string.
     */
    public ServiceSASSignatureValues withPermissions(String permissions) {
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
    public ServiceSASSignatureValues withIpRange(IPRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }

    /**
     * The name of the container the SAS user may access.
     */
    public String containerName() {
        return containerName;
    }

    /**
     * The name of the container the SAS user may access.
     */
    public ServiceSASSignatureValues withContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * The name of the container the SAS user may access.
     */
    public String blobName() {
        return blobName;
    }

    /**
     * The name of the container the SAS user may access.
     */
    public ServiceSASSignatureValues withBlobName(String blobName) {
        this.blobName = blobName;
        return this;
    }

    /**
     * The name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String identifier() {
        return identifier;
    }

    /**
     * The name of the access policy on the container this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public ServiceSASSignatureValues withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * The cache-control header for the SAS.
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * The cache-control header for the SAS.
     */
    public ServiceSASSignatureValues withCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * The content-disposition header for the SAS.
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * The content-disposition header for the SAS.
     */
    public ServiceSASSignatureValues withContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * The content-encoding header for the SAS.
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * The content-encoding header for the SAS.
     */
    public ServiceSASSignatureValues withContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * The content-language header for the SAS.
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * The content-language header for the SAS.
     */
    public ServiceSASSignatureValues withContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * The content-type header for the SAS.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * The content-type header for the SAS.
     */
    public ServiceSASSignatureValues withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Uses an account's shared key credential to sign these signature values to produce the proper SAS query
     * parameters.
     *
     * @param sharedKeyCredentials
     *         A {@link SharedKeyCredentials} object used to sign the SAS values.
     *
     * @return {@link SASQueryParameters}
     */
    public SASQueryParameters generateSASQueryParameters(SharedKeyCredentials sharedKeyCredentials) {
        Utility.assertNotNull("sharedKeyCredentials", sharedKeyCredentials);
        Utility.assertNotNull("version", this.version);
        Utility.assertNotNull("containerName", this.containerName);

        String resource = "c";
        String verifiedPermissions = null;
        // Calling parse and toString guarantees the proper ordering and throws on invalid characters.
        if (Utility.isNullOrEmpty(this.blobName)) {
            if (this.permissions != null) {
                verifiedPermissions = ContainerSASPermission.parse(this.permissions).toString();
            }
        } else {
            if (this.permissions != null) {
                verifiedPermissions = BlobSASPermission.parse(this.permissions).toString();
            }
            resource = "b";
        }

        // Signature is generated on the un-url-encoded values.
        final String stringToSign = stringToSign(verifiedPermissions, sharedKeyCredentials);

        String signature = null;
        try {
            signature = sharedKeyCredentials.computeHmac256(stringToSign);
        } catch (InvalidKeyException e) {
            throw new Error(e); // The key should have been validated by now. If it is no longer valid here, we fail.
        }

        return new SASQueryParameters(this.version, null, null,
                this.protocol, this.startTime, this.expiryTime, this.ipRange, this.identifier, resource,
                this.permissions, signature, this.cacheControl, this.contentDisposition, this.contentEncoding,
                this.contentLanguage, this.contentType);
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

    private String stringToSign(final String verifiedPermissions,
            final SharedKeyCredentials sharedKeyCredentials) {
        return String.join("\n",
                verifiedPermissions == null ? "" : verifiedPermissions,
                this.startTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.startTime),
                this.expiryTime == null ? "" : Utility.ISO8601UTCDateFormatter.format(this.expiryTime),
                getCanonicalName(sharedKeyCredentials.getAccountName()),
                this.identifier == null ? "" : this.identifier,
                this.ipRange == null ? IPRange.DEFAULT.toString() : this.ipRange.toString(),
                this.protocol == null ? "" : protocol.toString(),
                this.version,
                this.cacheControl == null ? "" : this.cacheControl,
                this.contentDisposition == null ? "" : this.contentDisposition,
                this.contentEncoding == null ? "" : this.contentEncoding,
                this.contentLanguage == null ? "" : this.contentLanguage,
                this.contentType == null ? "" : this.contentType
        );
    }
}
