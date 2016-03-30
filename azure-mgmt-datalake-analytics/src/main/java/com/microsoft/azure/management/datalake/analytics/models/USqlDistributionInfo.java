/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import java.util.List;

/**
 * A Data Lake Analytics catalog U-SQL distribution information object.
 */
public class USqlDistributionInfo {
    /**
     * Gets or sets the type of this distribution.
     */
    private Integer type;

    /**
     * Gets or sets the list of directed columns in the distribution.
     */
    private List<USqlDirectedColumn> keys;

    /**
     * Gets or sets the count of indices using this distribution.
     */
    private Integer count;

    /**
     * Gets or sets the dynamic count of indices using this distribution.
     */
    private Integer dynamicCount;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public Integer getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * Get the keys value.
     *
     * @return the keys value
     */
    public List<USqlDirectedColumn> getKeys() {
        return this.keys;
    }

    /**
     * Set the keys value.
     *
     * @param keys the keys value to set
     */
    public void setKeys(List<USqlDirectedColumn> keys) {
        this.keys = keys;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer getCount() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Get the dynamicCount value.
     *
     * @return the dynamicCount value
     */
    public Integer getDynamicCount() {
        return this.dynamicCount;
    }

    /**
     * Set the dynamicCount value.
     *
     * @param dynamicCount the dynamicCount value to set
     */
    public void setDynamicCount(Integer dynamicCount) {
        this.dynamicCount = dynamicCount;
    }

}
