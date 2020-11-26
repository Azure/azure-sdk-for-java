// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
/*
 * Disclaimer:
 *      This class is copied from https://github.com/Microsoft/azure-tools-for-java/ with minor modification (fixing
 *      static analysis error).
 *      Location in the repo: /Utils/azuretools-core/src/com/microsoft/azuretools/azurecommons/util/GetHashMac.java
 */

package com.azure.spring.cloud.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the class to get HashMac
 */
public final class GetHashMac {
    public static final String MAC_REGEX = "([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}";
    public static final String MAC_REGEX_ZERO = "([0]{2}[:-]){5}[0]{2}";
    public static final String HASHED_MAC_REGEX = "[0-9a-f]{64}";
    private static final String UNKNOWN_MAC = "Unknown-Mac-Address";

    private GetHashMac() {
        super();
    }

    public static boolean isValidHashMacFormat(String hashMac) {
        if (hashMac == null || hashMac.isEmpty()) {
            return false;
        }

        final Pattern hashedMacPattern = Pattern.compile(HASHED_MAC_REGEX);
        final Matcher matcher = hashedMacPattern.matcher(hashMac);
        return matcher.matches();
    }

    public static String getHashMac() {
        final String rawMac = getRawMac();
        if (rawMac == null || rawMac.isEmpty()) {
            return UNKNOWN_MAC;
        }

        final Pattern pattern = Pattern.compile(MAC_REGEX);
        final Pattern patternZero = Pattern.compile(MAC_REGEX_ZERO);
        final Matcher matcher = pattern.matcher(rawMac);
        String mac = "";
        while (matcher.find()) {
            mac = matcher.group(0);
            if (!patternZero.matcher(mac).matches()) {
                break;
            }
        }

        return hash(mac);
    }

    private static String getRawMac() {
        final StringBuilder ret = new StringBuilder();

        final String os = System.getProperty("os.name");
        String[] command = {"ifconfig", "-a"};
        if (os != null && !os.isEmpty() && os.toLowerCase(Locale.US).startsWith("win")) {
            command = new String[]{"getmac"};
        }

        try {
            final ProcessBuilder builder = new ProcessBuilder(command);
            final Process process = builder.start();

            try (InputStream inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(inputStreamReader)) {
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    ret.append(tmp);
                }
            }
        } catch (IOException e) {
            return null;
        }

        return ret.toString();
    }

    public static String hash(String mac) {
        if (mac == null || mac.isEmpty()) {
            return null;
        }

        final String ret;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = mac.getBytes(StandardCharsets.UTF_8);
            md.update(bytes);
            final byte[] bytesAfterDigest = md.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytesAfterDigest.length; i++) {
                sb.append(Integer.toString((bytesAfterDigest[i] & 0xff) + 0x100, 16).substring(1));
            }

            ret = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }

        return ret;
    }
}
