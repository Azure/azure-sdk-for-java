// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.util.Configuration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TestData {
    public static final String ACCOUNT_ENDPOINT = GetTestValue("ACCOUNT_ENDPOINT",
        "contosoprod.api.prod.adu.microsoft.com");

    public static final String TENANT_ID = GetTestValue("TENANT_ID", "tenantId");

    public static final String CLIENT_ID = GetTestValue("CLIENT_ID", "clientId");

    public static final String INSTANCE_ID = GetTestValue("INSTANCE_ID", "blue");

    public static final String PROVIDER = "Contoso";

    public static final String NAME = "Virtual-Machine";

    public static final String VERSION = GetTestValue("UPDATE_VERSION", "2021.302.1202.48");

    public static final String OPERATION_ID = GetTestValue("UPDATE_OPERATION",
        "e3a75d1b-e359-4bbd-a84c-68fbfa8b7b9f?api-version=2");

    public static final String DEVICE_CLASS_ID = "b83e3c87fbf98063c20c3269f1c9e58d255906dd";

    public static final String DEVICE_ID = GetTestValue("DEVICE_ID", "dpokluda-test");

    public static final String DEPLOYMENT_ID = GetTestValue("DEPLOYMENT_ID", "dpokluda-test-2021-302-1202-48");

    public static final OffsetDateTime CREATE_DEPLOYMENT_START = OffsetDateTime
        .of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static String GetTestValue(String name, String defaultValue) {
        if (Configuration.getGlobalConfiguration().contains(name)) {
            return Configuration.getGlobalConfiguration()
                .get(name);
        }
        else {
            String value = System.getenv("AZURE_" + name);
            if (value == null)
            {
                value = defaultValue;
            }
            Configuration c = Configuration.getGlobalConfiguration();

            Configuration.getGlobalConfiguration().put(name, value);
            return value;
        }
    }
}
