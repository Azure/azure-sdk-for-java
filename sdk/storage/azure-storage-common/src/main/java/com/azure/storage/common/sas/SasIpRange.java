// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

/**
 * This type specifies a continuous range of IP addresses. It is used to limit permissions on SAS tokens. Null may be
 * set if it is not desired to confine the sas permissions to an IP range.
 */
public final class SasIpRange {
    private String ipMin;
    private String ipMax;

    /**
     * Constructs an SasIpRange object.
     */
    public SasIpRange() {
    }

    /**
     * Creates a {@code SasIpRange} from the specified string.
     *
     * @param rangeStr The {@code String} representation of the {@code SasIpRange}.
     * @return The {@code SasIpRange} generated from the {@code String}.
     */
    public static SasIpRange parse(String rangeStr) {
        String[] addrs = rangeStr.split("-");

        SasIpRange range = new SasIpRange().setIpMin(addrs[0]);
        if (addrs.length > 1) {
            range.setIpMax(addrs[1]);
        }

        return range;
    }

    /**
     * @return the minimum IP address of the range
     */
    public String getIpMin() {
        return ipMin;
    }

    /**
     * Sets the minimum IP address of the range.
     *
     * @param ipMin IP address to set as the minimum
     * @return the updated SasIpRange object
     */
    public SasIpRange setIpMin(String ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    /**
     * @return the maximum IP address of the range
     */
    public String getIpMax() {
        return ipMax;
    }

    /**
     * Sets the maximum IP address of the range.
     *
     * @param ipMax IP address to set as the maximum
     * @return the updated SasIpRange object
     */
    public SasIpRange setIpMax(String ipMax) {
        this.ipMax = ipMax;
        return this;
    }

    /**
     * Output the single IP address or range of IP addresses for.
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
