// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.ModelBridgeInternal;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used internally by client (for generating the auth header with master/system key)
 * to generate the master-key auth header for communication with Azure Cosmos DB database service.
 */
public class BaseAuthorizationTokenProvider implements AuthorizationTokenProvider {

    private static final String AUTH_PREFIX = "type=master&ver=1.0&sig=";
    private final AzureKeyCredential credential;
    private volatile String currentCredentialKey;
    private volatile MacPool macPool;

    private final Lock macInstanceLock = new ReentrantLock();

    public BaseAuthorizationTokenProvider(AzureKeyCredential credential) {
        this.credential = credential;
        reInitializeIfPossible();
    }

    private static String getResourceSegment(ResourceType resourceType) {
        switch (resourceType) {
        case Attachment:
            return Paths.ATTACHMENTS_PATH_SEGMENT;
        case Database:
            return Paths.DATABASES_PATH_SEGMENT;
        case Conflict:
            return Paths.CONFLICTS_PATH_SEGMENT;
        case Document:
            return Paths.DOCUMENTS_PATH_SEGMENT;
        case DocumentCollection:
            return Paths.COLLECTIONS_PATH_SEGMENT;
        case Offer:
            return Paths.OFFERS_PATH_SEGMENT;
        case Permission:
            return Paths.PERMISSIONS_PATH_SEGMENT;
        case StoredProcedure:
            return Paths.STORED_PROCEDURES_PATH_SEGMENT;
        case Trigger:
            return Paths.TRIGGERS_PATH_SEGMENT;
        case UserDefinedFunction:
            return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
        case User:
            return Paths.USERS_PATH_SEGMENT;
        case PartitionKeyRange:
            return Paths.PARTITION_KEY_RANGES_PATH_SEGMENT;
        case Media:
            return Paths.MEDIA_PATH_SEGMENT;
        case DatabaseAccount:
            return "";
        default:
            return null;
        }
    }

    /**
     * This API is a helper method to create auth header based on client request using masterkey.
     *
     * @param verb                 the verb.
     * @param resourceIdOrFullName the resource id or full name
     * @param resourceType         the resource type.
     * @param headers              the request headers.
     * @return the key authorization signature.
     */
    public String generateKeyAuthorizationSignature(RequestVerb verb,
                                                    String resourceIdOrFullName,
                                                    ResourceType resourceType,
                                                    Map<String, String> headers) {
        return this.generateKeyAuthorizationSignature(verb, resourceIdOrFullName,
                BaseAuthorizationTokenProvider.getResourceSegment(resourceType), headers);
    }

    /**
     * This API is a helper method to create auth header based on client request using masterkey.
     *
     * @param verb                 the verb
     * @param resourceIdOrFullName the resource id or full name
     * @param  resourceSegment     the resource segment
     * @param headers              the request headers
     * @return the key authorization signature
     */
    public String generateKeyAuthorizationSignature(RequestVerb verb,
            String resourceIdOrFullName,
            String resourceSegment,
            Map<String, String> headers) {
        if (verb == null) {
            throw new IllegalArgumentException("verb");
        }

        if (resourceIdOrFullName == null) {
            resourceIdOrFullName = "";
        }

        if (resourceSegment == null) {
            throw new IllegalArgumentException("resourceSegment");
        }

        if (headers == null) {
            throw new IllegalArgumentException("headers");
        }

        if (StringUtils.isEmpty(this.credential.getKey())) {
            throw new IllegalArgumentException("key credentials cannot be empty");
        }

        if(!PathsHelper.isNameBased(resourceIdOrFullName)) {
            resourceIdOrFullName = resourceIdOrFullName.toLowerCase(Locale.ROOT);
        }

        // Skipping lower casing of resourceId since it may now contain "ID" of the resource as part of the FullName
        StringBuilder body = new StringBuilder();
        body.append(ModelBridgeInternal.toLower(verb))
                .append('\n')
                .append(resourceSegment)
                .append('\n')
                .append(resourceIdOrFullName)
                .append('\n');

        if (headers.containsKey(HttpConstants.HttpHeaders.X_DATE)) {
            body.append(headers.get(HttpConstants.HttpHeaders.X_DATE).toLowerCase(Locale.ROOT));
        }

        body.append('\n');

        if (headers.containsKey(HttpConstants.HttpHeaders.HTTP_DATE)) {
            body.append(headers.get(HttpConstants.HttpHeaders.HTTP_DATE).toLowerCase(Locale.ROOT));
        }

        body.append('\n');

        MacPool.ReUsableMac macInstance = getReUseableMacInstance();

        try {

            byte[] digest = macInstance.get().doFinal(body.toString().getBytes(StandardCharsets.UTF_8));
            String auth = Utils.encodeBase64String(digest);
            return AUTH_PREFIX + auth;
        }
        finally {
            // doFinal already resets for re-use
            macInstance.close();
        }
    }

    /**
     * This API is a helper method to create auth header based on client request using resourceTokens.
     *
     * @param resourceTokens the resource tokens.
     * @param path           the path.
     * @param resourceId     the resource id.
     * @return the authorization token.
     */
    public String getAuthorizationTokenUsingResourceTokens(Map<String, String> resourceTokens,
            String path,
            String resourceId) {
        if (resourceTokens == null) {
            throw new IllegalArgumentException("resourceTokens");
        }

        String resourceToken = null;
        if (resourceTokens.containsKey(resourceId) && resourceTokens.get(resourceId) != null) {
            resourceToken = resourceTokens.get(resourceId);
        } else if (StringUtils.isEmpty(path) || StringUtils.isEmpty(resourceId)) {
            if (resourceTokens.size() > 0) {
                resourceToken = resourceTokens.values().iterator().next();
            }
        } else {
            // Get the last resource id from the path and use that to find the corresponding token.
            String[] pathParts = StringUtils.split(path, "/");
            String[] resourceTypes = {"dbs", "colls", "docs", "sprocs", "udfs", "triggers", "users", "permissions",
                    "attachments", "media", "conflicts"};
            HashSet<String> resourceTypesSet = new HashSet<String>();
            Collections.addAll(resourceTypesSet, resourceTypes);

            for (int i = pathParts.length - 1; i >= 0; --i) {

                if (!resourceTypesSet.contains(pathParts[i]) && resourceTokens.containsKey(pathParts[i])) {
                    resourceToken = resourceTokens.get(pathParts[i]);
                }
            }
        }

        return resourceToken;
    }

    private MacPool.ReUsableMac getReUseableMacInstance() {
        reInitializeIfPossible();

        return macPool.take();
    }

    /*
     * Ensures that this.macInstance is initialized
     * In-case of credential change, optimistically will try to refresh the macInstance
     *
     * Implementation is non-blocking, the one which acquire the lock will try to refresh
     * with new credentials
     *
     * NOTE: Calling it CTOR ensured that default is initialized.
     */
    private void reInitializeIfPossible() {
        // Java == operator is reference equals not content
        // leveraging reference comparison avoid hash computation
        if (this.currentCredentialKey != this.credential.getKey()) {
            // Try to acquire the lock, the one who got lock will try to refresh the macInstance
            boolean lockAcquired = this.macInstanceLock.tryLock();
            if (lockAcquired) {
                try {
                    if (this.currentCredentialKey != this.credential.getKey()) {
                        byte[] masterKeyBytes = this.credential.getKey().getBytes(StandardCharsets.UTF_8);
                        byte[] masterKeyDecodedBytes = Utils.Base64Decoder.decode(masterKeyBytes);
                        SecretKey signingKey = new SecretKeySpec(masterKeyDecodedBytes, "HMACSHA256");
                        try {
                            Mac macInstance = Mac.getInstance("HMACSHA256");
                            macInstance.init(signingKey);

                            this.currentCredentialKey = this.credential.getKey();
                            this.macPool = new MacPool(macInstance);
                        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                } finally {
                    this.macInstanceLock.unlock();
                }
            }
        }
    }
}
