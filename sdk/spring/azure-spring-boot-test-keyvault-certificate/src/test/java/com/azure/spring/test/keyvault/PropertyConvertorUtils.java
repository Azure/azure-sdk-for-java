// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.keyvault;

import java.util.List;

public class PropertyConvertorUtils {

    public static final String CERTIFICATE_PREFIX = "certificate_";

    public static final String AZURE_KEYVAULT_URI = System.getenv("CERTIFICATE_AZURE_KEYVAULT_URI");
    public static final String SPRING_CLIENT_ID = System.getenv("CERTIFICATE_AZURE_KEYVAULT_CLIENT_ID");
    public static final String SPRING_CLIENT_SECRET = System.getenv("CERTIFICATE_AZURE_KEYVAULT_CLIENT_SECRET");
    public static final String SPRING_TENANT_ID = System.getenv("CERTIFICATE_AZURE_KEYVAULT_TENANT_ID");
    public static void putEnvironmentPropertyToSystemProperty(List<String> key) {
        key.forEach(
            environmentPropertyKey -> {
                String value = System.getenv(environmentPropertyKey);
                String systemPropertyKey = environmentPropertyKey
                    .toLowerCase()
                    .replaceFirst(CERTIFICATE_PREFIX, "")
                    .replaceFirst("azure_keyvault_", "azure.keyvault.")
                    .replaceAll("_", "-");
                System.getProperties().put(systemPropertyKey, value);
            }
        );
    }
}
