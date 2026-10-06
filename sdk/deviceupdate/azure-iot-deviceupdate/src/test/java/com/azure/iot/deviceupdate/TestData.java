// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.util.Configuration;

public class TestData {
    public static final String TENANT_ID = getTestValue("TENANT_ID", "tenantId");

    public static final String CLIENT_ID = getTestValue("CLIENT_ID", "clientId");

    public static final String ACCOUNT_ENDPOINT = getTestValue("ACCOUNT_ENDPOINT", "blue.api.adu.microsoft.com");

    public static final String INSTANCE_ID = getTestValue("INSTANCE_ID", "blue");

    public static final String PROVIDER = "Contoso";

    public static final String NAME = "ScriptTest";

    public static final String FILE_ID = "faea58faa110346f7";

    public static final String VERSION = "1.0.0";

    public static final String DEVICE_GROUP = "contoso";

    public static final String DEVICE_CLASS_ID = "5272ba869f3fd0b562d6608370462574600d0b4e";

    public static final String DEVICE_ID = "test-device";

    public static final String DEPLOYMENT_ID = "f0837fc1-b027-4e8f-acc5-76333229c3b7";

    private static String getTestValue(String name, String defaultValue) {
        if (Configuration.getGlobalConfiguration().contains(name)) {
            return Configuration.getGlobalConfiguration().get(name);
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
