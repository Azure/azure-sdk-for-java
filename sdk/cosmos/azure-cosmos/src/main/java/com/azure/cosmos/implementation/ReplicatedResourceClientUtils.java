// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.util.HashSet;
import java.util.Set;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ReplicatedResourceClientUtils {

    private static final Set<ResourceType> masterResourceSet = new HashSet<>(){
        {
            add(ResourceType.Offer);
            add(ResourceType.Database);
            add(ResourceType.User);
            add(ResourceType.UserDefinedType);
            add(ResourceType.Permission);
            add(ResourceType.Topology);
            add(ResourceType.DatabaseAccount);
            add(ResourceType.PartitionKeyRange);
            add(ResourceType.DocumentCollection);
            add(ResourceType.Trigger);
            add(ResourceType.UserDefinedFunction);
            add(ResourceType.ClientEncryptionKey);
        }
    };

    public static boolean isReadingFromMaster(ResourceType resourceType, OperationType operationType) {
        if (resourceType == ResourceType.Offer ||
                resourceType == ResourceType.Database ||
                resourceType == ResourceType.User ||
                resourceType == ResourceType.UserDefinedType ||
                resourceType == ResourceType.Permission ||
                resourceType == ResourceType.Topology ||
                resourceType == ResourceType.DatabaseAccount ||
                (resourceType == ResourceType.PartitionKeyRange && operationType != OperationType.GetSplitPoint && operationType != OperationType.AbortSplit) ||
                (resourceType == ResourceType.DocumentCollection && (operationType == OperationType.ReadFeed || operationType == OperationType.Query || operationType == OperationType.SqlQuery)))
        {
            return true;
        }

        return false;
    }

    public static boolean isMasterResource(ResourceType resourceType) {
        return masterResourceSet.contains(resourceType);
    }
}
