package com.azure.security.keyvault.jca;

import java.util.List;

public class PropertyConvertorUtils {

    public static void putEnvironmentPropertyToSystemProperty(List<String> key) {
        key.forEach(
            environmentPropertyKey -> {
                String value = System.getenv(environmentPropertyKey);
                String systemPropertyKey = environmentPropertyKey.toLowerCase().replaceFirst("azure_keyvault_",
                    "azure.keyvault.").replaceAll("_", "-");
                System.getProperties().put(systemPropertyKey, value);
            }
        );
    }
}
