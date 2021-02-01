// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.sastokens.SasTokenPartitionKeyValueRangeImpl;
import com.azure.cosmos.util.Beta;

/**
 * Represents a partition key value range to be used when creating a shared access signature token.
 */
@Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface SasTokenPartitionKeyValueRange {

    /**
     * Creates a {@link SasTokenPartitionKeyValueRange} representing the partition key value for which to grant access.
     *
     * @param partitionKeyValue the partition key value for which to grant access.
     * @return a {@link SasTokenPartitionKeyValueRange} representing the partition key value for which to grant access.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    static SasTokenPartitionKeyValueRange create(String partitionKeyValue) {
        return new SasTokenPartitionKeyValueRangeImpl(partitionKeyValue);
    }

    /**
     * Gets the partition key value.
     *
     * @return the partition key value.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getPartitionKey();

    /**
     * Encodes the current partition key value range to be used when generating a shared access signature token.
     *
     * @return a string formatted as "base64_of_partitionKey".
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String encode();

    /**
     * Decodes a representation of a partition key value range used when generating a shared access signature token.
     *
     * The input string must be in "base64_of_partitionKey" format.
     *
     * @param encoding the encoded input string.
     * @return an instance of SasTokenPartitionKeyValueRange representing the input string.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    static SasTokenPartitionKeyValueRange decode(String encoding) {
        return SasTokenPartitionKeyValueRangeImpl.decode(encoding);
    }
}
