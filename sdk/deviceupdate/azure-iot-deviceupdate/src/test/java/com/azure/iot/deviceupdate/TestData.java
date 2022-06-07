// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.util.Configuration;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TestData {
    public static final String TENANT_ID = getTestValue("TENANT_ID", "tenantId");

    public static final String CLIENT_ID = getTestValue("CLIENT_ID", "clientId");

    public static final String ACCOUNT_ENDPOINT = getTestValue("ACCOUNT_ENDPOINT",
        "contosoprod.api.prod.adu.microsoft.com");

    public static final String INSTANCE_ID = getTestValue("INSTANCE_ID", "sdkinstance");

    public static final String PROVIDER = "fabrikam";

    public static final String NAME = "vacuum";

    public static final String VERSION = "2022.401.504.6";

    public static final String DEVICE_GROUP = "dpokluda-test";

    private static String getTestValue(String name, String defaultValue) {
        if (Configuration.getGlobalConfiguration().contains(name)) {
            return Configuration.getGlobalConfiguration()
                .get(name);
        } else {
            String value = System.getenv("AZURE_" + name);
            if (value == null) {
                value = defaultValue;
            }
            Configuration c = Configuration.getGlobalConfiguration();

            Configuration.getGlobalConfiguration().put(name, value);
            return value;
        }
    }
}
