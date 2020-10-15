// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.telemetry;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.codec.digest.DigestUtils;

public class MacAddressHelper {

    private static final String UNKNOWN_MAC = "Unknown-Mac-Address";

    private static final String HASHED_MAC_ADDRESS = computeHashedMacAddress();

    private static String computeHashedMacAddress() {
        try {
            InetAddress host = InetAddress.getLocalHost();
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

    public static String getHashedMacAddress() {
        return HASHED_MAC_ADDRESS;
    }
}
