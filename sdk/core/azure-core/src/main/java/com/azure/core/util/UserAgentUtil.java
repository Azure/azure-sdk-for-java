// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Utility for building user agent string for Azure client libraries as specified in the
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">design guidelines</a>.
 */
public final class UserAgentUtil {
    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH = "'applicationId' length cannot be greater than "
        + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";
    public static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    // From the design guidelines, the platform info format is:
    // <language runtime>; <os name> <os version>
    private static final String PLATFORM_INFO_FORMAT = "%s; %s; %s";

    // Maximum length of application id defined in the design guidelines.
    private static final int MAX_APP_ID_LENGTH = 24;

    private UserAgentUtil() {
        // don't instantiate
    }

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param applicationId Name of the application.
     * @param sdkName Name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @param configuration The configuration to use to determine if platform info should be included in the user agent
     * string.
     *
     * @return User agent string as specified in design guidelines.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or larger than 24 in length.
     */
    public static String toUserAgentString(String applicationId, String sdkName, String sdkVersion,
        Configuration configuration) {
        StringBuilder userAgentBuilder = new StringBuilder();

        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw new IllegalArgumentException(INVALID_APPLICATION_ID_LENGTH);
            } else if (applicationId.contains(" ")) {
                throw new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE);
            } else {
                userAgentBuilder.append(applicationId).append(" ");
            }
        }

        // Add the required default User-Agent string.
        userAgentBuilder.append(DEFAULT_USER_AGENT_HEADER)
            .append("-")
            .append(sdkName)
            .append("/")
            .append(sdkVersion);

        // Only add the platform telemetry if it is allowed as it is optional.
        if (!isTelemetryDisabled(configuration)) {
            userAgentBuilder.append(" ")
                .append("(")
                .append(getPlatformInfo())
                .append(")");
        }

        return userAgentBuilder.toString();
    }

    /**
     * Retrieves the platform information telemetry that is appended to the User-Agent header.
     */
    private static String getPlatformInfo() {
        String javaVersion = Configuration.getGlobalConfiguration().get("java.version");
        String osName = Configuration.getGlobalConfiguration().get("os.name");
        String osVersion = Configuration.getGlobalConfiguration().get("os.version");

        return String.format(PLATFORM_INFO_FORMAT, javaVersion, osName, osVersion);
    }

    /**
     * Retrieves the telemetry disabled flag from the passed configuration if it isn't {@code null} otherwise it will
     * check in the global configuration.
     */
    private static boolean isTelemetryDisabled(Configuration configuration) {
        return (configuration == null)
            ? Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false)
            : configuration.get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false);
    }

}
