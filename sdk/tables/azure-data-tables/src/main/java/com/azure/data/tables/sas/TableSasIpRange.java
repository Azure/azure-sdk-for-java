// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;

/**
 * This type specifies a continuous range of IP addresses. It is used to limit permissions on SAS tokens. Null may be
 * set if it is not desired to confine the sas permissions to an IP range.
 */
@Fluent
public final class TableSasIpRange {
    private String ipMin;
    private String ipMax;

    /**
     * Creates a {@link TableSasIpRange} from the specified string.
     *
     * @param rangeStr The {@code String} representation of the {@link TableSasIpRange}.
     * @return The {@link TableSasIpRange} generated from the {@code String}.
     */
    public static TableSasIpRange parse(String rangeStr) {
        String[] addrs = rangeStr.split("-");

        TableSasIpRange range = new TableSasIpRange().setIpMin(addrs[0]);

        if (addrs.length > 1) {
            range.setIpMax(addrs[1]);
        }

        return range;
    }

    /**
     * @return The minimum IP address of the range.
     */
    public String getIpMin() {
        return ipMin;
    }

    /**
     * Sets the minimum IP address of the range.
     *
     * @param ipMin IP address to set as the minimum.
     * @return The updated {@link TableSasIpRange} object.
     */
    public TableSasIpRange setIpMin(String ipMin) {
        this.ipMin = ipMin;

        return this;
    }

    /**
     * @return The maximum IP address of the range.
     */
    public String getIpMax() {
        return ipMax;
    }

    /**
     * Sets the maximum IP address of the range.
     *
     * @param ipMax IP address to set as the maximum.
     * @return The updated {@link TableSasIpRange} object.
     */
    public TableSasIpRange setIpMax(String ipMax) {
        this.ipMax = ipMax;

        return this;
    }

    /**
     * Output the single IP address or range of IP addresses formatted as a {@code String}. If {@code minIpRange} is set
     * to {@code null}, an empty string is returned from this method. Otherwise, if {@code maxIpRange} is set
     * to {@code null}, then this method returns the value of {@code minIpRange}.
     *
     * @return The single IP address or range of IP addresses formatted as a {@code String}.
     */
    @Override
    public String toString() {
        if (this.ipMin == null) {
            return "";
        } else if (this.ipMax == null) {
            return this.ipMin;
        } else {
            return this.ipMin + "-" + this.ipMax;
        }
    }
}
