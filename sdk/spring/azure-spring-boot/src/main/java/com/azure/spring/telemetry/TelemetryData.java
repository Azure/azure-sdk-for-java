// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.telemetry;

/**
 * This class contains constants like telemetry keys and methods to retrieve telemetry info.
 */
public class TelemetryData {

    public static final String INSTALLATION_ID = "installationId";
    public static final String PROJECT_VERSION = "version";
    public static final String SERVICE_NAME = "serviceName";
    public static final String HASHED_ACCOUNT_NAME = "hashedAccountName";
    public static final String HASHED_NAMESPACE = "hashedNamespace";
    public static final String TENANT_NAME = "tenantName";

    public static String getClassPackageSimpleName(Class<?> clazz) {
        if (clazz == null) {
            return "unknown";
        }

        return clazz.getPackage().getName().replaceAll("\\w+\\.", "");
    }
}
