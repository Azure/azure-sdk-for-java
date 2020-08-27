// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * Used internally. The user agent object, which is used to track the version of the Java SDK of the Azure Cosmos DB database service.
 */
public class UserAgentContainer {

    private static final int MAX_SUFFIX_LENGTH = 64;
    private final String baseUserAgent;
    private String suffix;
    private String userAgent;
    public final static String AZSDK_USERAGENT_PREFIX = "azsdk-java-";

    private UserAgentContainer(String sdkName, String sdkVersion) {
        this.baseUserAgent = Utils.getUserAgent(sdkName, sdkVersion);
        this.suffix = "";
        this.userAgent = baseUserAgent;
    }

    public UserAgentContainer() {
        this(HttpConstants.Versions.SDK_NAME, HttpConstants.Versions.SDK_VERSION);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        if (suffix.length() > MAX_SUFFIX_LENGTH) {
            suffix = suffix.substring(0, MAX_SUFFIX_LENGTH);
        }

        this.suffix = suffix;
        this.userAgent = baseUserAgent.concat(" ").concat(this.suffix);
    }

    public String getUserAgent() {
        return this.userAgent;
    }
}
