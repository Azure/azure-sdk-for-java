// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for partition key extraction
 */
public class PartitionKeyHelper {

    /**
     * The property name of the item's id field, which is also the last partition key path token
     * ("/id") of a hierarchical partition key that ends with the item id.
     */
    private static final String ID_PARTITION_KEY_PATH = "/id";

    private static ImplementationBridgeHelpers.PartitionKeyHelper.PartitionKeyAccessor partitionKeyAccessor() {
        return ImplementationBridgeHelpers.PartitionKeyHelper.getPartitionKeyAccessor();
    }

    /**
     * Returns {@code true} when the last path of the (hierarchical) partition key definition is
     * "/id". When this is the case a partition key value uniquely maps to a single item and the
     * SDK can append the item id to the partition key on the caller's behalf.
     *
     * @param partitionKeyDefinition the partition key definition of the container (may be null).
     * @return {@code true} if the last partition key path is "/id"; otherwise {@code false}.
     */
    public static boolean isLastPartitionKeyPathId(PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition == null) {
            return false;
        }

        List<String> paths = partitionKeyDefinition.getPaths();
        if (paths == null || paths.isEmpty()) {
            return false;
        }

        return ID_PARTITION_KEY_PATH.equals(paths.get(paths.size() - 1));
    }

    /**
     * Returns {@code true} when the SDK can complete the provided partition key by appending the
     * item id. This is limited to containers whose last partition key path is "/id" and callers
     * that either omitted the partition key or provided exactly its prefix (component count ==
     * pathCount - 1). Other incomplete or malformed partition keys are not eligible for automatic
     * completion. A zero-component prefix for a single-path "/id" definition is represented by an
     * omitted key, so it is available only through APIs that permit callers to omit the partition
     * key.
     *
     * @param partitionKeyDefinition the partition key definition of the container (may be null).
     * @param providedPartitionKey the partition key provided by the caller (may be null).
     * @return {@code true} if the partition key can be completed with the item id.
     */
    public static boolean canCompletePartitionKeyWithId(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKeyInternal providedPartitionKey) {

        if (!isLastPartitionKeyPathId(partitionKeyDefinition)) {
            return false;
        }

        if (providedPartitionKey == null) {
            return true;
        }

        // PartitionKey.NONE is an explicit sentinel, not an omitted partition key.
        if (providedPartitionKey.getComponents() == null) {
            return false;
        }

        int pathCount = partitionKeyDefinition.getPaths().size();
        return providedPartitionKey.getComponents().size() == pathCount - 1;
    }

    /**
     * Returns whether the provided partition key contains one component for every path in the
     * container's partition key definition.
     *
     * @param partitionKeyDefinition the partition key definition of the container.
     * @param providedPartitionKey the partition key provided by the caller.
     * @return {@code true} if the partition key is fully specified; otherwise {@code false}.
     */
    public static boolean isFullPartitionKey(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKeyInternal providedPartitionKey) {

        return partitionKeyDefinition != null
            && partitionKeyDefinition.getPaths() != null
            && providedPartitionKey != null
            && providedPartitionKey.getComponents() != null
            && providedPartitionKey.getComponents().size() == partitionKeyDefinition.getPaths().size();
    }

    /**
     * When the last path of a (hierarchical) partition key definition is "/id", ensures the item's
     * id is part of the partition key so callers can address an item using only the prefix of the
     * partition key (i.e. without repeating the id). For a single-path "/id" definition, the empty
     * prefix is available only through APIs that permit an omitted partition key.
     *
     * <ul>
     *   <li>If the last partition key path is not "/id", the provided partition key is returned unchanged.</li>
     *   <li>If the provided partition key already contains all components (it is fully specified,
     *       including the id), it is returned unchanged.</li>
     *   <li>If the provided partition key is {@link PartitionKey#NONE}, it is returned unchanged.</li>
     *   <li>If the provided partition key is exactly the prefix (component count == pathCount - 1),
     *       the item id is appended.</li>
     *   <li>If no partition key is provided, a partition key of {@code [null * (pathCount - 1), id]}
     *       is built.</li>
     *   <li>If the id cannot be determined and it is required, an {@link IllegalArgumentException}
     *       is thrown.</li>
     * </ul>
     *
     * @param partitionKeyDefinition the partition key definition of the container.
     * @param providedPartitionKey the partition key provided by the caller (may be null).
     * @param itemId the item id (may be null/empty).
     * @return the (possibly augmented) partition key internal.
     */
    public static PartitionKeyInternal completePartitionKeyInternalWithIdIfNeeded(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKeyInternal providedPartitionKey,
        String itemId) {

        // The provided partition key is already complete (or the container does not end in "/id"),
        // so there is nothing to append and it is returned unchanged.
        if (!canCompletePartitionKeyWithId(partitionKeyDefinition, providedPartitionKey)) {
            return providedPartitionKey;
        }

        int pathCount = partitionKeyDefinition.getPaths().size();
        boolean hasProvidedPartitionKey =
            providedPartitionKey != null && providedPartitionKey.getComponents() != null;

        if (Strings.isNullOrEmpty(itemId)) {
            throw new IllegalArgumentException(
                "itemId needs to be specified if the last partition key path is '/id', "
                    + "or add the id value to the partition key paths.");
        }

        List<Object> values = new ArrayList<>();
        if (hasProvidedPartitionKey) {
            values.addAll(Arrays.asList(providedPartitionKey.toObjectArray()));
        } else {
            // No partition key was provided: fill the prefix levels with null values.
            for (int i = 0; i < pathCount - 1; i++) {
                values.add(null);
            }
        }
        values.add(itemId);

        return PartitionKeyInternal.fromObjectArray(values, false);
    }

    /**
     * Completes a {@link PartitionKey} with the item id when eligible. Returns the original
     * {@code providedPartitionKey} instance when no augmentation is needed.
     *
     * @param partitionKeyDefinition the partition key definition of the container.
     * @param providedPartitionKey the partition key provided by the caller (may be null).
     * @param itemId the item id (may be null/empty).
     * @return the (possibly augmented) partition key.
     */
    public static PartitionKey completePartitionKeyWithIdIfNeeded(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKey providedPartitionKey,
        String itemId) {

        PartitionKeyInternal providedInternal = providedPartitionKey == null
            ? null
            : ModelBridgeInternal.getPartitionKeyInternal(providedPartitionKey);

        PartitionKeyInternal result =
            completePartitionKeyInternalWithIdIfNeeded(partitionKeyDefinition, providedInternal, itemId);

        if (result == providedInternal) {
            return providedPartitionKey;
        }

        return partitionKeyAccessor().toPartitionKey(result);
    }

    public static PartitionKeyInternal extractPartitionKeyValueFromDocument(
        JsonSerializable document,
        PartitionKeyDefinition partitionKeyDefinition) {

        PartitionKey partitionKey = extractPartitionKeyFromDocument(document, partitionKeyDefinition);
        return partitionKey == null ? null : ModelBridgeInternal.getPartitionKeyInternal(partitionKey);
    }

    public static PartitionKey extractPartitionKeyFromDocument(
        JsonSerializable document,
        PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition != null) {
            switch (partitionKeyDefinition.getKind()) {
                case HASH:
                    String path = partitionKeyDefinition.getPaths().iterator().next();
                    List<String> parts = PathParser.getPathParts(path);
                    if (parts.size() >= 1) {
                        Object value = document.getObjectByPath(parts);
                        if (value == null || value.getClass() == ObjectNode.class) {
                            value = ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
                        }

                        if (value instanceof PartitionKeyInternal) {
                            return partitionKeyAccessor()
                                .toPartitionKey((PartitionKeyInternal) value);
                        } else {
                            return partitionKeyAccessor()
                                .toPartitionKey(PartitionKeyInternal.fromObjectArray(Collections.singletonList(value), false));
                        }
                    }
                    break;
                case MULTI_HASH:
                    Object[] partitionKeyValues = new Object[partitionKeyDefinition.getPaths().size()];
                    for(int pathIter = 0 ; pathIter < partitionKeyDefinition.getPaths().size(); pathIter++){
                        String partitionPath = partitionKeyDefinition.getPaths().get(pathIter);
                        List<String> partitionPathParts = PathParser.getPathParts(partitionPath);
                        partitionKeyValues[pathIter] = document.getObjectByPath(partitionPathParts);
                    }

                    return partitionKeyAccessor()
                        .toPartitionKey(PartitionKeyInternal.fromObjectArray(partitionKeyValues, false));

                default:
                    throw new IllegalArgumentException("Unrecognized Partition kind: " + partitionKeyDefinition.getKind());
            }
        }

        return null;
    }
}
