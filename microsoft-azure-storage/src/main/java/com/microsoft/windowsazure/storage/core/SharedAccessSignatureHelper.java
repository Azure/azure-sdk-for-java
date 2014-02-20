/**
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
package com.microsoft.windowsazure.storage.core;

import java.security.InvalidKeyException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ServiceClient;
import com.microsoft.windowsazure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.blob.SharedAccessBlobHeaders;
import com.microsoft.windowsazure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.windowsazure.storage.queue.SharedAccessQueuePolicy;
import com.microsoft.windowsazure.storage.table.SharedAccessTablePolicy;

/**
 * RESERVED FOR INTERNAL USE. Contains helper methods for implementing shared access signatures.
 */
public class SharedAccessSignatureHelper {
    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceType
     *            Either "b" for blobs or "c" for containers.
     * @param signature
     *            The signature to use.
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForBlob(final SharedAccessBlobPolicy policy,
            final SharedAccessBlobHeaders headers, final String groupPolicyIdentifier, final String resourceType,
            final String signature) throws StorageException {
        Utility.assertNotNullOrEmpty("resourceType", resourceType);

        return generateSharedAccessSignatureHelper(policy, null /* startPartitionKey */, null /* startRowKey */,
                null /* endPartitionKey */, null /* endRowKey */, groupPolicyIdentifier, resourceType,
                null /* tableName */, signature, null /* accoutKetName */, headers);
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param signature
     *            The signature to use.
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForQueue(final SharedAccessQueuePolicy policy,
            final String groupPolicyIdentifier, final String signature) throws StorageException {
        return generateSharedAccessSignatureHelper(policy, null /* startPartitionKey */, null /* startRowKey */,
                null /* endPartitionKey */, null /* endRowKey */, groupPolicyIdentifier, null /* resourceType */,
                null /* tableName */, signature, null /* accountKeyName */, null /* headers */);
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     */
    public static UriQueryBuilder generateSharedAccessSignatureForTable(final SharedAccessTablePolicy policy,
            final String startPartitionKey, final String startRowKey, final String endPartitionKey,
            final String endRowKey, final String accessPolicyIdentifier, final String tableName,
            final String signature, final String accountKeyName) throws StorageException {
        Utility.assertNotNull("tableName", tableName);
        return generateSharedAccessSignatureHelper(policy, startPartitionKey, startRowKey, endPartitionKey, endRowKey,
                accessPolicyIdentifier, null /* resourceType */, tableName, signature, accountKeyName, null /* headers */);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for blob service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param headers
     *            The optional header values to set for a blob accessed with this shared access signature.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            The resource name.
     * @param client
     *            The ServiceClient associated with the object.
     * @param opContext
     *            An object used to track the execution of the operation
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForBlob(final SharedAccessBlobPolicy policy,
            final SharedAccessBlobHeaders headers, final String accessPolicyIdentifier, final String resourceName,
            final ServiceClient client, final OperationContext opContext) throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForBlob(policy, resourceName, accessPolicyIdentifier, client,
                opContext, headers);

    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for queue service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            The resource name.
     * @param client
     *            The ServiceClient associated with the object.
     * @param opContext
     *            An object used to track the execution of the operation
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */

    public static String generateSharedAccessSignatureHashForQueue(final SharedAccessQueuePolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForQueueAndTable(policy, resourceName, accessPolicyIdentifier, false,
                null, null, null, null, client, opContext);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for blob service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            The resource name.
     * @param client
     *            The ServiceClient associated with the object.
     * @param opContext
     *            An object used to track the execution of the operation
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForTable(final SharedAccessTablePolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final String startPartitionKey,
            final String startRowKey, final String endPartitionKey, final String endRowKey, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForQueueAndTable(policy, resourceName, accessPolicyIdentifier, true,
                startPartitionKey, startRowKey, endPartitionKey, endRowKey, client, opContext);

    }

    /**
     * Parses the query parameters and populates a StorageCredentialsSharedAccessSignature object if one is present.
     * 
     * @param queryParams
     *            The parameters to parse
     * @return The StorageCredentialsSharedAccessSignature if one is present, otherwise null
     * @throws IllegalArgumentException
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    public static StorageCredentialsSharedAccessSignature parseQuery(final HashMap<String, String[]> queryParams)
            throws StorageException {
        String signature = null;
        String signedStart = null;
        String signedExpiry = null;
        String signedResource = null;
        String signedPermissions = null;
        String signedIdentifier = null;
        String signedVersion = null;
        String cacheControl = null;
        String contentType = null;
        String contentEncoding = null;
        String contentLanguage = null;
        String contentDisposition = null;

        boolean sasParameterFound = false;

        StorageCredentialsSharedAccessSignature credentials = null;

        for (final Entry<String, String[]> entry : queryParams.entrySet()) {
            final String lowerKey = entry.getKey().toLowerCase(Utility.LOCALE_US);

            if (lowerKey.equals(Constants.QueryConstants.SIGNED_START)) {
                signedStart = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNED_EXPIRY)) {
                signedExpiry = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNED_PERMISSIONS)) {
                signedPermissions = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNED_RESOURCE)) {
                signedResource = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNED_IDENTIFIER)) {
                signedIdentifier = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNATURE)) {
                signature = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.SIGNED_VERSION)) {
                signedVersion = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.CACHE_CONTROL)) {
                cacheControl = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.CONTENT_TYPE)) {
                contentType = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.CONTENT_ENCODING)) {
                contentEncoding = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.CONTENT_LANGUAGE)) {
                contentLanguage = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(Constants.QueryConstants.CONTENT_DISPOSITION)) {
                contentDisposition = entry.getValue()[0];
                sasParameterFound = true;
            }
        }

        if (sasParameterFound) {
            if (signature == null) {
                final String errorMessage = SR.MISSING_MANDATORY_PARAMETER_FOR_SAS;
                throw new IllegalArgumentException(errorMessage);
            }

            final UriQueryBuilder builder = new UriQueryBuilder();

            if (!Utility.isNullOrEmpty(signedStart)) {
                builder.add(Constants.QueryConstants.SIGNED_START, signedStart);
            }

            if (!Utility.isNullOrEmpty(signedExpiry)) {
                builder.add(Constants.QueryConstants.SIGNED_EXPIRY, signedExpiry);
            }

            if (!Utility.isNullOrEmpty(signedPermissions)) {
                builder.add(Constants.QueryConstants.SIGNED_PERMISSIONS, signedPermissions);
            }

            if (!Utility.isNullOrEmpty(signedResource)) {
                builder.add(Constants.QueryConstants.SIGNED_RESOURCE, signedResource);
            }

            if (!Utility.isNullOrEmpty(signedIdentifier)) {
                builder.add(Constants.QueryConstants.SIGNED_IDENTIFIER, signedIdentifier);
            }

            if (!Utility.isNullOrEmpty(signedVersion)) {
                builder.add(Constants.QueryConstants.SIGNED_VERSION, signedVersion);
            }

            if (!Utility.isNullOrEmpty(signature)) {
                builder.add(Constants.QueryConstants.SIGNATURE, signature);
            }

            if (!Utility.isNullOrEmpty(cacheControl)) {
                builder.add(Constants.QueryConstants.CACHE_CONTROL, cacheControl);
            }

            if (!Utility.isNullOrEmpty(contentType)) {
                builder.add(Constants.QueryConstants.CONTENT_TYPE, contentType);
            }

            if (!Utility.isNullOrEmpty(contentEncoding)) {
                builder.add(Constants.QueryConstants.CONTENT_ENCODING, contentEncoding);
            }

            if (!Utility.isNullOrEmpty(contentLanguage)) {
                builder.add(Constants.QueryConstants.CONTENT_LANGUAGE, contentLanguage);
            }

            if (!Utility.isNullOrEmpty(contentDisposition)) {
                builder.add(Constants.QueryConstants.CONTENT_DISPOSITION, contentDisposition);
            }

            final String token = builder.toString();
            credentials = new StorageCredentialsSharedAccessSignature(token);
        }

        return credentials;
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param permissions
     *            The permissions for a shared access signature URI.
     * @param startTime
     *            The start time for a shared access signature URI.
     * @param expiryTime
     *            The expiry time for a shared access signature URI.
     * @param startPatitionKey
     *            The start partition key for a shared access signature URI.
     * @param startRowKey
     *            The start row key for a shared access signature URI.
     * @param endPatitionKey
     *            The end partition key for a shared access signature URI.
     * @param endRowKey
     *            The end row key for a shared access signature URI.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceType
     *            Either "b" for blobs or "c" for containers.
     * @param tableName
     *            The table name.
     * @param signature
     *            The signature hash.
     * @param accountKeyName
     *            The account key name.
     * @param headers
     *            Optional blob headers.
     * @return The finished query builder
     * @throws StorageException
     */
    private static UriQueryBuilder generateSharedAccessSignatureHelper(final SharedAccessPolicy policy,
            final String startPatitionKey, final String startRowKey, final String endPatitionKey,
            final String endRowKey, final String accessPolicyIdentifier, final String resourceType,
            final String tableName, final String signature, final String accountKeyName,
            final SharedAccessBlobHeaders headers) throws StorageException {
        Utility.assertNotNull("signature", signature);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.SIGNED_VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        if (!Utility.isNullOrEmpty(permissions)) {
            builder.add(Constants.QueryConstants.SIGNED_PERMISSIONS, permissions);
        }

        final String startString = Utility.getUTCTimeOrEmpty(startTime);
        if (!Utility.isNullOrEmpty(startString)) {
            builder.add(Constants.QueryConstants.SIGNED_START, startString);
        }

        final String stopString = Utility.getUTCTimeOrEmpty(expiryTime);
        if (!Utility.isNullOrEmpty(stopString)) {
            builder.add(Constants.QueryConstants.SIGNED_EXPIRY, stopString);
        }

        if (!Utility.isNullOrEmpty(startPatitionKey)) {
            builder.add(Constants.QueryConstants.START_PARTITION_KEY, startPatitionKey);
        }

        if (!Utility.isNullOrEmpty(startRowKey)) {
            builder.add(Constants.QueryConstants.START_ROW_KEY, startRowKey);
        }

        if (!Utility.isNullOrEmpty(endPatitionKey)) {
            builder.add(Constants.QueryConstants.END_PARTITION_KEY, endPatitionKey);
        }

        if (!Utility.isNullOrEmpty(endRowKey)) {
            builder.add(Constants.QueryConstants.END_ROW_KEY, endRowKey);
        }

        if (!Utility.isNullOrEmpty(accessPolicyIdentifier)) {
            builder.add(Constants.QueryConstants.SIGNED_IDENTIFIER, accessPolicyIdentifier);
        }

        if (!Utility.isNullOrEmpty(resourceType)) {
            builder.add(Constants.QueryConstants.SIGNED_RESOURCE, resourceType);
        }

        if (!Utility.isNullOrEmpty(tableName)) {
            builder.add(Constants.QueryConstants.SAS_TABLE_NAME, tableName);
        }

        if (headers != null) {
            if (!Utility.isNullOrEmpty(headers.getCacheControl())) {
                builder.add(Constants.QueryConstants.CACHE_CONTROL, headers.getCacheControl());
            }
            if (!Utility.isNullOrEmpty(headers.getContentType())) {
                builder.add(Constants.QueryConstants.CONTENT_TYPE, headers.getContentType());
            }
            if (!Utility.isNullOrEmpty(headers.getContentEncoding())) {
                builder.add(Constants.QueryConstants.CONTENT_ENCODING, headers.getContentEncoding());
            }
            if (!Utility.isNullOrEmpty(headers.getContentLanguage())) {
                builder.add(Constants.QueryConstants.CONTENT_LANGUAGE, headers.getContentLanguage());
            }
            if (!Utility.isNullOrEmpty(headers.getContentDisposition())) {
                builder.add(Constants.QueryConstants.CONTENT_DISPOSITION, headers.getContentDisposition());
            }
        }

        if (!Utility.isNullOrEmpty(signature)) {
            builder.add(Constants.QueryConstants.SIGNATURE, signature);
        }

        if (!Utility.isNullOrEmpty(accountKeyName)) {
            builder.add(Constants.QueryConstants.SIGNED_KEY, accountKeyName);
        }

        return builder;
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for blob service.
     * 
     * @param permissions
     *            The permissions for a shared access signature.
     * @param startTime
     *            The start time for a shared access signature.
     * @param expiryTime
     *            The expiry time for a shared access signature.
     * @param resourceName
     *            The canonical resource string, unescaped.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param client
     *            Reference to the ServiceClient.
     * @param opContext
     *            An object used to track the execution of the operation.
     * @param headers
     *            The optional header values to set for a blob returned with this SAS.
     * @return
     *         The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureHashForBlob(final SharedAccessPolicy policy,
            final String resourceName, final String accessPolicyIdentifier, final ServiceClient client,
            final OperationContext opContext, final SharedAccessBlobHeaders headers) throws InvalidKeyException,
            StorageException {
        Utility.assertNotNullOrEmpty("resourceName", resourceName);
        Utility.assertNotNull("client", client);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        String cacheControl = null;
        String contentDisposition = null;
        String contentEncoding = null;
        String contentLanguage = null;
        String contentType = null;

        if (headers != null) {
            cacheControl = headers.getCacheControl();
            contentDisposition = headers.getContentDisposition();
            contentEncoding = headers.getContentEncoding();
            contentLanguage = headers.getContentLanguage();
            contentType = headers.getContentType();
        }

        String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
                permissions == null ? Constants.EMPTY_STRING : permissions, Utility.getUTCTimeOrEmpty(startTime),
                Utility.getUTCTimeOrEmpty(expiryTime), resourceName,
                accessPolicyIdentifier == null ? Constants.EMPTY_STRING : accessPolicyIdentifier,
                Constants.HeaderConstants.TARGET_STORAGE_VERSION, cacheControl == null ? Constants.EMPTY_STRING
                        : cacheControl, contentDisposition == null ? Constants.EMPTY_STRING : contentDisposition,
                contentEncoding == null ? Constants.EMPTY_STRING : contentEncoding,
                contentLanguage == null ? Constants.EMPTY_STRING : contentLanguage,
                contentType == null ? Constants.EMPTY_STRING : contentType);

        stringToSign = Utility.safeDecode(stringToSign);
        final String signature = client.getCredentials().computeHmac256(stringToSign, opContext);

        // add logging
        return signature;
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for queue and table service.
     * 
     * @param permissions
     *            The permissions for a shared access signature.
     * @param startTime
     *            The start time for a shared access signature.
     * @param expiryTime
     *            The expiry time for a shared access signature.
     * @param resourceName
     *            The canonical resource string, unescaped.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param client
     *            Reference to the ServiceClient.
     * @param opContext
     *            An object used to track the execution of the operation.
     * @return
     *         the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureHashForQueueAndTable(final SharedAccessPolicy policy,
            final String resourceName, final String accessPolicyIdentifier, final boolean useTableSas,
            final String startPatitionKey, final String startRowKey, final String endPatitionKey,
            final String endRowKey, final ServiceClient client, final OperationContext opContext)
            throws InvalidKeyException, StorageException {
        Utility.assertNotNullOrEmpty("resourceName", resourceName);
        Utility.assertNotNull("client", client);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s", permissions == null ? Constants.EMPTY_STRING
                : permissions, Utility.getUTCTimeOrEmpty(startTime), Utility.getUTCTimeOrEmpty(expiryTime),
                resourceName, accessPolicyIdentifier == null ? Constants.EMPTY_STRING : accessPolicyIdentifier,
                Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        if (useTableSas) {
            stringToSign = String.format("%s\n%s\n%s\n%s\n%s", stringToSign,
                    startPatitionKey == null ? Constants.EMPTY_STRING : startPatitionKey,
                    startRowKey == null ? Constants.EMPTY_STRING : startRowKey,
                    endPatitionKey == null ? Constants.EMPTY_STRING : endPatitionKey,
                    endRowKey == null ? Constants.EMPTY_STRING : endRowKey);
        }

        stringToSign = Utility.safeDecode(stringToSign);
        final String signature = client.getCredentials().computeHmac256(stringToSign, opContext);

        // add logging
        return signature;
    }

    /**
     * Private Default Ctor.
     */
    private SharedAccessSignatureHelper() {
        // No op
    }
}
