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
     * When the last path of a (hierarchical) partition key definition is "/id", ensures the item's
     * id is part of the partition key so callers can address an item using only the prefix of the
     * partition key (i.e. without repeating the id).
     *
     * <p>The behaviour mirrors the .NET SDK:</p>
     * <ul>
     *   <li>If the last partition key path is not "/id", the provided partition key is returned unchanged.</li>
     *   <li>If the provided partition key already contains all components (it is fully specified,
     *       including the id), it is returned unchanged.</li>
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
    public static PartitionKeyInternal ensureIdIsInPartitionKeyInternal(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKeyInternal providedPartitionKey,
        String itemId) {

        if (!isLastPartitionKeyPathId(partitionKeyDefinition)) {
            return providedPartitionKey;
        }

        int pathCount = partitionKeyDefinition.getPaths().size();

        boolean hasProvidedPartitionKey =
            providedPartitionKey != null && providedPartitionKey.getComponents() != null;
        int existingComponentCount =
            hasProvidedPartitionKey ? providedPartitionKey.getComponents().size() : 0;

        // Already fully specified (the id is part of the partition key) -> nothing to do.
        if (hasProvidedPartitionKey && existingComponentCount == pathCount) {
            return providedPartitionKey;
        }

        // A partition key is provided but it is not exactly the prefix of the partition key
        // (pathCount - 1). Leave it untouched and let the server validate it.
        if (hasProvidedPartitionKey && existingComponentCount != pathCount - 1) {
            return providedPartitionKey;
        }

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
     * {@link PartitionKey} overload of {@link #ensureIdIsInPartitionKeyInternal}. Returns the
     * original {@code providedPartitionKey} instance when no augmentation is needed.
     *
     * @param partitionKeyDefinition the partition key definition of the container.
     * @param providedPartitionKey the partition key provided by the caller (may be null).
     * @param itemId the item id (may be null/empty).
     * @return the (possibly augmented) partition key.
     */
    public static PartitionKey ensureIdIsInPartitionKey(
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKey providedPartitionKey,
        String itemId) {

        if (!isLastPartitionKeyPathId(partitionKeyDefinition)) {
            return providedPartitionKey;
        }

        PartitionKeyInternal providedInternal = providedPartitionKey == null
            ? null
            : ModelBridgeInternal.getPartitionKeyInternal(providedPartitionKey);

        PartitionKeyInternal result =
            ensureIdIsInPartitionKeyInternal(partitionKeyDefinition, providedInternal, itemId);

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
