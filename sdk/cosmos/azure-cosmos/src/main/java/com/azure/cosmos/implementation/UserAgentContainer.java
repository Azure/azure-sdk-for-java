// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.net.IDN;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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
    private String suffix;
    private String userAgent;
    public final static String AZSDK_USERAGENT_PREFIX = "azsdk-java-";

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

    public void setSuffix(String suffix) {
        if (suffix.length() > maxSuffixLength) {
            suffix = suffix.substring(0, maxSuffixLength);
        }

        this.suffix = suffix;
        this.userAgent = stripNonAsciiCharacters(baseUserAgent.concat(" ").concat(this.suffix));
    }

    public String getUserAgent() {
        return this.userAgent;
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
