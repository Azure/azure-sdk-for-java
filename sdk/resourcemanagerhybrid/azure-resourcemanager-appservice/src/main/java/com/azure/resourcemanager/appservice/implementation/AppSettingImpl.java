// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.AppSetting;

/** An immutable client-side representation of an app setting on a web app. */
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
