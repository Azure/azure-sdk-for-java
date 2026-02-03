// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

/**
 * The {@code UserAgentOptions} class is used to configure the user agent string for HTTP requests.
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class UserAgentOptions {
    private String sdkName;
    private String sdkVersion;
    private String applicationId;

    /**
     * Creates a new instance of {@link UserAgentOptions}.
     */
    public UserAgentOptions() {
    }

    /**
     * Sets the SDK name to be used in the user agent.
     *
     * @param sdkName The SDK name to set.
     * @return The updated {@link UserAgentOptions} instance.
     */
    public UserAgentOptions setSdkName(String sdkName) {
        this.sdkName = sdkName;
        return this;
    }

    /**
     * Sets the SDK version to be used in the user agent.
     *
     * @param sdkVersion The SDK version to set.
     * @return The updated {@link UserAgentOptions} instance.
     */
    public UserAgentOptions setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    /**
     * Sets the application ID to be used in the user agent.
     *
     * @param applicationId The application ID to set.
     * @return The updated {@link UserAgentOptions} instance.
     */
    public UserAgentOptions setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    /**
     * Gets the SDK name for the user agent.
     *
     * @return The SDK name.
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
