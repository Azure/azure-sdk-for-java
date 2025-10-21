// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * Used internally. The user agent object, which is used to track the version of the Java SDK of the Azure Cosmos DB database service.
 */
public class UserAgentContainer {
    private static final Pattern nonASCII = Pattern.compile("[^\\x00-\\x7f]");
    private static final Pattern markers = Pattern.compile("\\p{M}");
    private static final int MAX_USER_AGENT_LENGTH = 255;
    private final int maxSuffixLength;
    private final String baseUserAgent;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
    private String suffix;
    private String userAgent;
    private String baseUserAgentWithSuffix;
    public final static String AZSDK_USERAGENT_PREFIX = "azsdk-java-";

    public final static String BASE_USER_AGENT_STRING = Utils.getUserAgent(
        HttpConstants.Versions.SDK_NAME,
        HttpConstants.Versions.getSdkVersion());

    private UserAgentContainer(String sdkName, String sdkVersion) {
        this.baseUserAgent = Utils.getUserAgent(sdkName, sdkVersion);
        this.suffix = "";
        this.userAgent = baseUserAgent;
        this.maxSuffixLength = MAX_USER_AGENT_LENGTH - 1 - baseUserAgent.length();
    }

    public UserAgentContainer() {
        this(HttpConstants.Versions.SDK_NAME, HttpConstants.Versions.getSdkVersion());
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setFeatureEnabledFlagsAsSuffix(Set<UserAgentFeatureFlags> userAgentFeatureFlags) {

        if (userAgentFeatureFlags == null || userAgentFeatureFlags.isEmpty()) {
            return;
        }

        writeLock.lock();
        try {

            int value = 0;

            for (UserAgentFeatureFlags userAgentFeatureFlag : userAgentFeatureFlags) {
                value += userAgentFeatureFlag.getValue();
            }

            this.userAgent = !Strings.isNullOrEmpty(this.baseUserAgentWithSuffix) ? this.baseUserAgentWithSuffix : this.baseUserAgent;
            this.userAgent = this.userAgent + "|F" + Integer.toHexString(value).toUpperCase(Locale.ROOT);
        } finally {
            writeLock.unlock();
        }
    }

    public void setSuffix(String suffix) {
        writeLock.lock();
        try {
            if (suffix == null) {
                suffix = "";
            }

            if (suffix.length() > maxSuffixLength) {
                suffix = suffix.substring(0, maxSuffixLength);
            }

            this.suffix = suffix;
            this.userAgent = stripNonAsciiCharacters(baseUserAgent.concat(" ").concat(this.suffix));
            this.baseUserAgentWithSuffix = this.userAgent;
        } finally {
            writeLock.unlock();
        }
    }

    public String getUserAgent() {
        readLock.lock();
        try {
            return this.userAgent;
        } finally {
            readLock.unlock();
        }
    }

    private static String stripNonAsciiCharacters(String input) {
        // replace accents and diacriticals with their ASCII match (+ marker which we will remove in next step)
        String normalized = input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD);

        // Stripping off the markers
        String strippedMarkers = markers.matcher(normalized).replaceAll("");

        // Just to be sure replace all remaining non-ASCII chars with _
        return nonASCII.matcher(strippedMarkers).replaceAll("_");
    }
}
