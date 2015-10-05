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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;
import com.microsoft.azure.storage.SharedAccessHeaders;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
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
     * Get the signature hash embedded inside the Shared Access Signature for a {@link CloudStorageAccount}.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param ipRange
     *            An optional range of IP addresses.
     * @param protocols
     *            An optional restriction of allowed protocols.
     * @param signature
     *            The signature to use.
     *            
     * @return The finished query builder
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForAccount(
            final SharedAccessAccountPolicy policy, final String signature)
            throws StorageException {
        
        Utility.assertNotNull("policy", policy);
        
        Utility.assertNotNull("signature", signature);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;
        IPRange ipRange = null;
        SharedAccessProtocols protocols = null;
        String services = null;
        String resourceTypes = null;
        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
            ipRange = policy.getRange();
            protocols = policy.getProtocols();
            services = policy.servicesToString();
            resourceTypes = policy.resourceTypesToString();
        }

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.SIGNED_VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_SERVICE, services);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_RESOURCE_TYPE, resourceTypes);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_PERMISSIONS, permissions);

        final String startString = Utility.getUTCTimeOrEmpty(startTime);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_START, startString);

        final String stopString = Utility.getUTCTimeOrEmpty(expiryTime);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_EXPIRY, stopString);

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_RESOURCE, resourceTypes);
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_IP, ipRange != null ? ipRange.toString() : null);
        addIfNotNullOrEmpty(
                builder, Constants.QueryConstants.SIGNED_PROTOCOLS, protocols != null ? protocols.toString() : null);

        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNATURE, signature);

        return builder;
    }
    
    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            The shared access policy for the shared access signature.
     * @param headers
     *            The optional header values to set for a blob or file accessed with this shared access signature.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceType
     *            Either "b" for blobs, "c" for containers, "f" for files, or "s" for shares.
     * @param ipRange
     *            The range of IP addresses for the shared access signature.
     * @param protocols
     *            The Internet protocols for the shared access signature.
     * @param signature
     *            The signature to use.
     *            
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForBlobAndFile(
            final SharedAccessPolicy policy, final SharedAccessHeaders headers, final String groupPolicyIdentifier,
            final String resourceType, final IPRange ipRange, final SharedAccessProtocols protocols, final String signature)
            throws StorageException {
        
        Utility.assertNotNullOrEmpty("resourceType", resourceType);

        return generateSharedAccessSignatureHelper(policy, null /* startPartitionKey */, null /* startRowKey */,
                null /* endPartitionKey */, null /* endRowKey */, groupPolicyIdentifier, resourceType, ipRange,
                protocols, null /* tableName */, signature, headers);
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            The shared access policy for the shared access signature.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param ipRange
     *            The range of IP addresses for the shared access signature.
     * @param protocols
     *            The Internet protocols for the shared access signature.
     * @param signature
     *            The signature to use.
     *            
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForQueue(
            final SharedAccessQueuePolicy policy, final String groupPolicyIdentifier, final IPRange ipRange,
            final SharedAccessProtocols protocols, final String signature)
            throws StorageException {

        return generateSharedAccessSignatureHelper(policy, null /* startPartitionKey */, null /* startRowKey */,
                null /* endPartitionKey */, null /* endRowKey */, groupPolicyIdentifier, null /* resourceType */,
                ipRange, protocols, null /* tableName */, signature, null /* headers */);
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            The shared access policy for the shared access signature.
     * @param startPartitionKey
     *            An optional restriction of the beginning of the range of partition keys to include.
     * @param startRowKey
     *            An optional restriction of the beginning of the range of row keys to include.
     * @param endPartitionKey
     *            An optional restriction of the end of the range of partition keys to include.
     * @param endRowKey
     *            An optional restriction of the end of the range of row keys to include.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param ipRange
     *            The range of IP addresses for the shared access signature.
     * @param protocols
     *            The Internet protocols for the shared access signature.
     * @param tableName
     *            The table name.
     * @param signature
     *            The signature to use.
     *            
     * @return The finished query builder
     * @throws IllegalArgumentException
     * @throws StorageException
     */
    public static UriQueryBuilder generateSharedAccessSignatureForTable(
            final SharedAccessTablePolicy policy, final String startPartitionKey, final String startRowKey,
            final String endPartitionKey, final String endRowKey, final String accessPolicyIdentifier,
            final IPRange ipRange, final SharedAccessProtocols protocols, final String tableName, final String signature)
            throws StorageException {
        
        Utility.assertNotNull("tableName", tableName);
        return generateSharedAccessSignatureHelper(
                policy, startPartitionKey, startRowKey, endPartitionKey, endRowKey, accessPolicyIdentifier,
                null /* resourceType */, ipRange, protocols, tableName, signature, null /* headers */);
    }
    
    /**
     * Get the signature hash embedded inside the Shared Access Signature for a {@link CloudStorageAccount}.
     * 
     * @param accountName
     *            The name of the account to use for the SAS.
     * @param policy
     *            The shared access policy to hash.
     * @param ipRange
     *            An optional range of IP addresses.
     * @param protocols
     *            An optional restriction of allowed protocols.
     * @param creds
     *            The {@link StorageCredentials} associated with the object.
     *            
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForAccount(
            final String accountName, final SharedAccessAccountPolicy policy, final StorageCredentials creds)
            throws InvalidKeyException, StorageException {
        Utility.assertNotNullOrEmpty("resource", accountName);
        Utility.assertNotNull("credentials", creds);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;
        IPRange ipRange = null;
        SharedAccessProtocols protocols = null;
        String services = null;
        String resourceTypes = null;
        
        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
            ipRange = policy.getRange();
            protocols = policy.getProtocols();
            services = policy.servicesToString();
            resourceTypes = policy.resourceTypesToString();
        }
        
        
        String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
                accountName, permissions == null ? Constants.EMPTY_STRING : permissions, services, resourceTypes,
                Utility.getUTCTimeOrEmpty(startTime), Utility.getUTCTimeOrEmpty(expiryTime),
                ipRange == null ? Constants.EMPTY_STRING : ipRange.toString(),
                protocols == null ? Constants.EMPTY_STRING : protocols.toString(),
                Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        return generateSharedAccessSignatureHashHelper(stringToSign, creds);
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
     * @param ipRange
     *            The range of IP addresses to hash.
     * @param protocols
     *            The Internet protocols to hash.
     * @param client
     *            The ServiceClient associated with the object.
     *            
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForBlobAndFile(final SharedAccessPolicy policy,
            SharedAccessHeaders headers, final String accessPolicyIdentifier, final String resourceName,
            final IPRange ipRange, final SharedAccessProtocols protocols, final ServiceClient client)
            throws InvalidKeyException, StorageException {
        
        String stringToSign = generateSharedAccessSignatureStringToSign(
                policy, resourceName, ipRange, protocols, accessPolicyIdentifier);

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
            
        stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s", stringToSign,
                cacheControl == null ? Constants.EMPTY_STRING : cacheControl,
                contentDisposition == null ? Constants.EMPTY_STRING : contentDisposition,
                contentEncoding == null ? Constants.EMPTY_STRING : contentEncoding,
                contentLanguage == null ? Constants.EMPTY_STRING : contentLanguage,
                contentType == null ? Constants.EMPTY_STRING : contentType);
        
        return generateSharedAccessSignatureHashHelper(stringToSign, client.getCredentials());
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
     * @param ipRange
     *            The range of IP addresses to hash.
     * @param protocols
     *            The Internet protocols to hash.
     * @param client
     *            The ServiceClient associated with the object.
     *            
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */

    public static String generateSharedAccessSignatureHashForQueue(
            final SharedAccessQueuePolicy policy, final String accessPolicyIdentifier, final String resourceName,
            final IPRange ipRange, final SharedAccessProtocols protocols, final ServiceClient client)
            throws InvalidKeyException, StorageException {
        
        final String stringToSign = generateSharedAccessSignatureStringToSign(
                policy, resourceName, ipRange, protocols, accessPolicyIdentifier);
        
        return generateSharedAccessSignatureHashHelper(stringToSign, client.getCredentials());
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
     * @param ipRange
     *            The range of IP addresses to hash.
     * @param protocols
     *            The Internet protocols to hash.
     * @param startPartitionKey
     *            An optional restriction of the beginning of the range of partition keys to hash.
     * @param startRowKey
     *            An optional restriction of the beginning of the range of row keys to hash.
     * @param endPartitionKey
     *            An optional restriction of the end of the range of partition keys to hash.
     * @param endRowKey
     *            An optional restriction of the end of the range of row keys to hash.
     * @param client
     *            The ServiceClient associated with the object.
     *            
     * @return The signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHashForTable(
            final SharedAccessTablePolicy policy, final String accessPolicyIdentifier, final String resourceName,
            final IPRange ipRange, final SharedAccessProtocols protocols, final String startPartitionKey,
            final String startRowKey, final String endPartitionKey, final String endRowKey, final ServiceClient client)
            throws InvalidKeyException, StorageException {

        String stringToSign = generateSharedAccessSignatureStringToSign(
                policy, resourceName, ipRange, protocols, accessPolicyIdentifier);
        
        stringToSign = String.format("%s\n%s\n%s\n%s\n%s", stringToSign,
                startPartitionKey == null ? Constants.EMPTY_STRING : startPartitionKey,
                startRowKey == null ? Constants.EMPTY_STRING : startRowKey,
                endPartitionKey == null ? Constants.EMPTY_STRING : endPartitionKey,
                endRowKey == null ? Constants.EMPTY_STRING : endRowKey);
        
        return generateSharedAccessSignatureHashHelper(stringToSign, client.getCredentials());
    }

    /**
     * Parses the query parameters and populates a StorageCredentialsSharedAccessSignature object if one is present.
     * 
     * @param completeUri
     *            A {@link StorageUri} object which represents the complete Uri.
     *            
     * @return The StorageCredentialsSharedAccessSignature if one is present, otherwise null.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    public static StorageCredentialsSharedAccessSignature parseQuery(final StorageUri completeUri) throws StorageException {
        final HashMap<String, String[]> queryParameters = PathUtility.parseQueryString(completeUri.getQuery());
        return parseQuery(queryParameters);
    }

    /**
     * Parses the query parameters and populates a StorageCredentialsSharedAccessSignature object if one is present.
     * 
     * @param queryParams
     *            The parameters to parse.
     *            
     * @return The StorageCredentialsSharedAccessSignature if one is present, otherwise null.
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
     *            
     * @throws StorageException
     *            An exception representing any error which occurred during the operation.
     */
    private static void addIfNotNullOrEmpty(UriQueryBuilder builder, String name, String val) throws StorageException {
        if (!Utility.isNullOrEmpty(val)) {
            builder.add(name, val);
        }
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     * 
     * @param policy
     *            A {@link SharedAccessPolicy} containing the permissions for the SAS.
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
     *            The resource type for a shared access signature URI.
     * @param ipRange
     *            The range of IP addresses for the shared access signature.
     * @param protocols
     *            The Internet protocols for the shared access signature.
     * @param tableName
     *            The table name.
     * @param signature
     *            The signature hash.
     * @param headers
     *            Optional blob or file headers.
     *            
     * @return The finished query builder.
     * @throws StorageException
     *            An exception representing any error which occurred during the operation.
     */
    private static UriQueryBuilder generateSharedAccessSignatureHelper(
            final SharedAccessPolicy policy, final String startPartitionKey, final String startRowKey,
            final String endPartitionKey, final String endRowKey, final String accessPolicyIdentifier,
            final String resourceType, final IPRange ipRange, final SharedAccessProtocols protocols,
            final String tableName, final String signature, final SharedAccessHeaders headers)
            throws StorageException {
        
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
        addIfNotNullOrEmpty(builder, Constants.QueryConstants.SIGNED_IP, ipRange != null ? ipRange.toString() : null);
        addIfNotNullOrEmpty(
                builder, Constants.QueryConstants.SIGNED_PROTOCOLS, protocols != null ? protocols.toString() : null);

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
     * Get the signature hash embedded inside the Shared Access Signature.
     *
     * @param stringToSign
     *            The string to decode and hash
     * @param creds
     *            Reference to the {@link StorageCredentials.}.
     * 
     * @return The signature hash embedded inside the Shared Access Signature.
     *         
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureHashHelper(String stringToSign, final StorageCredentials creds)
            throws StorageException, InvalidKeyException {
        
        Utility.assertNotNull("credentials", creds);
    
        Logger.trace(null, LogConstants.SIGNING, stringToSign);

        stringToSign = Utility.safeDecode(stringToSign);
        return StorageCredentialsHelper.computeHmac256(creds, stringToSign);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature.
     * 
     * @param policy
     *            A {@link SharedAccessPolicy} containing the permissions for the SAS.
     * @param resource
     *            The canonical resource (or resource type) string, unescaped.
     * @param ipRange
     *            The range of IP addresses to hash.
     * @param protocols
     *            The Internet protocols to hash.
     * @param headers
     *            The optional header values to set for a blob or file accessed with this shared access signature.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     *            
     * @return The signature hash embedded inside the Shared Access Signature.
     *         
     * @throws InvalidKeyException
     * @throws StorageException
     */
    private static String generateSharedAccessSignatureStringToSign(
            final SharedAccessPolicy policy, final String resource, final IPRange ipRange,
            final SharedAccessProtocols protocols, final String accessPolicyIdentifier)
            throws InvalidKeyException, StorageException {
        
        Utility.assertNotNullOrEmpty("resource", resource);

        String permissions = null;
        Date startTime = null;
        Date expiryTime = null;
        
        if (policy != null) {
            permissions = policy.permissionsToString();
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }
        
        String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
                permissions == null ? Constants.EMPTY_STRING : permissions,
                Utility.getUTCTimeOrEmpty(startTime), Utility.getUTCTimeOrEmpty(expiryTime), resource,
                accessPolicyIdentifier == null ? Constants.EMPTY_STRING : accessPolicyIdentifier,
                ipRange == null ? Constants.EMPTY_STRING : ipRange.toString(),
                protocols == null ? Constants.EMPTY_STRING : protocols.toString(),
                Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        return stringToSign;
    }
    
    /**
     * Private Default Ctor.
     */
    private SharedAccessSignatureHelper() {
        // No op
    }
}
