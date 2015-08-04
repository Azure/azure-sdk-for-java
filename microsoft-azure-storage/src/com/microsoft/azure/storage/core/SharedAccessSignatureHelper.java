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
package com.microsoft.azure.storage.core;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.SharedAccessHeaders;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.queue.SharedAccessQueuePolicy;
import com.microsoft.azure.storage.table.SharedAccessTablePolicy;

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
     *            Either "b" for blobs, "c" for containers, "f" for files, or "s" for shares.
     * @param signature
     *            The signature to use.
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForBlobAndFile(
            final SharedAccessPolicy policy, final SharedAccessHeaders headers,
            final String groupPolicyIdentifier, final String resourceType, final String signature)
            throws StorageException {
        Utility.assertNotNullOrEmpty("resourceType", resourceType);

        return generateSharedAccessSignatureHelper(policy, null /* startPartitionKey */, null /* startRowKey */,
                null /* endPartitionKey */, null /* endRowKey */, groupPolicyIdentifier, resourceType,
                null /* tableName */, signature, headers);
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
                null /* tableName */, signature, null /* headers */);
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     */
    public static UriQueryBuilder generateSharedAccessSignatureForTable(final SharedAccessTablePolicy policy,
            final String startPartitionKey, final String startRowKey, final String endPartitionKey,
            final String endRowKey, final String accessPolicyIdentifier, final String tableName,
            final String signature) throws StorageException {
        Utility.assertNotNull("tableName", tableName);
        return generateSharedAccessSignatureHelper(policy, startPartitionKey, startRowKey, endPartitionKey, endRowKey,
                accessPolicyIdentifier, null /* resourceType */, tableName, signature, null /* headers */);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for the blob or file service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param headers
     *            The optional header values to set for a blob or file accessed with this shared access signature.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            The resource name.
     * @param client
     *            The ServiceClient associated with the object.
     * 
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForBlobAndFile(final SharedAccessPolicy policy,
            final SharedAccessHeaders headers, final String accessPolicyIdentifier, final String resourceName,
            final ServiceClient client) throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForBlobAndFile(
                policy, resourceName, accessPolicyIdentifier, client, headers);
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
     * 
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */

    public static String generateSharedAccessSignatureHashForQueue(final SharedAccessQueuePolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final ServiceClient client)
            throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForQueueAndTable(
                policy, resourceName, accessPolicyIdentifier, false, null, null, null, null, client);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for the table service.
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
            final String startRowKey, final String endPartitionKey, final String endRowKey, final ServiceClient client)
            throws InvalidKeyException, StorageException {
        return generateSharedAccessSignatureHashForQueueAndTable(policy, resourceName, accessPolicyIdentifier, true,
                startPartitionKey, startRowKey, endPartitionKey, endRowKey, client);
    }
    
    /**
     * Parses the query parameters and populates a StorageCredentialsSharedAccessSignature object if one is present.
     * 
     * @param completeUri
     *            A {@link StorageUri} object which represents the complete Uri.
     * @return The StorageCredentialsSharedAccessSignature if one is present, otherwise null
     * @throws IllegalArgumentException
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    public static StorageCredentialsSharedAccessSignature parseQuery(final StorageUri completeUri)
            throws StorageException {
        final HashMap<String, String[]> queryParameters = PathUtility.parseQueryString(completeUri.getQuery());
        return parseQuery(queryParameters);
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
        boolean sasParameterFound = false;
        List<String> removeList = new ArrayList<String>();
        for (final Entry<String, String[]> entry : queryParams.entrySet()) {
            final String lowerKey = entry.getKey().toLowerCase(Utility.LOCALE_US);

            if (lowerKey.equals(Constants.QueryConstants.SIGNATURE)) {
                sasParameterFound = true;
            } else if (lowerKey.equals(Constants.QueryConstants.COMPONENT)) {
                removeList.add(entry.getKey());
            } else if (lowerKey.equals(Constants.QueryConstants.RESOURCETYPE)) {
                removeList.add(entry.getKey());
            } else if (lowerKey.equals(Constants.QueryConstants.SNAPSHOT)) {
                removeList.add(entry.getKey());
            } else if (lowerKey.equals(Constants.QueryConstants.API_VERSION)) {
                removeList.add(entry.getKey());
            } 
        }
        
        for (String removeParam : removeList) {
            queryParams.remove(removeParam);
        }

        if (sasParameterFound) {
            final UriQueryBuilder builder = new UriQueryBuilder();
            
            StringBuilder values = new StringBuilder();
            for (final Entry<String, String[]> entry : queryParams.entrySet()) {
                values.setLength(0);
                for (int i = 0; i < entry.getValue().length; i++) {
                    values.append(entry.getValue()[i]);
                    values.append(',');
                }
                values.deleteCharAt(values.length() - 1);
                
                addIfNotNullOrEmpty(builder, entry.getKey().toLowerCase(), values.toString());
            }

            return new StorageCredentialsSharedAccessSignature(builder.toString());
        }

        return null;
    }

    /**
     * Helper to add a name/value pair to a <code>UriQueryBuilder</code> if the value is not null or empty.
     * 
     * @param builder
     *            The builder to add to.
     * @param name
     *            The name to add.
     * @param val
     *            The value to add if not null or empty.
     * @throws StorageException
     */
    private static void addIfNotNullOrEmpty(UriQueryBuilder builder, String name, String val) throws StorageException {
        if (!Utility.isNullOrEmpty(val)) {
            builder.add(name, val);
        }
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
     * @param startPartitionKey
     *            The start partition key for a shared access signature URI.
     * @param startRowKey
     *            The start row key for a shared access signature URI.
     * @param endPartitionKey
     *            The end partition key for a shared access signature URI.
     * @param endRowKey
     *            The end row key for a shared access signature URI.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceType
     *            Either "b" for blobs, "c" for containers, "f" for files, or "s" for shares.
     * @param tableName
     *            The table name.
     * @param signature
     *            The signature hash.
     * @param headers
     *            Optional blob or file headers.
     * @return The finished query builder
     * @throws StorageException
     */
    private static UriQueryBuilder generateSharedAccessSignatureHelper(final SharedAccessPolicy policy,
            final String startPartitionKey, final String startRowKey, final String endPartitionKey,
            final String endRowKey, final String accessPolicyIdentifier, final String resourceType,
            final String tableName, final String signature, final SharedAccessHeaders headers) throws StorageException {
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

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_PERMISSIONS, permissions);

        final String startString = Utility.getUTCTimeOrEmpty(startTime);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_START, startString);

        final String stopString = Utility.getUTCTimeOrEmpty(expiryTime);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_EXPIRY, stopString);

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.START_PARTITION_KEY, startPartitionKey);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.START_ROW_KEY, startRowKey);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.END_PARTITION_KEY, endPartitionKey);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.END_ROW_KEY, endRowKey);

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_IDENTIFIER, accessPolicyIdentifier);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_RESOURCE, resourceType);

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SAS_TABLE_NAME, tableName);

        if (headers != null) {
            addIfNotNullOrEmpty(builder, Constants.QueryConstants.CACHE_CONTROL, headers.getCacheControl());
            addIfNotNullOrEmpty(builder, Constants.QueryConstants.CONTENT_TYPE, headers.getContentType());
            addIfNotNullOrEmpty(builder, Constants.QueryConstants.CONTENT_ENCODING, headers.getContentEncoding());
            addIfNotNullOrEmpty(builder, Constants.QueryConstants.CONTENT_LANGUAGE, headers.getContentLanguage());
            addIfNotNullOrEmpty(builder, Constants.QueryConstants.CONTENT_DISPOSITION, headers.getContentDisposition());
        }

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNATURE, signature);

        return builder;
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for the blob or file service.
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
     * @param headers
     *            The optional header values to set for a blob or file returned with this SAS.
     * @return
     *         The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureHashForBlobAndFile(final SharedAccessPolicy policy,
            final String resourceName, final String accessPolicyIdentifier, final ServiceClient client,
            final SharedAccessHeaders headers) throws InvalidKeyException,
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
        final String signature = StorageCredentialsHelper.computeHmac256(client.getCredentials(), stringToSign);

        Logger.trace(null, LogConstants.SIGNING, stringToSign);
        
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
     * @return
     *         the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureHashForQueueAndTable(final SharedAccessPolicy policy,
            final String resourceName, final String accessPolicyIdentifier, final boolean useTableSas,
            final String startPartitionKey, final String startRowKey, final String endPartitionKey,
            final String endRowKey, final ServiceClient client)
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
                    startPartitionKey == null ? Constants.EMPTY_STRING : startPartitionKey,
                    startRowKey == null ? Constants.EMPTY_STRING : startRowKey,
                    endPartitionKey == null ? Constants.EMPTY_STRING : endPartitionKey,
                    endRowKey == null ? Constants.EMPTY_STRING : endRowKey);
        }

        stringToSign = Utility.safeDecode(stringToSign);
        final String signature = StorageCredentialsHelper.computeHmac256(client.getCredentials(), stringToSign);

        Logger.trace(null, LogConstants.SIGNING, stringToSign);
        
        return signature;
    }

    /**
     * Private Default Ctor.
     */
    private SharedAccessSignatureHelper() {
        // No op
    }
}
