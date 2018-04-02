/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

/**
 * This class is used internally by both client (for generating the auth header with master/system key) and by the Gateway when
 * verifying the auth header in the Azure Cosmos DB database service.
 */
public class BaseAuthorizationTokenProvider implements AuthorizationTokenProvider {

    private final String masterKey;
    private final Mac macInstance;

    public BaseAuthorizationTokenProvider(String masterKey) {
        this.masterKey = masterKey;
        byte[] masterKeyDecodedBytes = Utils.Base64Decoder.decode(this.masterKey.getBytes());
        SecretKey signingKey = new SecretKeySpec(masterKeyDecodedBytes, "HMACSHA256");
        try {
            this.macInstance = Mac.getInstance("HMACSHA256");
            this.macInstance.init(signingKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getResourceSegement(ResourceType resourceType) {
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
            return Paths.PARTITION_KEY_RANGE_PATH_SEGMENT;
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
    public String generateKeyAuthorizationSignature(String verb,
            String resourceIdOrFullName,
            ResourceType resourceType,
            Map<String, String> headers) {
        return this.generateKeyAuthorizationSignature(verb, resourceIdOrFullName,
                BaseAuthorizationTokenProvider.getResourceSegement(resourceType).toLowerCase(), headers);
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
    public String generateKeyAuthorizationSignature(String verb,
            String resourceIdOrFullName,
            String resourceSegment,
            Map<String, String> headers) {
        if (verb == null || verb.isEmpty()) {
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

        if (this.masterKey == null || this.masterKey.isEmpty()) {
            throw new IllegalArgumentException("masterKey");
        }

        if(!PathsHelper.isNameBased(resourceIdOrFullName)) {
            resourceIdOrFullName = resourceIdOrFullName.toLowerCase(Locale.ROOT);
        }

        // Skipping lower casing of resourceId since it may now contain "ID" of the resource as part of the FullName
        String body = String.format("%s\n%s\n%s\n",
                verb.toLowerCase(),
                resourceSegment,
                resourceIdOrFullName);

        if (headers.containsKey(HttpConstants.HttpHeaders.X_DATE)) {
            body += headers.get(HttpConstants.HttpHeaders.X_DATE).toLowerCase();
        }

        body += '\n';

        if (headers.containsKey(HttpConstants.HttpHeaders.HTTP_DATE)) {
            body += headers.get(HttpConstants.HttpHeaders.HTTP_DATE).toLowerCase();
        }

        body += '\n';

        Mac mac = null;
        try {
            mac = (Mac) this.macInstance.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }

        byte[] digest = mac.doFinal(body.getBytes());

        String auth = Utils.encodeBase64String(digest);

        String authtoken = "type=master&ver=1.0&sig=" + auth;

        return authtoken;
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
            String[] pathParts = path.split("/");
            String[] resourceTypes = {"dbs", "colls", "docs", "sprocs", "udfs", "triggers", "users", "permissions",
                    "attachments", "media", "conflicts"};
            HashSet<String> resourceTypesSet = new HashSet<String>();
            for (String resourceType : resourceTypes) {
                resourceTypesSet.add(resourceType);
            }

            for (int i = pathParts.length - 1; i >= 0; --i) {

                if (!resourceTypesSet.contains(pathParts[i]) && resourceTokens.containsKey(pathParts[i])) {
                    resourceToken = resourceTokens.get(pathParts[i]);
                }
            }
        }

        return resourceToken;
    }
}
