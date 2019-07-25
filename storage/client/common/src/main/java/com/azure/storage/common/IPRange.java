// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * This type specifies a continuous range of IP addresses. It is used to limit permissions on SAS tokens. Null may be
 * set if it is not desired to confine the sas permissions to an IP range.
 */
public final class IPRange {
    private String ipMin;
    private String ipMax;

    private IPRange() {
    }

    /**
     * Creates a {@code IPRange} from the specified string.
     *
     * @param rangeStr The {@code String} representation of the {@code IPRange}.
     * @return The {@code IPRange} generated from the {@code String}.
     */
    public static IPRange parse(String rangeStr) {
        String[] addrs = rangeStr.split("-");

        IPRange range = new IPRange().ipMin(addrs[0]);
        if (addrs.length > 1) {
            range.ipMax(addrs[1]);
        }

        return range;
    }

    /**
     * The minimum IP address of the range.
     */
    public String ipMin() {
        return ipMin;
    }

    /**
     * The minimum IP address of the range.
     */
    public IPRange ipMin(String ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    /**
     * The maximum IP address of the range.
     */
    public String ipMax() {
        return ipMax;
    }

    /**
     * The maximum IP address of the range.
     */
    public IPRange ipMax(String ipMax) {
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
