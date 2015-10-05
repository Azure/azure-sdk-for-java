/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

import java.net.Inet4Address;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * A continuous range of IP addresses.
 */
public final class IPRange {
    private String ipMin;
    private String ipMax;

    /**
     * Creates an IP Range using the specified single IP address. The IP address must be IPv4.
     * 
     * @param ip
     *            the single IP address
     */
    public IPRange(String ip) {
        Utility.assertNotNull("ip", ip);
        IPRange.validateIPAddress(ip);
        
        this.ipMin = ip;
        this.ipMax = ip;
    }
    
    /**
     * Creates an IP Range using the specified minimum and maximum IP addresses. The IP addresses must be IPv4.
     * 
     * @param mininimumIP
     *            the minimum IP address of the range
     * @param maximumIP
     *            the maximum IP address of the range
     */
    public IPRange(String mininimumIP, String maximumIP) {
        Utility.assertNotNull("mininimumIP", mininimumIP);
        Utility.assertNotNull("maximumIP", maximumIP);
        
        IPRange.validateIPAddress(mininimumIP);
        IPRange.validateIPAddress(maximumIP);
        
        this.ipMin = mininimumIP;
        this.ipMax = maximumIP;
    }

    /**
     * The minimum IP address for the range, inclusive.
     * Will match {@link #getIpMax()} if this <code>IPRange</code> represents a single IP address.
     * 
     * @return The minimum IP address
     */
    public String getIpMin() {
        return this.ipMin;
    }

    /**
     * The maximum IP address for the range, inclusive.
     * Will match {@link #getIpMin()} if this <code>IPRange</code> represents a single IP address.
     * 
     * @return The maximum IP address
     */
    public String getIpMax() {
        return this.ipMax;
    }

    /**
     * Output the single IP address or range of IP addresses.
     * 
     * @return the single IP address or range of IP addresses formated as a <code>String</code>
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.ipMin);
        if (!this.ipMin.equals(this.ipMax)) {
            str.append("-");
            str.append(this.ipMax);
        }
        
        return str.toString();
    }
    
    /**
     * Validate that the IP address is IPv4.
     * 
     * @param ipAddress
     *              the IP address to validate
     */
    private static void validateIPAddress(String ipAddress) {
        try {
            @SuppressWarnings("unused")
            Inet4Address address = (Inet4Address) Inet4Address.getByName(ipAddress);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(String.format(SR.INVALID_IP_ADDRESS, ipAddress), ex);
        }
    }
}
