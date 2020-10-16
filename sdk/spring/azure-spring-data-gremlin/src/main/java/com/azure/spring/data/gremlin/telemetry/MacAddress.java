// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Disclaimer:
 *      This class is copied from https://github.com/Microsoft/azure-tools-for-java/ with minor modification (fixing
 *      static analysis error).
 *      Location in the repo: /Utils/azuretools-core/src/com/microsoft/azuretools/azurecommons/util/MacAddress.java
 */
package com.azure.spring.data.gremlin.telemetry;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MacAddress {

    private static final String UNKNOWN_MAC_ADDRESS = "Unknown-Mac-Address";
    private static final String MAC_REGEX = "([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}";
    private static final String MAC_REGEX_ZERO = "([0]{2}[:-]){5}[0]{2}";
    private static final String HASHED_MAC_REGEX = "[0-9a-f]{64}";

    public static boolean isValidHashMacFormat(@NonNull String hashMac) {
        Assert.notNull(hashMac, "hashMac should not be null");
        if (hashMac.isEmpty()) {
            return false;
        }

        return Pattern.compile(HASHED_MAC_REGEX).matcher(hashMac).matches();
    }

    private static String getRawMac() {
        final List<String> commands;
        final String os = System.getProperty("os.name");
        final StringBuilder macBuilder = new StringBuilder();

        if (os != null && !os.isEmpty() && os.toLowerCase(Locale.US).startsWith("win")) {
            commands = Collections.singletonList("getmac");
        } else {
            commands = Arrays.asList("ifconfig", "-a");
        }

        try {
            String tmp;
            final ProcessBuilder builder = new ProcessBuilder(commands);
            final Process process = builder.start();
            try (InputStreamReader streamReader = new InputStreamReader(process.getInputStream(), UTF_8);
                 BufferedReader reader = new BufferedReader(streamReader)) {
                while ((tmp = reader.readLine()) != null) {
                    macBuilder.append(tmp);
                }
            }
        } catch (IOException e) {
            return "";
        }
        return macBuilder.toString();
    }

    private static String getHexDigest(byte digest) {
        final String hex = Integer.toString((digest & 0xff) + 0x100, 16);

        return hex.substring(1);
    }

    private static String hash(@NonNull String mac) {
        Assert.notNull(mac, "mac should not be null");
        if (mac.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            messageDigest.update(mac.getBytes(UTF_8));

            final byte[] digestBytes = messageDigest.digest();

            for (final byte digest : digestBytes) {
                builder.append(getHexDigest(digest));
            }
        } catch (NoSuchAlgorithmException ex) {
            return "";
        }

        Assert.isTrue(

            isValidHashMacFormat(builder.toString()), "Invalid format for HashMac");

        return builder.toString();
    }

    public static String getHashMac() {
        final String rawMac = getRawMac();

        if (rawMac.isEmpty()) {
            return UNKNOWN_MAC_ADDRESS;
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

        final String hashMac = hash(mac);

        if (StringUtils.hasText(hashMac)) {
            return hashMac;
        }

        return UNKNOWN_MAC_ADDRESS;
    }
}

