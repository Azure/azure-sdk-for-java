package com.azure.applicationconfig;

/*
 * Gets the SDK information for this library component.
 */
class AzureConfiguration {
    // User-Agent header value format: azsdk-<sdk_language>-<client_lib>/<sdk_version> <platform_info>
    private static final String USER_AGENT_FORMAT = "azsdk-java-%s/%s %s";

    //TODO: Eventually remove these hardcoded strings with https://github.com/Azure/azure-sdk-for-java/issues/3141
    static final String NAME = "application-configuration";
    static final String VERSION = "1.0.0-SNAPSHOT";

    /*
     * Gets the User-Agent header value using the provided SDK name and version.
     * TODO: Move this out to the common library where it can be consumed by everyone
     */
    static String getUserAgentHeader(String sdkName, String sdkVersion) {
        String platformInfo = System.getProperty("java.version") + "; " + getOsInformation();

        return String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, platformInfo);
    }

    private static String getOsInformation() {
        return String.join(" ", System.getProperty("os.name"), System.getProperty("os.version"));
    }
}
