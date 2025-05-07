// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration;

import com.azure.v2.data.appconfiguration.models.SettingFields;
import com.azure.v2.data.appconfiguration.models.SettingSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SettingSelectorTest {

    @Test
    public void nullSettingSelectorFields() {
        final SettingSelector settingSelector = new SettingSelector();
        assertEquals("SettingSelector(keyFilter=null, labelFilter=null, acceptDateTime=null, fields=ALL_FIELDS)",
            settingSelector.toString());
    }

    @Test
    public void toStringTest() {
        final SettingSelector settingSelector
            = new SettingSelector().setFields(SettingFields.ETAG, SettingFields.CONTENT_TYPE);
        assertEquals(
            "SettingSelector(keyFilter=null, labelFilter=null, acceptDateTime=null, fields=etag, content_type)",
            settingSelector.toString());
    }
}
