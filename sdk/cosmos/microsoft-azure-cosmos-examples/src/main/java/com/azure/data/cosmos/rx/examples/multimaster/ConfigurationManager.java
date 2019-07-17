// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples.multimaster;

import java.util.Properties;

public class ConfigurationManager {
    public static Properties getAppSettings() {
        return System.getProperties();
    }
}
