package com.azure.core.util;

/**
 * Utility for building user agent string for Azure client libraries as specified in the
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">design guidelines</a>.
 */
public final class UserAgentUtil {

    public static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    /*
     * The base User-Agent header format is azsdk-java-<client_lib>/<sdk_version>. Additional information such as the
     * application ID will be prepended and platform telemetry will be appended, a fully configured User-Agent header
     * format is <application_id> azsdk-java-<client_lib>/<sdk_version> <platform_info>.
     */
    private static final String DEFAULT_USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s";

    // From the design guidelines, the platform info format is:
    // <language runtime>; <os name> <os version>
    private static final String PLATFORM_INFO_FORMAT = "%s; %s; %s";

    private UserAgentUtil() {
        // don't instantiate
    }

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param sdkName name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @return User agent string as specified in design guidelines.
     */
    public static String toUserAgentString(String sdkName, String sdkVersion) {
        return toUserAgentString(null, sdkName, sdkVersion);
    }

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param sdkName Name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @param configuration The configuration to use to determine if platform info should be included in the user agent
     * string.
     * @return User agent string as specified in design guidelines.
     */
    public static String toUserAgentString(String sdkName, String sdkVersion,
        Configuration configuration) {
        return toUserAgentString(null, sdkName, sdkVersion, configuration);
    }

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param applicationId Name of the application.
     * @param sdkName Name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @return User agent string as specified in design guidelines.
     */
    public static String toUserAgentString(String applicationId, String sdkName, String sdkVersion) {
        return toUserAgentString(applicationId, sdkName, sdkVersion, null);
    }

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param applicationId Name of the application.
     * @param sdkName Name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @param configuration The configuration to use to determine if platform info should be included in the user agent
     * string.
     * @return User agent string as specified in design guidelines.
     */
    public static String toUserAgentString(String applicationId, String sdkName, String sdkVersion,
        Configuration configuration) {
        StringBuilder userAgentBuilder = new StringBuilder();

        // Only add the application ID if it is present as it is optional.
        if (applicationId != null) {
            applicationId = applicationId.length() > 24 ? applicationId.substring(0, 24) : applicationId;
            userAgentBuilder.append(applicationId).append(" ");
        }

        // Add the required default User-Agent string.
        userAgentBuilder.append(String.format(DEFAULT_USER_AGENT_FORMAT, sdkName, sdkVersion));

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
