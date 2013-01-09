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
package com.microsoft.windowsazure.services.blob.core.storage;

import java.security.InvalidKeyException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.windowsazure.services.blob.client.SharedAccessBlobPolicy;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ServiceClient;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.queue.client.SharedAccessQueuePolicy;
import com.microsoft.windowsazure.services.table.client.SharedAccessTablePolicy;

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
    public static UriQueryBuilder generateSharedAccessSignature(final SharedAccessBlobPolicy policy,
            final String groupPolicyIdentifier, final String resourceType, final String signature)
            throws StorageException {
        Utility.assertNotNullOrEmpty("resourceType", resourceType);
        Utility.assertNotNull("signature", signature);

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.SIGNED_VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        if (policy != null) {
            String permissions = SharedAccessBlobPolicy.permissionsToString(policy.getPermissions());

            if (Utility.isNullOrEmpty(permissions)) {
                permissions = null;
            }

            final String startString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime());
            if (!Utility.isNullOrEmpty(startString)) {
                builder.add(Constants.QueryConstants.SIGNED_START, startString);
            }

            final String stopString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime());
            if (!Utility.isNullOrEmpty(stopString)) {
                builder.add(Constants.QueryConstants.SIGNED_EXPIRY, stopString);
            }

            if (!Utility.isNullOrEmpty(permissions)) {
                builder.add(Constants.QueryConstants.SIGNED_PERMISSIONS, permissions);
            }
        }

        builder.add(Constants.QueryConstants.SIGNED_RESOURCE, resourceType);

        if (!Utility.isNullOrEmpty(groupPolicyIdentifier)) {
            builder.add(Constants.QueryConstants.SIGNED_IDENTIFIER, groupPolicyIdentifier);
        }

        if (!Utility.isNullOrEmpty(signature)) {
            builder.add(Constants.QueryConstants.SIGNATURE, signature);
        }

        return builder;
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
    public static UriQueryBuilder generateSharedAccessSignature(final SharedAccessQueuePolicy policy,
            final String groupPolicyIdentifier, final String signature) throws StorageException {
        Utility.assertNotNull("signature", signature);

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.SIGNED_VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION);

        if (policy != null) {
            String permissions = SharedAccessQueuePolicy.permissionsToString(policy.getPermissions());

            if (Utility.isNullOrEmpty(permissions)) {
                permissions = null;
            }

            final String startString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime());
            if (!Utility.isNullOrEmpty(startString)) {
                builder.add(Constants.QueryConstants.SIGNED_START, startString);
            }

            final String stopString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime());
            if (!Utility.isNullOrEmpty(stopString)) {
                builder.add(Constants.QueryConstants.SIGNED_EXPIRY, stopString);
            }

            if (!Utility.isNullOrEmpty(permissions)) {
                builder.add(Constants.QueryConstants.SIGNED_PERMISSIONS, permissions);
            }
        }

        if (!Utility.isNullOrEmpty(groupPolicyIdentifier)) {
            builder.add(Constants.QueryConstants.SIGNED_IDENTIFIER, groupPolicyIdentifier);
        }

        if (!Utility.isNullOrEmpty(signature)) {
            builder.add(Constants.QueryConstants.SIGNATURE, signature);
        }

        return builder;
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
    public static UriQueryBuilder generateSharedAccessSignature(final String permissions, final Date startTime,
            final Date expiryTime, final String startPatitionKey, final String startRowKey,
            final String endPatitionKey, final String endRowKey, final String accessPolicyIdentifier,
            final String resourceType, final String tableName, final String signature, final String accountKeyName)
            throws StorageException {
        Utility.assertNotNull("signature", signature);

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

        if (!Utility.isNullOrEmpty(signature)) {
            builder.add(Constants.QueryConstants.SIGNATURE, signature);
        }

        if (!Utility.isNullOrEmpty(accountKeyName)) {
            builder.add(Constants.QueryConstants.SIGNED_KEY, accountKeyName);
        }

        return builder;
    }

    /**
     * Get the complete query builder for creating the Shared Access Signature query.
     */
    public static UriQueryBuilder generateSharedAccessSignature(final SharedAccessTablePolicy policy,
            final String startPartitionKey, final String startRowKey, final String endPartitionKey,
            final String endRowKey, final String accessPolicyIdentifier, final String tableName,
            final String signature, final String accountKeyName) throws StorageException {

        String permissionString = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissionString = SharedAccessTablePolicy.permissionsToString(policy.getPermissions());
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        return generateSharedAccessSignature(permissionString, startTime, expiryTime, startPartitionKey, startRowKey,
                endPartitionKey, endRowKey, accessPolicyIdentifier, null, tableName, signature, accountKeyName);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for blob service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param accessPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            the resource name.
     * @param client
     *            the ServiceClient associated with the object.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHash(final SharedAccessBlobPolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        String permissionString = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissionString = SharedAccessBlobPolicy.permissionsToString(policy.getPermissions());
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        return generateSharedAccessSignatureHash(permissionString, startTime, expiryTime, resourceName,
                accessPolicyIdentifier, false, null, null, null, null, client, opContext);

    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for queue service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            the resource name.
     * @param client
     *            the ServiceClient associated with the object.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */

    public static String generateSharedAccessSignatureHash(final SharedAccessQueuePolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {

        String permissionString = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissionString = SharedAccessQueuePolicy.permissionsToString(policy.getPermissions());
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        return generateSharedAccessSignatureHash(permissionString, startTime, expiryTime, resourceName,
                accessPolicyIdentifier, false, null, null, null, null, client, opContext);
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature for all storage service.
     * 
     * @return the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHash(final String permissions, final Date startTime,
            final Date expiryTime, final String resourceName, final String accessPolicyIdentifier,
            final boolean useTableSas, final String startPatitionKey, final String startRowKey,
            final String endPatitionKey, final String endRowKey, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        Utility.assertNotNullOrEmpty("resourceName", resourceName);
        Utility.assertNotNull("client", client);

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
     * Get the signature hash embedded inside the Shared Access Signature for blob service.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            the resource name.
     * @param client
     *            the ServiceClient associated with the object.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    public static String generateSharedAccessSignatureHash(final SharedAccessTablePolicy policy,
            final String accessPolicyIdentifier, final String resourceName, final String startPartitionKey,
            final String startRowKey, final String endPartitionKey, final String endRowKey, final ServiceClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        String permissionString = null;
        Date startTime = null;
        Date expiryTime = null;

        if (policy != null) {
            permissionString = SharedAccessTablePolicy.permissionsToString(policy.getPermissions());
            startTime = policy.getSharedAccessStartTime();
            expiryTime = policy.getSharedAccessExpiryTime();
        }

        return generateSharedAccessSignatureHash(permissionString, startTime, expiryTime, resourceName,
                accessPolicyIdentifier, true, startPartitionKey, startRowKey, endPartitionKey, endRowKey, client,
                opContext);

    }

    /**
     * Parses the query parameters and populates a StorageCredentialsSharedAccessSignature object if one is present.
     * 
     * @param queryParams
     *            the parameters to parse
     * @return the StorageCredentialsSharedAccessSignature if one is present, otherwise null
     * @throws IllegalArgumentException
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    public static StorageCredentialsSharedAccessSignature parseQuery(final HashMap<String, String[]> queryParams)
            throws StorageException {
        String signature = null;
        String signedStart = null;
        String signedExpiry = null;
        String signedResource = null;
        String sigendPermissions = null;
        String signedIdentifier = null;
        String signedVersion = null;

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
                sigendPermissions = entry.getValue()[0];
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
        }

        if (sasParameterFound) {
            if (signature == null) {
                final String errorMessage = "Missing mandatory parameters for valid Shared Access Signature";
                throw new IllegalArgumentException(errorMessage);
            }

            final UriQueryBuilder builder = new UriQueryBuilder();

            if (!Utility.isNullOrEmpty(signedStart)) {
                builder.add(Constants.QueryConstants.SIGNED_START, signedStart);
            }

            if (!Utility.isNullOrEmpty(signedExpiry)) {
                builder.add(Constants.QueryConstants.SIGNED_EXPIRY, signedExpiry);
            }

            if (!Utility.isNullOrEmpty(sigendPermissions)) {
                builder.add(Constants.QueryConstants.SIGNED_PERMISSIONS, sigendPermissions);
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

            final String token = builder.toString();
            credentials = new StorageCredentialsSharedAccessSignature(token);
        }

        return credentials;
    }

    /**
     * Private Default Ctor.
     */
    private SharedAccessSignatureHelper() {
        // No op
    }
}
