// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.sastokens;

import com.azure.cosmos.models.SasTokenPartitionKeyValueRange;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents a partition key value range to be used when creating a shared access signature token.
 */
public class SasTokenPartitionKeyValueRangeImpl implements SasTokenPartitionKeyValueRange {
    String partitionKeyValue;

    /**
     * Constructs the partition key value range for which to grant access.
     *
     * @param partitionKeyValue the partition key value for which to grant access.
     */
    public SasTokenPartitionKeyValueRangeImpl(String partitionKeyValue) {
        this.partitionKeyValue = partitionKeyValue;
    }

    /**
     * Gets the partition key value.
     *
     * @return the partition key value.
     */
    @Override
    public String getPartitionKey() {
        return this.partitionKeyValue;
    }

    /**
     * Encodes the current partition key value range to be used when generating a shared access signature token.
     *
     *
     * @return a string formatted as "base64_of_partitionKey".
     */
    @Override
    public String encode() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(this.partitionKeyValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a representation of a partition key value range used when generating a shared access signature token.
     *
     * The input string must be in "base64_of_partitionKey" format.
     *
     * @return an instance of SasTokenPartitionKeyValueRange representing the input string.
     */
    public static SasTokenPartitionKeyValueRange decode(String encoding) {
        byte[] decodedString = Base64.getDecoder().decode(encoding);

        return new SasTokenPartitionKeyValueRangeImpl(new String(decodedString, StandardCharsets.UTF_8));
    }
}
