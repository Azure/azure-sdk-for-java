// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.util.Configuration;

public class TestData {
    public static final String TENANT_ID = getTestValue("TENANT_ID", "tenantId");

    public static final String CLIENT_ID = getTestValue("CLIENT_ID", "clientId");

    public static final String ACCOUNT_ENDPOINT = getTestValue("ACCOUNT_ENDPOINT",
        "contosoprodwus2.api.adu.microsoft.com");

    public static final String INSTANCE_ID = getTestValue("INSTANCE_ID", "blue");

    public static final String PROVIDER = "sdk-tests-provider";

    public static final String NAME = "sdk-tests-name";

    public static final String FILE_ID = "f25626693f7c20ff8";

    public static final String VERSION = "2.0.0.0";

    public static final String DEVICE_GROUP = "sdk-tests-group";

    public static final String DEVICE_CLASS_ID = "c61300e6b3da62926c23d92519ea3f2e73116e71";

    public static final String DEVICE_ID = "sdk-tests-638309378295188213";

    public static final String DEPLOYMENT_ID = "sdk-tests-deployment";

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
