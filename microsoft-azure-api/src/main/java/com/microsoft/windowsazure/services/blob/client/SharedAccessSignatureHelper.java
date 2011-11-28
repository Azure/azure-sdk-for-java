package com.microsoft.windowsazure.services.blob.client;

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. Contains helper methods for implementing shared access signatures.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
final class SharedAccessSignatureHelper {
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
    protected static UriQueryBuilder generateSharedAccessSignature(final SharedAccessPolicy policy,
            final String groupPolicyIdentifier, final String resourceType, final String signature)
            throws StorageException {
        Utility.assertNotNullOrEmpty("resourceType", resourceType);
        Utility.assertNotNull("signature", signature);

        final UriQueryBuilder builder = new UriQueryBuilder();
        if (policy != null) {
            String permissions = SharedAccessPolicy.permissionsToString(policy.getPermissions());

            if (Utility.isNullOrEmpty(permissions)) {
                permissions = null;
            }

            final String startString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime());
            if (!Utility.isNullOrEmpty(startString)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_START, startString);
            }

            final String stopString = Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime());
            if (!Utility.isNullOrEmpty(stopString)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_EXPIRY, stopString);
            }

            if (!Utility.isNullOrEmpty(permissions)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_PERMISSIONS, permissions);
            }
        }

        builder.add(BlobConstants.QueryConstants.SIGNED_RESOURCE, resourceType);

        if (!Utility.isNullOrEmpty(groupPolicyIdentifier)) {
            builder.add(BlobConstants.QueryConstants.SIGNED_IDENTIFIER, groupPolicyIdentifier);
        }

        if (!Utility.isNullOrEmpty(signature)) {
            builder.add(BlobConstants.QueryConstants.SIGNATURE, signature);
        }

        return builder;
    }

    /**
     * Get the signature hash embedded inside the Shared Access Signature.
     * 
     * @param policy
     *            The shared access policy to hash.
     * @param groupPolicyIdentifier
     *            An optional identifier for the policy.
     * @param resourceName
     *            the resource name.
     * @param client
     *            the CloudBlobClient associated with the object.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the signature hash embedded inside the Shared Access Signature.
     * @throws InvalidKeyException
     * @throws StorageException
     */
    protected static String generateSharedAccessSignatureHash(final SharedAccessPolicy policy,
            final String groupPolicyIdentifier, final String resourceName, final CloudBlobClient client,
            final OperationContext opContext) throws InvalidKeyException, StorageException {
        Utility.assertNotNullOrEmpty("resourceName", resourceName);
        Utility.assertNotNull("client", client);

        String stringToSign = null;

        if (policy == null) {
            // Revokable access
            Utility.assertNotNullOrEmpty("groupPolicyIdentifier", groupPolicyIdentifier);
            stringToSign = String.format("%s\n%s\n%s\n%s\n%s", Constants.EMPTY_STRING, Constants.EMPTY_STRING,
                    Constants.EMPTY_STRING, resourceName, groupPolicyIdentifier);
        }
        else {
            // Non Revokable access
            if (policy.getSharedAccessExpiryTime() == null) {
                throw new IllegalArgumentException("Policy Expiry time is mandatory and cannot be null");
            }

            if (policy.getPermissions() == null) {
                throw new IllegalArgumentException("Policy permissions are mandatory and cannot be null");
            }

            stringToSign = String.format("%s\n%s\n%s\n%s\n%s",
                    SharedAccessPolicy.permissionsToString(policy.getPermissions()),
                    Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime()),
                    Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime()), resourceName,
                    groupPolicyIdentifier == null ? Constants.EMPTY_STRING : groupPolicyIdentifier);
        }

        stringToSign = Utility.safeDecode(stringToSign);
        final String signature = client.getCredentials().computeHmac256(stringToSign, opContext);

        // add logging
        return signature;
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
    protected static StorageCredentialsSharedAccessSignature parseQuery(final HashMap<String, String[]> queryParams)
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

            if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_START)) {
                signedStart = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_EXPIRY)) {
                signedExpiry = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_PERMISSIONS)) {
                sigendPermissions = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_RESOURCE)) {
                signedResource = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_IDENTIFIER)) {
                signedIdentifier = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNATURE)) {
                signature = entry.getValue()[0];
                sasParameterFound = true;
            }
            else if (lowerKey.equals(BlobConstants.QueryConstants.SIGNED_VERSION)) {
                signedVersion = entry.getValue()[0];
                sasParameterFound = true;
            }
        }

        if (sasParameterFound) {
            if (signature == null || signedResource == null) {
                final String errorMessage = "Missing mandatory parameters for valid Shared Access Signature";
                throw new IllegalArgumentException(errorMessage);
            }

            final UriQueryBuilder builder = new UriQueryBuilder();

            if (!Utility.isNullOrEmpty(signedStart)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_START, signedStart);
            }

            if (!Utility.isNullOrEmpty(signedExpiry)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_EXPIRY, signedExpiry);
            }

            if (!Utility.isNullOrEmpty(sigendPermissions)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_PERMISSIONS, sigendPermissions);
            }

            builder.add(BlobConstants.QueryConstants.SIGNED_RESOURCE, signedResource);

            if (!Utility.isNullOrEmpty(signedIdentifier)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_IDENTIFIER, signedIdentifier);
            }

            if (!Utility.isNullOrEmpty(signedVersion)) {
                builder.add(BlobConstants.QueryConstants.SIGNED_VERSION, signedVersion);
            }

            if (!Utility.isNullOrEmpty(signature)) {
                builder.add(BlobConstants.QueryConstants.SIGNATURE, signature);
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
