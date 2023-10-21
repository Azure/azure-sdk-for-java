// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.appconfiguration;

import android.util.Log;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.List;
import java.util.stream.Collectors;

public class WatchFeature {
    /**
     * Runs the sample algorithm and demonstrates how to read configuration setting revision history.
     */

    private static final String TAG = "WatchFeatureOutput";

    /**
     * A refresh method that runs every day to update settings and returns a updated settings.
     *
     * @param client a configuration client.
     * @param watchSettings a list of settings in the watching store.
     *
     * @return a list of updated settings that doesn't match previous ETag value.
     */
    public static List<ConfigurationSetting> refresh(ConfigurationClient client,
        List<ConfigurationSetting> watchSettings) {
        return watchSettings
                   .stream()
                   .filter(setting -> {
                       ConfigurationSetting retrievedSetting = client.getConfigurationSetting(setting.getKey(),
                           setting.getLabel());
                       String latestETag = retrievedSetting.getETag();
                       String watchingETag = setting.getETag();
                       if (!latestETag.equals(watchingETag)) {
                           Log.i(TAG, String.format(
                               "Some keys in watching key store matching the key [%s] and label [%s] is updated, "
                                   + "preview ETag value [%s] not equals to current value [%s].%n",
                               retrievedSetting.getKey(), retrievedSetting.getLabel(), watchingETag, latestETag));
                           setting.setETag(latestETag).setValue(retrievedSetting.getValue());
                           return true;
                       }
                       return false;
                   })
                   .collect(Collectors.toList());
    }
}
