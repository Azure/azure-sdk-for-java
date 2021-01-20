// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ReplicatedResourceClientUtils {

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
        if (resourceType == ResourceType.Offer ||
                resourceType == ResourceType.Database ||
                resourceType == ResourceType.User ||
                resourceType == ResourceType.UserDefinedType ||
                resourceType == ResourceType.Permission ||
                resourceType == ResourceType.Topology ||
                resourceType == ResourceType.DatabaseAccount ||
                resourceType == ResourceType.PartitionKeyRange ||
                resourceType == ResourceType.DocumentCollection ||
                resourceType == ResourceType.Trigger ||
                resourceType == ResourceType.UserDefinedFunction) {
            return true;
        }

        return false;
    }

    public static boolean isMasterOperation(ResourceType resourceType, OperationType operationType) {
        // Stored procedures, trigger, and user defined functions CRUD operations are done on
        // master so they do not require the session token. Stored procedures execute is not a master operation
        return isMasterResource(resourceType) ||
            isStoredProcedureMasterOperation(resourceType, operationType) ||
            operationType == OperationType.QueryPlan;
    }

    private static boolean isStoredProcedureMasterOperation(ResourceType resourceType, OperationType operationType) {
        return resourceType == ResourceType.StoredProcedure && operationType != OperationType.ExecuteJavaScript;
    }
}
