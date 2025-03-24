// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

/**
 * The {@code UserAgentOptions} class is used to configure the user agent string for HTTP requests.
 */
public class UserAgentOptions {
    private final String sdkName;
    private final String sdkVersion;
    private String applicationId;

    /**
     * Creates a {@link UserAgentOptions} with the specified SDK name and version.
     *
     * @param sdkName The name of the SDK.
     * @param sdkVersion The version of the SDK.
     */
    public UserAgentOptions(String sdkName, String sdkVersion) {
        this.sdkName = sdkName;
        this.sdkVersion = sdkVersion;
    }

    /**
     * Sets the application ID for the user agent.
     *
     * @param applicationId The application ID to set.
     * @return The current {@link UserAgentOptions} instance.
     */
    public UserAgentOptions setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    /**
     * Gets the application ID for the user agent.
     *
     * @return The application ID.
     */
    public String getSdkName() {
        return sdkName;
    }

    /**
     * Gets the SDK version for the user agent.
     *
     * @return The SDK version.
     */
    public String getSdkVersion() {
        return sdkVersion;
    }

    /**
     * Gets the application ID for the user agent.
     *
     * @return The application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }
}
