package com.microsoft.azure.sevicebus.security;

public class SecurityConstants {
    public static final String SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL = "https://servicebus.azure.net/";
    public static final String JWT_TOKEN_TYPE = "jwt";
    public static final String SAS_TOKEN_TYPE = "servicebus.windows.net:sastoken";
    public static final int DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS = 20*60; // 20 minutes
    
}
