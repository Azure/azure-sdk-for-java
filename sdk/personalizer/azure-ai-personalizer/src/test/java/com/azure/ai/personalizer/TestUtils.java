package com.azure.ai.personalizer;

import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;

public final class TestUtils {

    static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    public static final String AZURE_PERSONALIZER_ENDPOINT_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("AZURE_PERSONALIZER_ENDPOINT");

    public static PersonalizerAudience getAudience(String endpoint) {
        String authority = getAuthority(endpoint);
        switch (authority) {
            case AzureAuthorityHosts.AZURE_PUBLIC_CLOUD:
                return PersonalizerAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD;

            case AzureAuthorityHosts.AZURE_CHINA:
                return PersonalizerAudience.AZURE_RESOURCE_MANAGER_CHINA;

            case AzureAuthorityHosts.AZURE_GOVERNMENT:
                return PersonalizerAudience.AZURE_RESOURCE_MANAGER_US_GOVERNMENT;

            default:
                return null;
        }
    }

    public static String getAuthority(String endpoint) {
        if (endpoint == null) {
            return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }

        if (endpoint.contains(".io")) {
            return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }

        if (endpoint.contains(".cn")) {
            return AzureAuthorityHosts.AZURE_CHINA;
        }

        if (endpoint.contains(".us")) {
            return AzureAuthorityHosts.AZURE_GOVERNMENT;
        }

        // By default, we will assume that the authority is public
        return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
    }

}
