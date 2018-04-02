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

package com.microsoft.azure.cosmosdb;

import java.util.Collection;

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.routing.Range;

/**
 * Represent a partition key range in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class PartitionKeyRange extends Resource {
    public static final String MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY = "";
    public static final String MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY = "FF";
    public static final String MASTER_PARTITION_KEY_RANGE_ID = "M";

    /**
     * Initialize a partition key range object.
     */
    public PartitionKeyRange() {
        super();
    }

    /**
     * Initialize a partition key range object from json string.
     * 
     * @param jsonString
     *            the json string that represents the partition key range
     *            object.
     */
    public PartitionKeyRange(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize a partition key range object from json object.
     * 
     * @param jsonObject
     *            the json object that represents the partition key range
     *            object.
     */
    public PartitionKeyRange(JSONObject jsonObject) {
        super(jsonObject);
    }

    public PartitionKeyRange(String id, String minInclusive, String maxExclusive) {
        super();
        this.setId(id);
        this.setMinInclusive(minInclusive);
        this.setMaxExclusive(maxExclusive);
    }

    public String getMinInclusive() {
        return super.getString("minInclusive");
    }

    public void setMinInclusive(String minInclusive) {
        super.set("minInclusive", minInclusive);
    }

    public String getMaxExclusive() {
        return super.getString("maxExclusive");
    }

    public void setMaxExclusive(String maxExclusive) {
        super.set("maxExclusive", maxExclusive);
    }

    public Range<String> toRange() {
        return new Range<String>(this.getMinInclusive(), this.getMaxExclusive(), true, false);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartitionKeyRange)) {
            return false;
        }

        PartitionKeyRange otherRange = (PartitionKeyRange) obj;

        return this.getId().compareTo(otherRange.getId()) == 0
                && this.getMinInclusive().compareTo(otherRange.getMinInclusive()) == 0
                && this.getMaxExclusive().compareTo(otherRange.getMaxExclusive()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ this.getId().hashCode();
        hash = (hash * 397) ^ this.getMinInclusive().hashCode();
        hash = (hash * 397) ^ this.getMaxExclusive().hashCode();
        return hash;
    }

    void setParents(Collection<String> parents) {
        this.set(Constants.Properties.PARENTS, parents);
    }

    /**
     * Used internally to indicate the ID of the parent range
     * @return a list partition key range ID
     */
    public Collection<String> getParents() { return this.getCollection(Constants.Properties.PARENTS, String.class); }
}
