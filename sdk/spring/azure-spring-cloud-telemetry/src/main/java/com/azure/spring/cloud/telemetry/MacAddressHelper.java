// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.telemetry;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Helper class to get the MAC address.
 */
public class MacAddressHelper {

    private static final String UNKNOWN_MAC = "Unknown-Mac-Address";

    private static final String HASHED_MAC_ADDRESS = computeHashedMacAddress();

    private static String computeHashedMacAddress() {
        try {
            InetAddress host = getLocalHostLANAddress();
            byte[] macBytes = NetworkInterface.getByInetAddress(host).getHardwareAddress();

            if (macBytes == null) {
                return UNKNOWN_MAC;
            }
            return DigestUtils.sha256Hex(macBytes);
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            return UNKNOWN_MAC;
        }
    }

    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> ifaces;
            // Solve problem: can not get NetworkInterface.
            // Refs: https://github.com/Azure/azure-sdk-for-java/issues/17811
            for (ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {    //exclude loopback type addresses
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }


    public static String getHashedMacAddress() {
        return HASHED_MAC_ADDRESS;
    }
}
