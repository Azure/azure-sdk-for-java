// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.routing.PartitionKeyAndResourceTokenPair;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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
     * @param headers
     *            Header information of the request .
     * @param resourceAddress
     *            Resource full name or ID .
     * @param requestVerb
     *            The verb .
     */
    public static String getAuthorizationTokenUsingResourceTokens(
            Map<String, List<PartitionKeyAndResourceTokenPair>> resourceTokensMap,
            String requestVerb,
            String resourceAddress,
            Map<String, String> headers) {
        PartitionKeyInternal partitionKey = PartitionKeyInternal.Empty;
        String partitionKeyString = headers.get(HttpConstants.HttpHeaders.PARTITION_KEY);
        if (partitionKeyString != null) {
            partitionKey = PartitionKeyInternal.fromJsonString(partitionKeyString);
        }

        if (PathsHelper.isNameBased(resourceAddress)) {
            String resourceToken = null;
            for (int index = 2; index < ResourceId.MAX_PATH_FRAGMENT; index = index + 2) {
                String resourceParent = PathsHelper.getParentByIndex(resourceAddress, index);
                if (resourceParent == null)
                    break;
                resourceToken = getResourceToken(resourceTokensMap, resourceParent, partitionKey);
                if (resourceToken != null)
                    break;
            }

            // Get or Head for collection can be done with any child token
            if (resourceToken == null && PathsHelper.getCollectionPath(resourceAddress).equalsIgnoreCase(resourceAddress)
                    && HttpConstants.HttpMethods.GET.equalsIgnoreCase(requestVerb)
                    || HttpConstants.HttpMethods.HEAD.equalsIgnoreCase(requestVerb)) {
                String resourceAddressWithSlash = resourceAddress.endsWith(Constants.Properties.PATH_SEPARATOR)
                        ? resourceAddress
                        : resourceAddress + Constants.Properties.PATH_SEPARATOR;
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

            logger.debug("returned token for resourceAddress [{}] = [{}]",
                    resourceAddress, resourceToken);
            return resourceToken;
        } else {
            String resourceToken = null;
            ResourceId resourceId = ResourceId.parse(resourceAddress);
            if (resourceId.getAttachment() != 0 || resourceId.getPermission() != 0
                    || resourceId.getStoredProcedure() != 0 || resourceId.getTrigger() != 0
                    || resourceId.getUserDefinedFunction() != 0) {
                // Use the leaf ID - attachment/permission/sproc/trigger/udf
                resourceToken = getResourceToken(resourceTokensMap, resourceAddress, partitionKey);
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
            // Get or Head for collection can be done with any child token
            if (resourceToken == null && resourceId.getDocumentCollection() != 0
                    && (HttpConstants.HttpMethods.GET.equalsIgnoreCase(requestVerb)
                            || HttpConstants.HttpMethods.HEAD.equalsIgnoreCase(requestVerb))) {
                for (String key : resourceTokensMap.keySet()) {
                    ResourceId tokenRid;
                    Pair<Boolean, ResourceId> pair = ResourceId.tryParse(key);
                    ResourceId test1= pair.getRight().getDocumentCollectionId();
                    boolean test = test1.equals(resourceId);
                    if (!PathsHelper.isNameBased(key) && pair.getLeft()
                            && pair.getRight().getDocumentCollectionId().equals(resourceId)) {
                        if (resourceTokensMap.get(key) != null && resourceTokensMap.get(key).size() > 0) {
                            resourceToken = resourceTokensMap.get(key).get(0).getResourceToken();
                        }
                    }
                }

            }

            if (resourceToken == null) {
                throw new IllegalArgumentException(RMResources.ResourceTokenNotFound);
            }

            logger.debug("returned token for resourceAddress [{}] = [{}]",
                    resourceAddress, resourceToken);
            return resourceToken;
        }
    }
}
