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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyAndResourceTokenPair;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;

/**
 * This class is used internally and act as a helper in authorization of
 * resources from permission feed and its supporting method.
 *
 */
public class ResourceTokenAuthorizationHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResourceTokenAuthorizationHelper.class);

    /**
     * This method help to differentiate between master key and resource token
     * 
     * @param token
     *            ResourceToken provide
     * @return Whether given token is resource token or not
     */
    public static boolean isResourceToken(String token) {
        int typeSeparatorPosition = token.indexOf('&');
        if (typeSeparatorPosition == -1) {
            return false;
        }
        String authType = token.substring(0, typeSeparatorPosition);
        int typeKeyValueSepartorPosition = authType.indexOf('=');
        if (typeKeyValueSepartorPosition == -1 || !authType.substring(0, typeKeyValueSepartorPosition)
                .equalsIgnoreCase(Constants.Properties.AUTH_SCHEMA_TYPE)) {
            return false;
        }

        String authTypeValue = authType.substring(typeKeyValueSepartorPosition + 1);

        return authTypeValue.equalsIgnoreCase(Constants.Properties.RESOURCE_TOKEN);
    }

    /**
     * Private method which will fetch resource token based on partition key and
     * resource address .
     * 
     * @param resourceTokensMap
     * @param resourceAddress
     * @param partitionKey
     * @return
     */
    private static String getResourceToken(Map<String, List<PartitionKeyAndResourceTokenPair>> resourceTokensMap,
            String resourceAddress,
            PartitionKeyInternal partitionKey) {
        List<PartitionKeyAndResourceTokenPair> partitionKeyAndResourceTokenPairs = resourceTokensMap
                .get(resourceAddress);
        if (partitionKeyAndResourceTokenPairs != null) {
            for (PartitionKeyAndResourceTokenPair pair : partitionKeyAndResourceTokenPairs) {
                if (pair.getPartitionKey().contains(partitionKey) || partitionKey.equals(PartitionKeyInternal.Empty)) {
                    return pair.getResourceToken();
                }
            }
        }

        return null;
    }

    /**
     * This method will try to fetch the resource token to access the resource .
     * 
     * @param resourceTokensMap
     *            It contains the resource link and its partition key and resource
     *            token list .
     * @param resourcePath
     *            Resource request link .
     * @param headers
     *            Header information of the request .
     * @param resourceAddress
     *            Resource full name or ID .
     * @param requestVerb
     *            The verb .
     */
    public static String getAuthorizationTokenUsingResourceTokens(
            Map<String, List<PartitionKeyAndResourceTokenPair>> resourceTokensMap,
            String resourcePath,
            String requestVerb,
            String resourceName,
            Map<String, String> headers) {
        PartitionKeyInternal partitionKey = PartitionKeyInternal.Empty;
        String partitionKeyString = headers.get(HttpConstants.HttpHeaders.PARTITION_KEY);
        if (partitionKeyString != null) {
            partitionKey = PartitionKeyInternal.fromJsonString(partitionKeyString);
        }

        if (resourcePath.startsWith(Constants.Properties.PATH_SEPARATOR)) {
            resourcePath = resourcePath.substring(1);
        }

        if (PathsHelper.isNameBased(resourcePath)) {
            String resourceToken = null;
            for (int index = 2; index < ResourceId.MAX_PATH_FRAGMENT; index = index + 2) {
                String resourceParent = PathsHelper.getParentByIndex(resourcePath, index);
                if (resourceParent == null)
                    break;
                resourceToken = getResourceToken(resourceTokensMap, resourceParent, partitionKey);
                if (resourceToken != null)
                    break;
            }

            // Get or Head for collection can be done with any child token
            if (resourceToken == null && PathsHelper.getCollectionPath(resourcePath).equalsIgnoreCase(resourcePath)
                    && HttpConstants.HttpMethods.GET.equalsIgnoreCase(requestVerb)
                    || HttpConstants.HttpMethods.HEAD.equalsIgnoreCase(requestVerb)) {
                String resourceAddressWithSlash = resourcePath.endsWith(Constants.Properties.PATH_SEPARATOR)
                        ? resourcePath
                        : resourcePath + Constants.Properties.PATH_SEPARATOR;
                for (String key : resourceTokensMap.keySet()) {
                    if (key.startsWith(resourceAddressWithSlash)) {
                        if (resourceTokensMap.get(key) != null && resourceTokensMap.get(key).size() > 0)
                            resourceToken = resourceTokensMap.get(key).get(0).getResourceToken();
                        break;
                    }
                }
            }

            if (resourceToken == null) {
                throw new IllegalArgumentException(RMResources.ResourceTokenNotFound);
            }

            logger.debug("returned token for  is  resourcePath [{}] , resourceAddress [{}] = [{}] ", resourcePath,
                    resourceName, resourceToken);
            return resourceToken;
        } else {
            String resourceToken = null;
            ResourceId resourceId = ResourceId.parse(resourceName);
            if (resourceId.getAttachment() != 0 || resourceId.getPermission() != 0
                    || resourceId.getStoredProcedure() != 0 || resourceId.getTrigger() != 0
                    || resourceId.getUserDefinedFunction() != 0) {
                // Use the leaf ID - attachment/permission/sproc/trigger/udf
                resourceToken = getResourceToken(resourceTokensMap, resourceName, partitionKey);
            }

            if (resourceToken == null && (resourceId.getAttachment() != 0 || resourceId.getDocument() != 0)) {
                // Use DocumentID for attachment/document
                resourceToken = getResourceToken(resourceTokensMap, resourceId.getDocumentId().toString(),
                        partitionKey);
            }

            if (resourceToken == null && (resourceId.getAttachment() != 0 || resourceId.getDocument() != 0
                    || resourceId.getStoredProcedure() != 0 || resourceId.getTrigger() != 0
                    || resourceId.getUserDefinedFunction() != 0 || resourceId.getDocumentCollection() != 0)) {
                // Use CollectionID for attachment/document/sproc/trigger/udf/collection
                resourceToken = getResourceToken(resourceTokensMap, resourceId.getDocumentCollectionId().toString(),
                        partitionKey);
            }

            if (resourceToken == null && (resourceId.getPermission() != 0 || resourceId.getUser() != 0)) {
                // Use UserID for permission/user
                resourceToken = getResourceToken(resourceTokensMap, resourceId.getUserId().toString(), partitionKey);
            }

            if (resourceToken == null) {
                // Use DatabaseId if all else fail
                resourceToken = getResourceToken(resourceTokensMap, resourceId.getDatabaseId().toString(),
                        partitionKey);
            }

            if (resourceToken == null && resourceId.getDocument() != 0
                    && (HttpConstants.HttpMethods.GET.equalsIgnoreCase(requestVerb)
                            || HttpConstants.HttpMethods.POST.equalsIgnoreCase(requestVerb))) {
                for (String key : resourceTokensMap.keySet()) {
                    ResourceId tokenRid;
                    Pair<Boolean, ResourceId> pair = ResourceId.tryParse(key);
                    if (!PathsHelper.isNameBased(key) && pair.getLeft()
                            && pair.getRight().getDocumentCollectionId().equals(resourceId)) {
                        if (resourceTokensMap.get(key) != null && resourceTokensMap.get(key).size() > 0) {
                            resourceTokensMap.get(key).get(0).getResourceToken();
                        }
                    }
                }

            }

            if (resourceToken == null) {
                throw new IllegalArgumentException(RMResources.ResourceTokenNotFound);
            }

            logger.debug("returned token for  is  resourcePath [{}] , resourceAddress [{}] = [{}] ", resourcePath,
                    resourceName, resourceToken);
            return resourceToken;
        }
    }
}
