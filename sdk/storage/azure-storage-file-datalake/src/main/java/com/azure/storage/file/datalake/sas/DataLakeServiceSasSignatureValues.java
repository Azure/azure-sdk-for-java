// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.sas;

import com.azure.core.util.Configuration;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.datalake.DataLakeServiceVersion;
import com.azure.storage.file.datalake.models.UserDelegationKey;

import java.time.OffsetDateTime;

/**
 * Used to initialize parameters for a Shared Access Signature (SAS) for an Azure Data Lake Storage service. Once all
 * the values here are set, use the appropriate SAS generation method on the desired file system/path client to obtain a
 * representation of the SAS which can then be applied to a new client using the .sasToken(String) method on the
 * desired client builder.
 *
 * @see <a href=https://docs.microsoft.com/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service
 * SAS</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/create-user-delegation-sas>Constructing a
 * User Delegation SAS</a>
 */
public final class DataLakeServiceSasSignatureValues {

    private static final String VERSION = Configuration.getGlobalConfiguration()
        .get(Constants.PROPERTY_AZURE_STORAGE_SAS_SERVICE_VERSION, DataLakeServiceVersion.getLatest().getVersion());

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String identifier;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentType;

    private String preauthorizedAgentObjectId; /* saoid */

    private String agentObjectId; /* suoid */

    private String correlationId;

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link FileSystemSasPermission} allowed by the SAS.
     */
    public DataLakeServiceSasSignatureValues(OffsetDateTime expiryTime, FileSystemSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified expiry time and permissions
     *
     * @param expiryTime When the SAS will no longer work
     * @param permissions {@link PathSasPermission} allowed by the SAS
     */
    public DataLakeServiceSasSignatureValues(OffsetDateTime expiryTime, PathSasPermission permissions) {
        StorageImplUtils.assertNotNull("expiryTime", expiryTime);
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified identifier.
     * NOTE: Identifier can not be used for a {@link UserDelegationKey} SAS.
     *
     * @param identifier Name of the access policy
     */
    public DataLakeServiceSasSignatureValues(String identifier) {
        StorageImplUtils.assertNotNull("identifier", identifier);
        this.identifier = identifier;
    }

    /**
     * @return the version of the service this SAS will target. If not specified, it will default to the version
     * targeted by the library.
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Sets the version of the service this SAS will target. If not specified, it will default to the version targeted
     * by the library.
     *
     * @param version Version to target
     * @return the updated DataLakeServiceSasSignatureValues object
     * @deprecated The version is set to the latest version of sas. Users should stop calling this API as it is now
     * treated as a no-op.
     */
    @Deprecated
    public DataLakeServiceSasSignatureValues setVersion(String version) {
        /* No-op.*/
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setProtocol(SasProtocol protocol) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setStartTime(OffsetDateTime startTime) {
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
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return the permissions string allowed by the SAS. Please refer to either {@link FileSystemSasPermission} or
     * {@link PathSasPermission} depending on the resource being accessed for help determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the Path permissions allowed by the SAS.
     *
     * @param permissions {@link PathSasPermission}
     * @return the updated DataLakeServiceSasSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public DataLakeServiceSasSignatureValues setPermissions(PathSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
        this.permissions = permissions.toString();
        return this;
    }

    /**
     * Sets the File System permissions allowed by the SAS.
     *
     * @param permissions {@link FileSystemSasPermission}
     * @return the updated DataLakeServiceSasSignatureValues object
     * @throws NullPointerException if {@code permissions} is null.
     */
    public DataLakeServiceSasSignatureValues setPermissions(FileSystemSasPermission permissions) {
        StorageImplUtils.assertNotNull("permissions", permissions);
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
     * @see <a href=https://docs.microsoft.com/rest/api/storageservices/create-service-sas#specifying-ip-address-or-ip-range>Specifying IP Address or IP range</a>
     * @param sasIpRange Allowed IP range to set
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;
        return this;
    }

    /**
     * @return the name of the access policy on the file system this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the file system this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     *
     * @param identifier Name of the access policy
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @return the cache-control header for the SAS.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Sets the cache-control header for the SAS.
     *
     * @param cacheControl Cache-Control header value
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * @return the content-disposition header for the SAS.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Sets the content-disposition header for the SAS.
     *
     * @param contentDisposition Content-Disposition header value
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * @return the content-encoding header for the SAS.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the content-encoding header for the SAS.
     *
     * @param contentEncoding Content-Encoding header value
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * @return the content-language header for the SAS.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the content-language header for the SAS.
     *
     * @param contentLanguage Content-Language header value
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * @return the content-type header for the SAS.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content-type header for the SAS.
     *
     * @param contentType Content-Type header value
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @return The AAD object ID of a user assumed to be authorized by the owner of the user delegation key to perform
     * the action granted by the SAS token. The service will validate the SAS token and ensure that the owner of the
     * user delegation key has the required permissions before granting access but no additional permission check for
     * the agent object id will be performed.
     */
    public String getPreauthorizedAgentObjectId() {
        return preauthorizedAgentObjectId;
    }

    /**
     * Sets the AAD object ID of a user assumed to be authorized by the owner of the user delegation key to perform the
     * action granted by the SAS token.
     *
     * @param preauthorizedAgentObjectId The AAD object ID of a user assumed to be authorized by the owner of the user
     * delegation key to perform the action granted by the SAS token. The service will validate the SAS token and
     * ensure that the owner of the user delegation key has the required permissions before granting access but no
     * additional permission check for the agent object id will be performed.
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setPreauthorizedAgentObjectId(String preauthorizedAgentObjectId) {
        this.preauthorizedAgentObjectId = preauthorizedAgentObjectId;
        return this;
    }

    /**
     * @return The AAD object ID of a user assumed to be unauthorized by the owner of the user delegation key to
     * perform the action granted by the SAS token. The service will validate the SAS token and ensure that the owner
     * of the user delegation key has the required permissions before granting access and the service will perform an
     * additional POSIX ACL check to determine if this user is authorized to perform the requested operation.
     */
    public String getAgentObjectId() {
        return agentObjectId;
    }

    /**
     * Sets the AAD object ID of a user assumed to be unauthorized by the owner of the user delegation key to perform
     * the action granted by the SAS token.
     *
     * @param agentObjectId The AAD object ID of a user assumed to be unauthorized by the owner of the user delegation
     * key to perform the action granted by the SAS token. The service will validate the SAS token and ensure that the
     * owner of the user delegation key has the required permissions before granting access and the service will
     * perform an additional POSIX ACL check to determine if this user is authorized to perform the requested operation.
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setAgentObjectId(String agentObjectId) {
        this.agentObjectId = agentObjectId;
        return this;
    }

    /**
     * @return the correlation id value for the SAS.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlation id value for the SAS.
     *
     * <p>Note: This parameter is only valid for user delegation SAS. </p>
     *
     * @param correlationId A correlation ID used to correlate the storage audit logs with the audit logs used by the
     * principal generating and distributing SAS.
     * @return the updated DataLakeServiceSasSignatureValues object
     */
    public DataLakeServiceSasSignatureValues setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }
}
