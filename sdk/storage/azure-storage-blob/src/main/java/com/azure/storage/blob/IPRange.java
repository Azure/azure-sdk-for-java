// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

/**
 * This type specifies a continuous range of IP addresses. It is used to limit permissions on SAS tokens. Null may be
 * set if it is not desired to confine the sas permissions to an IP range. Please refer to
 * {@link AccountSASSignatureValues} or {@link ServiceSASSignatureValues} for more information.
 */
public final class IPRange {

    private String ipMin;

    private String ipMax;

    /**
     * Constructs an empty IPRange.
     */
    public IPRange() {
    }

    /**
     * Creates a {@code IPRange} from the specified string.
     *
     * @param rangeStr
     *         The {@code String} representation of the {@code IPRange}.
     *
     * @return The {@code IPRange} generated from the {@code String}.
     */
    public static IPRange parse(String rangeStr) {
        String[] addrs = rangeStr.split("-");
        IPRange range = new IPRange();
        range.ipMin = addrs[0];
        if (addrs.length > 1) {
            range.ipMax = addrs[1];
        }
        return range;
    }

    /**
     * @return the minimum IP address of the range.
     */
    public String ipMin() {
        return ipMin;
    }

    /**
     * Sets the minimum IP address of the range.
     *
     * @param ipMin Minimum IP of the range
     * @return the updated IPRange object
     */
    public IPRange ipMin(String ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    /**
     *
     * @return the maximum IP address of the range.
     */
    public String ipMax() {
        return ipMax;
    }

    /**
     * Sets the maximum IP address of the range.
     *
     * @param ipMax Maximum IP of the range
     * @return the updated IPRange object
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
        }
        this.ipMax = this.ipMax == null ? this.ipMin : this.ipMax;
        StringBuilder str = new StringBuilder(this.ipMin);
        if (!this.ipMin.equals(this.ipMax)) {
            str.append('-');
            str.append(this.ipMax);
        }

        return str.toString();
    }
}
