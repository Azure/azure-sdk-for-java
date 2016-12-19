/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppSetting;

/**
 * An immutable client-side representation of an app setting on a web app.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppSettingImpl implements AppSetting {
    private String key;
    private String value;
    private boolean sticky;

    AppSettingImpl(String key, String value, boolean sticky) {
        this.key = key;
        this.value = value;
        this.sticky = sticky;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean sticky() {
        return sticky;
    }
}
