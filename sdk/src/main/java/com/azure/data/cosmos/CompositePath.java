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

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a composite path of the IndexingPolicy in the Azure Cosmos DB database service.
 *  A composite path is used in a composite index. For example if you want to run a query like
 *  "SELECT * FROM c ORDER BY c.age, c.height", then you need to add "/age" and "/height" 
 *  as composite paths to your composite index.
 */
public class CompositePath extends JsonSerializable {
    /**
     * Constructor.
     */
    public CompositePath() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    public CompositePath(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String path() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the CompositePath.
     */
    public CompositePath path(String path) {
        super.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the sort order for the composite path.
     *
     * For example if you want to run the query "SELECT * FROM c ORDER BY c.age asc, c.height desc",
     * then you need to make the order for "/age" "ascending" and the order for "/height" "descending".
     * 
     * @return the sort order.
     */
    public CompositePathSortOrder order() {
        String strValue = super.getString(Constants.Properties.ORDER);
        if (!StringUtils.isEmpty(strValue)) {
            try {
                return CompositePathSortOrder.valueOf(StringUtils.upperCase(super.getString(Constants.Properties.ORDER)));
            } catch (IllegalArgumentException e) {
                this.getLogger().warn("INVALID indexingMode value {}.", super.getString(Constants.Properties.ORDER));
                return CompositePathSortOrder.ASCENDING;
            }
        }
        return CompositePathSortOrder.ASCENDING;
    }

    /**
     * Gets the sort order for the composite path.
     *
     * For example if you want to run the query "SELECT * FROM c ORDER BY c.age asc, c.height desc",
     * then you need to make the order for "/age" "ascending" and the order for "/height" "descending".
     * 
     * @param order the sort order.
     * @return the CompositePath.
     */
    public CompositePath order(CompositePathSortOrder order) {
        super.set(Constants.Properties.ORDER, order.toString());
        return this;
    }
}
