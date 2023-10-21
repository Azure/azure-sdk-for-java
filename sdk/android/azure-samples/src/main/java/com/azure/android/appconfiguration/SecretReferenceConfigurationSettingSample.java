// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.appconfiguration;

import android.util.Log;


import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;


public class SecretReferenceConfigurationSettingSample {

    private static final String TAG = "SecretReferenceConfigurationSettingOutput";

    public static void printSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting) {
        Log.i(TAG, String.format("Key: %s, Secret ID: %s, Content Type: %s, Value: %s%n", setting.getKey(),
            setting.getSecretId(), setting.getContentType(), setting.getValue()));
    }
}
