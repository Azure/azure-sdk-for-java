// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describes a Data Feed Metric dimension name-value pairs.
 * <p>
 * A metric advisor data feed schema {@link DataFeedSchema} defines dimension
 * names and metric for a data source, For example, a SQL data source for a
 * wholesale company can have "product_category" and "city" as dimension names
 * and "revenue" as a metric.
 * </p>
 * <p>
 * A combination of value for all dimension names identifies a metric time-series.
 * A {@link DimensionKey} can hold such a combination, for example,
 * [ product_category=men-shoes, city=redmond ] identifies one specific
 * time-series.
 * </p>
 * <p>
 * A {@link DimensionKey} can also have partial dimensions, for example,
 * [ city=redmond ] identifying a group of time-series having value for "city"
 * dimension as "redmond".
 * </p>
 * <p>
 * Two {@link DimensionKey} with same dimension name-value pairs identifies
 * the same time-series or time-series group, this means {@link DimensionKey#equals(Object)}
 * return {@code true}.
 * </p>
 *
 *
 */
public final class DimensionKey {
    private final Map<String, String> dimensions;

    /**
     * Creates a new instance of DimensionKey.
     */
    public DimensionKey() {
        this.dimensions =  new HashMap<>();
    }

    /**
     * Creates a new instance of DimensionKey that is initialized with
     * the provided map of dimension name-value pairs.
     *
     * @param dimensionNameValue The map of dimension name values.
     */
    public DimensionKey(Map<String, String> dimensionNameValue) {
        this.dimensions =  new HashMap<>(dimensionNameValue);
    }

    /**
     * Adds a dimension name-value to the key.
     *
     * @param dimensionName The dimension name.
     * @param dimensionValue The dimension value.
     * @return The DimensionKey object itself.
     */
    public DimensionKey put(String dimensionName, String dimensionValue) {
        this.dimensions.put(dimensionName, dimensionValue);
        return this;
    }

    /**
     * Gets dimension value for the given {@code dimensionName}.
     *
     * @param dimensionName The dimension name.
     * @return The dimension value if exists, {@code null} otherwise.
     */
    public String get(String dimensionName) {
        return this.dimensions.get(dimensionName);
    }

    /**
     * Removes a dimension name-value from the key.
     *
     * @param dimensionName The name of the dimension to remove.
     * @return The DimensionKey object itself.
     */
    public DimensionKey remove(String dimensionName) {
        this.dimensions.remove(dimensionName);
        return this;
    }

    /**
     * Gets the dimension name-value pairs in the key as a map.
     *
     * @return The dimension name-value map.
     */
    public Map<String, String> asMap() {
        return new HashMap<>(this.dimensions);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DimensionKey)) {
            return false;
        }

        DimensionKey otherDimensionKey = (DimensionKey) o;
        if (this.dimensions.size() != otherDimensionKey.dimensions.size()) {
            return false;
        }

        // Sort both map by dimension-names - log(n):
        final Iterator<Map.Entry<String, String>> itr1
            = (new TreeMap<>(this.dimensions)).entrySet().iterator();
        final Iterator<Map.Entry<String, String>> itr2
            = (new TreeMap<>(otherDimensionKey.dimensions)).entrySet().iterator();
        // Compare - O(n):
        while (itr1.hasNext()) {
            Map.Entry<String, String> entry1 = itr1.next();
            Map.Entry<String, String> entry2 = itr2.next();
            if (!entry1.getKey().equals(entry2.getKey())
                || !entry1.getValue().equals(entry2.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        Iterator<Map.Entry<String, String>> itr = this.dimensions.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String key = entry.getKey();
            String value = entry.getValue();
            int entryHashCode = (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
            hashCode += entryHashCode;
        }
        return hashCode;
    }
}
