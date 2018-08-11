package com.microsoft.azure.servicebus.security;

/**
 * This class contains all security related constants.
 * @since 1.2.0
 *
 */
public class SecurityConstants {
    /**
     * Resource URI to be used, for all service bus entities, when requesting authentication token from Azure Active Directory.
     */
    public static final String SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL = "https://servicebus.azure.net/";
    /**
     * JSON web token type.
     */
    public static final String JWT_TOKEN_TYPE = "jwt";
    /**
     * Shared Access Signature token type.
     */
    public static final String SAS_TOKEN_TYPE = "servicebus.windows.net:sastoken";
    /**
     * Default validity of a security token in seconds.
     */
    public static final int DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS = 20*60; // 20 minutes
    /**
     * Max allowed length for security key name.
     */
    public static final int MAX_KEY_NAME_LENGTH = 256;
    /**
     * Max allowed length for security key.
     */
    public static final int MAX_KEY_LENGTH = 256;
    
}
