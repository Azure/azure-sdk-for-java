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

package com.azure.data.cosmos;

public class CommonsBridgeInternal {
    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.version() != null && PartitionKeyDefinitionVersion.V2.val == pkd.version().val;
    }

    public static void setV2(PartitionKeyDefinition pkd) {
        pkd.version(PartitionKeyDefinitionVersion.V2);
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(FeedOptions options) {
        return options.partitionKeyRangeIdInternal();
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(ChangeFeedOptions options) {
        return options.partitionKeyRangeId();
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static FeedOptions partitionKeyRangeIdInternal(FeedOptions options, String partitionKeyRangeId) {
        return options.partitionKeyRangeIdInternal(partitionKeyRangeId);
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static ChangeFeedOptions partitionKeyRangeIdInternal(ChangeFeedOptions options, String partitionKeyRangeId) {
        return options.partitionKeyRangeId(partitionKeyRangeId);
    }
}
