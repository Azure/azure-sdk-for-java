// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.util.Configuration;

public class TestData {
    public static final String TENANT_ID = getTestValue("TENANT_ID", "33e01921-4d64-4f8c-a055-5bdaffd5e33d");

    public static final String CLIENT_ID = getTestValue("CLIENT_ID", "71318fd3-e515-4267-979a-98e06d2b139e");

    public static final String ACCOUNT_ENDPOINT = getTestValue("ACCOUNT_ENDPOINT",
        "contosotest.api.test.adu.microsoft.com");

    public static final String INSTANCE_ID = getTestValue("INSTANCE_ID", "blue");

    public static final String PROVIDER = "fabrikam";

    public static final String NAME = "vacuum";

    public static final String VERSION = "2022.401.504.6";

    public static final String DEVICE_GROUP = "dpokluda-test";

    public static final String GROUP_ID = "";

    public static final String DEVICE_CLASS_ID = "a8bcf456c07b9e7626db43c7ced5ac63e2238b6a";

    public static final String DEVICE_ID = "";

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
